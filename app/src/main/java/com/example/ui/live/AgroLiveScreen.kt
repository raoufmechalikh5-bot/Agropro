package com.example.ui.live

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.NoPhotography
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
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

    DisposableEffect(viewModel) {
        onDispose { viewModel.endSession() }
    }

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
                val frame: Bitmap? = runCatching { image.toAgroBitmap() }.getOrNull()
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
                    listOf(Color(0xFF06110C), Color(0xFF10251A), Color(0xFF07130D))
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B3A29),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5AA879).copy(alpha = 0.35f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFB8F0C9), modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("AgroPro Live", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("مساعدك الزراعي المباشر", color = Color(0xFFA8C7B3), style = MaterialTheme.typography.labelMedium)
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF173A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FA873).copy(alpha = 0.5f))
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (state.connected) Color(0xFF57E38C) else Color(0xFFFFC46B)))
                    Text(if (state.connected) "LIVE" else if (state.connecting) "CONNECTING" else "READY", color = Color(0xFFDDF5E5), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = {
                viewModel.endSession()
                onClose()
            }) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFFD7E9DD))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14271D)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
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
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.radialGradient(listOf(Color(0xFF28543A), Color(0xFF142B1E), Color(0xFF0A1710)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF315D42).copy(alpha = 0.75f), modifier = Modifier.size(86.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.NoPhotography, contentDescription = null, tint = Color(0xFFD0F1D9), modifier = Modifier.size(36.dp))
                                }
                            }
                            Text("الكاميرا متوقفة", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("فعّل الكاميرا ليشاهد المساعد النبات أو المحصول", color = Color(0xFFB8D5C3), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xD908160F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(if (state.connected) Color(0xFF55D98A) else Color(0xFFFFC46B)))
                        Text(
                            when {
                                state.connecting -> "جارٍ الاتصال بالمساعد"
                                state.connected && state.microphoneEnabled -> "أستمع إليك الآن"
                                state.connected -> "الميكروفون متوقف"
                                else -> "بانتظار بدء الجلسة"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (state.connecting) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF9BE0B0))
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xE609160F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8ACBA0).copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiveAssistantOrb(active = state.connected && state.microphoneEnabled)
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("AGRO AI", color = Color(0xFFE4F8EA), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                if (state.connected && state.microphoneEnabled) "المساعد يستمع" else "مساعدك الذكي",
                                color = Color(0xFFA8CDB5),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4A2525)), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(state.error.orEmpty(), color = Color.White)
                    TextButton(onClick = viewModel::connect) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                        Text("إعادة المحاولة", color = Color.White)
                    }
                }
            }
        }

        if (!state.connected && !state.connecting) {
            Button(
                onClick = {
                    if (context.hasPermission(Manifest.permission.RECORD_AUDIO)) viewModel.connect()
                    else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF328052))
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text("بدء المحادثة المباشرة", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                modifier = Modifier.weight(1f),
                icon = if (state.microphoneEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                label = if (state.microphoneEnabled) "الصوت" else "تشغيل الصوت",
                active = state.microphoneEnabled,
                enabled = state.connected,
                onClick = {
                    if (!context.hasPermission(Manifest.permission.RECORD_AUDIO)) microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    else viewModel.setMicrophoneEnabled(!state.microphoneEnabled)
                }
            )
            ControlButton(
                modifier = Modifier.weight(1f),
                icon = if (state.cameraEnabled) Icons.Default.CameraAlt else Icons.Default.NoPhotography,
                label = if (state.cameraEnabled) "الكاميرا" else "تشغيل الكاميرا",
                active = state.cameraEnabled,
                enabled = state.connected,
                onClick = {
                    if (state.cameraEnabled) viewModel.setCameraEnabled(false)
                    else if (context.hasPermission(Manifest.permission.CAMERA)) viewModel.setCameraEnabled(true)
                    else cameraPermission.launch(Manifest.permission.CAMERA)
                }
            )
        }

        Surface(
            onClick = {
                viewModel.endSession()
                onClose()
            },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF351D1D),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB65B55).copy(alpha = 0.45f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFFC4BE), modifier = Modifier.size(18.dp))
                Text("إنهاء المحادثة", modifier = Modifier.padding(start = 8.dp), color = Color(0xFFFFD7D2), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LiveAssistantOrb(
    modifier: Modifier = Modifier,
    active: Boolean
) {
    val transition = rememberInfiniteTransition(label = "live_orb")
    val pulse = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_orb_pulse"
    )
    Surface(
        modifier = modifier.size(68.dp).graphicsLayer {
            scaleX = if (active) pulse.value else 1f
            scaleY = if (active) pulse.value else 1f
        },
        shape = CircleShape,
        color = Color(0xD9153023),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF9BE0B0).copy(alpha = 0.72f)
        ),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF4CA66B), Color(0xFF173D2A), Color(0xFF0B1C13))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "AgroPro AI",
                tint = Color(0xFFE0F8E8),
                modifier = Modifier.size(30.dp)
            )
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


private fun ImageProxy.toAgroBitmap(): Bitmap? {
    if (format != ImageFormat.YUV_420_888) return null
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]
    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer
    val nv21 = ByteArray(width * height * 3 / 2)
    var outputIndex = 0

    for (row in 0 until height) {
        for (column in 0 until width) {
            nv21[outputIndex++] = yBuffer.get(row * yPlane.rowStride + column * yPlane.pixelStride)
        }
    }
    for (row in 0 until height / 2) {
        for (column in 0 until width / 2) {
            nv21[outputIndex++] = vBuffer.get(row * vPlane.rowStride + column * vPlane.pixelStride)
            nv21[outputIndex++] = uBuffer.get(row * uPlane.rowStride + column * uPlane.pixelStride)
        }
    }

    val jpeg = ByteArrayOutputStream()
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    if (!yuvImage.compressToJpeg(Rect(0, 0, width, height), 82, jpeg)) return null
    val bytes = jpeg.toByteArray()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
