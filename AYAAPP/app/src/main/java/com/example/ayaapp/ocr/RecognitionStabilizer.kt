package com.example.ayaapp.ocr

class RecognitionStabilizer(
    private val windowSize: Int = 5,
    private val threshold: Int = 3
) {
    private val buf = ArrayDeque<Pair<String, String>>()

    fun add(candidate: Pair<String, String>): Pair<String, String>? {
        buf.addLast(candidate)
        if (buf.size > windowSize) buf.removeFirst()
        val counts = buf.groupingBy { it }.eachCount()
        val top = counts.maxByOrNull { it.value } ?: return null
        return if (top.value >= threshold) top.key else null
    }

    fun clear() = buf.clear()
}
