package com.example.ui.live

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.core.toBitmap
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@Composable
fun AgroLiveScreen(
    viewModel: AgroLiveViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (viewModel.uiState.value.connected) viewModel.setMicrophoneEnabled(true)
            else viewModel.connect()
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setCameraEnabled(granted)
    }

    LaunchedEffect(Unit) {
        if (context.hasPermission(Manifest.permission.RECORD_AUDIO)) {
            viewModel.connect()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val lastFrameAt = remember { AtomicLong(0L) }
    val latestState by rememberUpdatedState(state)

    DisposableEffect(cameraController, analysisExecutor) {
        cameraController.setImageAnalysisAnalyzer(analysisExecutor) { image: ImageProxy ->
            val now = System.currentTimeMillis()
            if (now - lastFrameAt.get() >= 1_000L && latestState.cameraEnabled && latestState.connected) {
                lastFrameAt.set(now)
                val frame: Bitmap? = runCatching { image.toBitmap() }.getOrNull()
                frame?.let(viewModel::sendVideoFrame)
            }
            image.close()
        }
        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
            runCatching { cameraController.unbind() }
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(state.cameraEnabled) {
        if (state.cameraEnabled) {
            runCatching { cameraController.bindToLifecycle(lifecycleOwner) }
                .onFailure { viewModel.setCameraEnabled(false) }
        } else {
            runCatching { cameraController.unbind() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF07140F), Color(0xFF10291D), Color(0xFF07140F))
                )
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AgroPro Live",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "مساعدك الزراعي بالصوت والصورة",
                    color = Color(0xFFB8D5C3),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = {
                viewModel.endSession()
                onClose()
            }) {
                Icon(Icons.Default.Close, contentDescription = "إنهاء الجلسة", tint = Color.White)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14251D)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.cameraEnabled) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { viewContext ->
                            PreviewView(viewContext).apply {
                                controller = cameraController
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        update = { it.controller = cameraController }
                    )
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF234A35),
                            modifier = Modifier.size(104.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraOff,
                                    contentDescription = null,
                                    tint = Color(0xFFBFE8CC),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Text("الكاميرا متوقفة", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "شغّل الكاميرا ليتمكن المساعد من رؤية النبات أو المحصول مباشرة.",
                            color = Color(0xFFB8D5C3),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xCC07140F)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            Modifier.size(8.dp).clip(CircleShape)
                                .background(if (state.connected) Color(0xFF55D98A) else Color(0xFFFFC46B))
                        )
                        Text(
                            when {
                                state.connecting -> "جارٍ الاتصال..."
                                state.connected -> if (state.microphoneEnabled) "المساعد يستمع" else "الصوت متوقف"
                                else -> "بانتظار الاتصال"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (state.connecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF9BE0B0)
                    )
                }
            }
        }

        AnimatedVisibility(visible = state.error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A2525)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(state.error.orEmpty(), color = Color.White)
                    TextButton(onClick = viewModel::connect) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                        Text("إعادة المحاولة", color = Color.White)
                    }
                }
            }
        }

        Text(
            if (state.connected) "يمكنك التحدث بشكل طبيعي، وسيحافظ المساعد على سياق المحادثة." else
                "اسمح بالوصول إلى الميكروفون لبدء المحادثة المباشرة.",
            color = Color(0xFFB8D5C3),
            style = MaterialTheme.typography.bodySmall
        )

        if (!state.connected && !state.connecting) {
            Button(
                onClick = {
                    if (context.hasPermission(Manifest.permission.RECORD_AUDIO)) viewModel.connect()
                    else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C6844))
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text("بدء المحادثة المباشرة", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                modifier = Modifier.weight(1f),
                icon = if (state.microphoneEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                label = if (state.microphoneEnabled) "كتم الصوت" else "تشغيل الصوت",
                active = state.microphoneEnabled,
                enabled = state.connected,
                onClick = {
                    if (!context.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                        microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.setMicrophoneEnabled(!state.microphoneEnabled)
                    }
                }
            )
            ControlButton(
                modifier = Modifier.weight(1f),
                icon = if (state.cameraEnabled) Icons.Default.CameraAlt else Icons.Default.CameraOff,
                label = if (state.cameraEnabled) "إيقاف الكاميرا" else "تشغيل الكاميرا",
                active = state.cameraEnabled,
                enabled = state.connected,
                onClick = {
                    if (state.cameraEnabled) {
                        viewModel.setCameraEnabled(false)
                    } else if (context.hasPermission(Manifest.permission.CAMERA)) {
                        viewModel.setCameraEnabled(true)
                    } else {
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }

        Button(
            onClick = {
                viewModel.endSession()
                onClose()
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B3B35),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Text("إنهاء المحادثة", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ControlButton(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (active) Color(0xFF2C6844) else Color(0xFF1B3025),
        contentColor = Color.White,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Text(label, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Context.hasPermission(permission: String): Boolean =
    androidx.core.content.ContextCompat.checkSelfPermission(
        this,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
