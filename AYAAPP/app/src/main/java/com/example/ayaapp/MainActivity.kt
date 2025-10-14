package com.example.ayaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.ayaapp.ui.AyaAppScreen
import com.example.ayaapp.ui.theme.AyaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AyaAppViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AyaTheme(darkTheme = true) { AyaAppScreen(viewModel) } }
    }
}