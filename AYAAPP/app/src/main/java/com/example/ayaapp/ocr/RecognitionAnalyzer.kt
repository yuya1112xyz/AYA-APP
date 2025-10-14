package com.example.ayaapp.ocr

import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.hypot

/**
 * カメラフレームから「中央アルファベット + 円形マーク外周の数字列」を抽出するアナライザ。
 * - ViewModel 側から suspend で呼ぶ前提（ImageAnalysis.Analyzer は実装しません）
 * - 連続フレームの多数決で確定したときのみ onStablePair をコールします
 */
class RecognitionAnalyzer(
    private val onStablePair: (letter: String, number: String) -> Unit,
    windowSize: Int = 5,
    threshold: Int = 3
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val stabilizer = RecognitionStabilizer(windowSize = windowSize, threshold = threshold)

    /**
     * 1フレームを解析。確定したときだけ onStablePair を呼ぶ。
     * 呼び出し側（ViewModel）は、解析後に必ず imageProxy.close() されるよう try/finally で呼びます。
     */
    suspend fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        try {
            val result = recognizer.process(input).await() // TaskAwait.kt にある拡張に依存
            val pair = pickCenterLetterAndOuterDigits(result, input.width, input.height)
            if (pair != null) {
                val stable = stabilizer.add(pair)
                if (stable != null) onStablePair(stable.first, stable.second)
            }
        } finally {
            imageProxy.close()
        }
    }

    // ---------- 内部ロジック ----------

    private data class Node(val value: String, val box: RectF)

    /**
     * 文字要素から、中心に最も近い 1文字アルファベットを「中央字」、
     * 中心から一定距離以上にある純数字群を左→右で連結して「外周数字列」として抽出。
     */
    private fun pickCenterLetterAndOuterDigits(text: Text, w: Int, h: Int): Pair<String, String>? {
        val nodes = buildList {
            for (b in text.textBlocks) for (l in b.lines) for (e in l.elements) {
                val v = e.text.trim()
                val bb = e.boundingBox ?: continue
                if (v.isNotEmpty()) add(Node(v, RectF(bb)))
            }
        }
        if (nodes.isEmpty()) return null

        val cx = w / 2f
        val cy = h / 2f

        // 1) 中央アルファベット：中心距離が最小の「1文字の英字」
        val letter = nodes
            .filter { it.value.length == 1 && it.value[0].isLetter() }
            .minByOrNull { distance(it.box.centerX(), it.box.centerY(), cx, cy) }
            ?.value?.uppercase() ?: return null

        // 2) 外周数字列：中心から閾値以上の純数字を左→右で連結
        val innerRadius = maxOf(w, h) * 0.20f  // 必要なら 0.25f/0.30f に調整
        val digits = nodes
            .filter { it.value.all(Char::isDigit) }
            .filter { distance(it.box.centerX(), it.box.centerY(), cx, cy) >= innerRadius }
            .sortedBy { it.box.centerX() }
            .joinToString("") { it.value }

        return if (digits.isNotBlank()) letter to digits else null
    }

    private fun RectF.centerX() = (left + right) / 2f
    private fun RectF.centerY() = (top + bottom) / 2f
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float) = hypot(x1 - x2, y1 - y2)
}