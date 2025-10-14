package com.example.ayaapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.camera.view.PreviewView
import com.example.ayaapp.AyaAppViewModel
import com.example.ayaapp.data.RecognitionResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AyaAppScreen(
    viewModel: AyaAppViewModel,
    modifier: Modifier = Modifier
) {
    // --- State from ViewModel ---
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val filtered by viewModel.filteredResults.collectAsStateWithLifecycle(emptyList())
    val status by viewModel.status.collectAsStateWithLifecycle("")

    // --- Local state (UI only) ---
    var analyzing by remember { mutableStateOf(true) }

    // --- Permissions ---
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // 初回に履歴を読み込み
    LaunchedEffect(Unit) { viewModel.loadHistory() }

    // 端末設定で既に権限が許可されている場合は state にも反映
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted && !cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "AYA-APP") },
                actions = {
                    IconButton(onClick = { viewModel.saveLatestResult() }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    analyzing = !analyzing
                    viewModel.toggleAnalyzing()
                }
            ) {
                Icon(
                    imageVector = if (analyzing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (analyzing) "解析一時停止" else "解析再開"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ===== カメラ領域 =====
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp) // 端末に合わせて調整可
            ) {
                if (cameraPermission.status.isGranted) {
                    val previewView = remember { PreviewView(context) }

                    // startCamera は一度だけ呼ぶ
                    LaunchedEffect(previewView, lifecycleOwner) {
                        viewModel.startCamera(previewView, lifecycleOwner)
                    }
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PermissionRationale(
                        onRequest = { cameraPermission.launchPermissionRequest() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 検索 =====
            var tf by remember(searchQuery) { mutableStateOf(TextFieldValue(searchQuery)) }
            OutlinedTextField(
                value = tf,
                onValueChange = {
                    tf = it
                    viewModel.onSearchQueryChanged(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                singleLine = true,
                label = { Text("検索（アルファベット/数字 部分一致）") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 結果リスト（新着が先頭） =====
            ResultsList(items = filtered, modifier = Modifier.weight(1f))

            // ===== ステータス表示 =====
            Divider()
            Text(
                text = status,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ResultsList(items: List<RecognitionResult>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items) { item ->
            ResultRow(item)
        }
    }
}

@Composable
private fun ResultRow(item: RecognitionResult) {
    // 左：アルファベット / 右：数字
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.letter,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = item.number,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("カメラ権限が必要です。プレビューとOCRのために許可してください。")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) {
            Text("カメラを許可")
        }
    }
}