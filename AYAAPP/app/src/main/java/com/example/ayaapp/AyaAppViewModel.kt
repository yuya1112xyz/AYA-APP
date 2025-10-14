package com.example.ayaapp

import android.app.Application
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.ayaapp.data.AppDatabase
import com.example.ayaapp.data.RecognitionResult
import com.example.ayaapp.data.ResultRepository
import com.example.ayaapp.ocr.RecognitionAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AyaAppViewModel(app: Application) : AndroidViewModel(app) {

    // --- UI State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _results = MutableStateFlow<List<RecognitionResult>>(emptyList())
    private val _filtered = MutableStateFlow<List<RecognitionResult>>(emptyList())
    val filteredResults = _filtered.asStateFlow()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    // --- Data ---
    private val dao = AppDatabase.get(app).resultDao()
    private val repo = ResultRepository(dao)

    // --- CameraX ---
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var isAnalyzing = true

    // --- OCR Analyzer ---
    private val analyzer = RecognitionAnalyzer(
        onStablePair = { letter, number ->
            val newItem = RecognitionResult(letter = letter, number = number)
            _results.value = listOf(newItem) + _results.value
            applyFilter()
            _status.value = "認識: $letter - $number"
        },
        windowSize = 5,
        threshold = 3
    )


    // 検索クエリ更新
    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        applyFilter()
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim()
        _filtered.value = if (q.isBlank()) _results.value
        else _results.value.filter {
            it.letter.contains(q, ignoreCase = true) || it.number.contains(q)
        }
    }

    // 最新結果を保存
    fun saveLatestResult() = viewModelScope.launch {
        val latest = _results.value.firstOrNull() ?: run {
            _status.value = "保存対象なし"; return@launch
        }
        repo.insert(latest.letter, latest.number)
        _status.value = "保存しました: ${latest.letter}-${latest.number}"
    }

    // 履歴読み込み
    fun loadHistory() = viewModelScope.launch {
        val all = repo.getAll()
        _results.value = all
        applyFilter()
    }


    // 解析の一時停止／再開
    fun toggleAnalyzing() {
        isAnalyzing = !isAnalyzing
        _status.value = if (isAnalyzing) "解析再開" else "解析一時停止"
    }

    // CameraX 起動
    fun startCamera(previewView: PreviewView, owner: LifecycleOwner) {
        val ctx = getApplication<Application>()
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { ia ->
                    ia.setAnalyzer(cameraExecutor) { proxy ->
                        if (!isAnalyzing) { proxy.close(); return@setAnalyzer }
                        viewModelScope.launch { analyzer.analyze(proxy) }
                    }
                }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(owner, selector, preview, imageAnalysis)
                _status.value = "CameraX準備完了"
            } catch (e: Exception) {
                _status.value = "CameraXバインド失敗: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
