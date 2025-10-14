package com.example.ayaapp.data

class ResultRepository(private val dao: ResultDao) {
    suspend fun insert(letter: String, number: String) =
        dao.insert(RecognitionResult(letter = letter, number = number))
    suspend fun getAll() = dao.getAll()
    suspend fun search(q: String) = if (q.isBlank()) dao.getAll() else dao.search("%$q%")
}