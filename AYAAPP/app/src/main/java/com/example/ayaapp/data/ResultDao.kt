package com.example.ayaapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ResultDao {
    @Insert suspend fun insert(result: RecognitionResult)

    @Query("SELECT * FROM results ORDER BY timestamp DESC")
    suspend fun getAll(): List<RecognitionResult>

    @Query("""
        SELECT * FROM results
        WHERE UPPER(letter) LIKE UPPER(:q)
           OR number LIKE :q
        ORDER BY timestamp DESC
    """)
    suspend fun search(q: String): List<RecognitionResult>
}