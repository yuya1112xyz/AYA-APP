package com.example.ayaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class RecognitionResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val letter: String,                     // 中央アルファベット 1文字
    val number: String,                     // 外周の数字（1～n桁）
    val timestamp: Long = System.currentTimeMillis()
)