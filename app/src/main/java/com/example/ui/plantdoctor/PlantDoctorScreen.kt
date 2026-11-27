package com.example.ui.plantdoctor

import com.example.ui.agroText
import com.example.ui.components.ExpandableSuggestions
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.R
import com.example.data.ai.PlantDiagnosisResult
import com.example.data.ai.PlantDoctorUiState
import com.example.data.ai.SelectedPlantImage
import com.example.data.catalog.DiseaseFlashcard
import com.example.data.catalog.FlashcardCatalog
import com.example.data.local.DiagnosisHistoryEntity
import com.example.ui.theme.AgriTheme
import com.example.ui.theme.agriAmbientBackground
import com.example.util.PhotoStorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.weather.AgriWeatherCard


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PlantDoctorScreen(
    viewModel: PlantDoctorViewModel,
    onOpenInNpkCalculator: (cropNameAr: String, diseaseNameAr: String) -> Unit = { _, _ -> },
    onNavigateToNotebook: () -> Unit = {},
    onOpenLiveAssistant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val farmerNotes by viewModel.farmerNotes.collectAsStateWithLifecycle()
    val diagnosisState by viewModel.diagnosisState.collectAsStateWithLifecycle()
    val isFlashcardMode by viewModel.isFlashcardMode.collectAsStateWithLifecycle()
    val selectedCropFilter by viewModel.selectedCropFilter.collectAsStateWithLifecycle()
    val filteredFlashcards by viewModel.filteredFlashcards.collectAsStateWithLifecycle()
    val flippedCardIds by viewModel.flippedCardIds.collectAsStateWithLifecycle()
    val masteredCardIds by viewModel.masteredCardIds.collectAsStateWithLifecycle()
    val recentDiagnoses by viewModel.recentDiagnoses.collectAsStateWithLifecycle()

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var hasRequestedCameraPermission by rememberSaveable { mutableStateOf(false) }
    var showCameraRationaleDialog by remember { mutableStateOf(false) }
    var showCameraSettingsDialog by remember { mutableStateOf(false) }
    var showDiagnosisHistory by rememberSaveable { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            viewModel.loadImageFromUri(context, uri, "صورة كاميرا")
        }
    }

    val launchCameraCapture = remember(context, takePictureLauncher) {
        {
            val photoFile = PhotoStorageHelper.createTempCameraFile(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        }
    }

    val openAppSettings = remember(context) {
        {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        uris.forEachIndexed { index, uri ->
            viewModel.loadImageFromUri(context, uri, "صورة ${index + 1}")
        }
    }

    if (showCameraRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showCameraRationaleDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.camera_permission_rationale_title)) },
            text = { Text(stringResource(R.string.camera_permission_rationale_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraRationaleDialog = false
                        hasRequestedCameraPermission = true
                        cameraPermissionState.launchPermissionRequest()
                    },
                    modifier = Modifier.testTag("camera_rationale_confirm_button")
                ) {
                    Text(stringResource(R.string.camera_permission_grant_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraRationaleDialog = false }) {
                    Text(stringResource(R.string.camera_permission_dismiss))
                }
            },
            modifier = Modifier.testTag("camera_rationale_dialog")
        )
    }

    if (showCameraSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCameraSettingsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.camera_permission_denied_title)) },
            text = { Text(stringResource(R.string.camera_permission_denied_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraSettingsDialog = false
                        openAppSettings()
                    },
                    modifier = Modifier.testTag("camera_settings_confirm_button")
                ) {
                    Text(stringResource(R.string.camera_permission_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraSettingsDialog = false }) {
                    Text(stringResource(R.string.camera_permission_dismiss))
                }
            },
            modifier = Modifier.testTag("camera_settings_dialog")
        )
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme) {
    Box(
        modifier = modifier.fillMaxSize().background(
            MaterialTheme.colorScheme.background
        ),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_agri_hero),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(8.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = if (MaterialTheme.colorScheme.background.red < 0.3f) 0.76f else 0.40f))
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { AgriWeatherCard(modifier = Modifier.fillMaxWidth()) }
item {
                val diagnosisSelected = !isFlashcardMode
                val diagnosisWeight by animateFloatAsState(
                    targetValue = if (diagnosisSelected) 1.65f else 0.85f,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                    label = "diagnosis_tab_width"
                )
                val flashcardWeight by animateFloatAsState(
                    targetValue = if (isFlashcardMode) 1.65f else 0.85f,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                    label = "flashcard_tab_width"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("plant_doctor_mode_toggle"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(diagnosisWeight).height(70.dp).clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.setFlashcardMode(false) }
                            .testTag("tab_ai_diagnosis"),
                        color = if (diagnosisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, if (diagnosisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Biotech,
                                contentDescription = null,
                                tint = if (diagnosisSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp)
                            )
                            AnimatedVisibility(
                                visible = diagnosisSelected,
                                enter = fadeIn(spring()) + expandHorizontally(spring()),
                                exit = fadeOut(spring()) + shrinkHorizontally(spring())
                            ) {
                                Text(
                                    text = agroText("التشخيص الذكي"),
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(flashcardWeight).height(70.dp).clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.setFlashcardMode(true) }
                            .testTag("tab_student_flashcards"),
                        color = if (isFlashcardMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, if (isFlashcardMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = if (isFlashcardMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp)
                            )
                            AnimatedVisibility(
                                visible = isFlashcardMode,
                                enter = fadeIn(spring()) + expandHorizontally(spring()),
                                exit = fadeOut(spring()) + shrinkHorizontally(spring())
                            ) {
                                Text(
                                    text = agroText("بطاقات المراجعة"),
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (!isFlashcardMode) {
                item {
                    PlantCaptureCard(
                        selectedImages = selectedImages,
                        farmerNotes = farmerNotes,
                        isLoading = diagnosisState is PlantDoctorUiState.Loading,
                        cameraPermissionStatus = cameraPermissionState.status,
                        hasRequestedCameraPermission = hasRequestedCameraPermission,
                        onRequestCameraPermission = {
                            hasRequestedCameraPermission = true
                            cameraPermissionState.launchPermissionRequest()
                        },
                        onOpenAppSettings = openAppSettings,
                        onNotesChange = viewModel::updateFarmerNotes,
                        onRemoveImage = viewModel::removeImage,
                        onPickFromGallery = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onCaptureCamera = {
                            when {
                                cameraPermissionState.status.isGranted -> launchCameraCapture()
                                cameraPermissionState.status.shouldShowRationale -> {
                                    showCameraRationaleDialog = true
                                }
                                hasRequestedCameraPermission -> {
                                    showCameraSettingsDialog = true
                                }
                                else -> {
                                    hasRequestedCameraPermission = true
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        },
                        onAnalyzeClick = { viewModel.analyzePlantImage(context) },
                        onOpenLiveAssistant = onOpenLiveAssistant,
                        recentDiagnosisCount = recentDiagnoses.size,
                        onToggleDiagnosisHistory = { showDiagnosisHistory = !showDiagnosisHistory }
                    )
                }
                item {
                    AnimatedContent(
                        targetState = diagnosisState,
                        label = "diagnosis_state_anim"
                    ) { state ->
                        when (state) {
                            PlantDoctorUiState.Idle -> {
                                Text(
                                    text = agroText("أضف صورة واضحة للنبات للحصول على تشخيص وتوصيات"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            PlantDoctorUiState.Loading -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = agroText("جاري فحص الصور وتحديد المسبب المرضي..."),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = agroText("يتم تحليل الأعراض عبر الذكاء الاصطناعي وتقدير نسبة الثقة والمادة الفعالة."),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            is PlantDoctorUiState.Error -> {
                                val semantic = AgriTheme.semantic
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = semantic.dangerContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = semantic.danger
                                            )
                                            Text(
                                                text = agroText("تعذر إتمام التشخيص"),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = semantic.danger
                                            )
                                        }
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = semantic.onDangerContainer
                                        )
                                        OutlinedButton(
                                            onClick = { viewModel.analyzePlantImage(context) },
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("retry_diagnosis_button")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(agroText("إعادة المحاولة"))
                                        }
                                    }
                                }
                            }

                            is PlantDoctorUiState.Success -> {
                                DiagnosisResultCard(
                                    result = state.result,
                                    onShareReport = { shareDiagnosisReport(context, state.result) },
                                    onOpenInNpk = {
                                        onOpenInNpkCalculator(state.result.cropNameAr, state.result.diseaseNameAr)
                                    },
                                    onAddToNotebook = {
                                        viewModel.createTreatmentPlanInNotebook(state.result) {
                                            onNavigateToNotebook()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (recentDiagnoses.isNotEmpty()) {
                    item {
                        AnimatedVisibility(visible = showDiagnosisHistory) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                recentDiagnoses.forEach { record ->
                                    DiagnosisHistoryRow(
                                        record = record,
                                        onDelete = { viewModel.deleteDiagnosisRecord(record.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Student Revision Flashcards Mode
                item {
                    FlashcardProgressBanner(
                        masteredCount = masteredCardIds.size,
                        totalCount = FlashcardCatalog.cards.size
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FlashcardCatalog.crops) { crop ->
                            FilterChip(
                                selected = selectedCropFilter == crop,
                                onClick = { viewModel.selectCropFilter(crop) },
                                label = { Text(crop) },
                                modifier = Modifier.testTag("flashcard_filter_$crop")
                            )
                        }
                    }
                }

                items(filteredFlashcards, key = { it.id }) { card ->
                    DiseaseFlashcardItem(
                        card = card,
                        isFlipped = card.id in flippedCardIds,
                        isMastered = card.id in masteredCardIds,
                        onToggleFlip = { viewModel.toggleCardFlip(card.id) },
                        onToggleMastered = { viewModel.toggleCardMastered(card.id) }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun DashboardWelcomeHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            agroText("افحص نباتك"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            agroText("بالذكاء الاصطناعي"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC8F34A)
        )
    }
}

private fun shareDiagnosisReport(context: Context, result: PlantDiagnosisResult) {
    val recsText = if (result.recommendations.isNotEmpty()) {
        result.recommendations.mapIndexed { i, r -> "${i + 1}. $r" }.joinToString("\n")
    } else {
        "يرجى مراجعة مهندس وقاية النبات."
    }

    val report = buildString {
        appendLine("🌿 *تقرير تشخيص نباتي - تطبيق Agro pro*")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("🌱 *المحصول:* ${result.cropNameAr}")
        appendLine("🔬 *التشخيص:* ${result.diseaseNameAr} (${result.scientificName})")
        appendLine("🦠 *المسبب:* ${result.cause}")
        appendLine("⚠️ *درجة الخطورة:* ${result.severity}")
        appendLine("📊 *نسبة الثقة:* ${result.confidencePercent}%")
        appendLine("💊 *المادة الفعالة الموصى بها:* ${result.activeIngredient}")
        appendLine("⏳ *فترة التحريم قبل الجني (PHI):* ${result.phiDays}")
        if (result.summary.isNotBlank()) {
            appendLine("\n📝 *وصف الأعراض:*")
            appendLine(result.summary)
        }
        appendLine("\n✅ *بروتوكول التدخل والوقاية:*")
        appendLine(recsText)
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        append("تم الإنشاء بواسطة Agro pro")
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "تقرير تشخيص: ${result.diseaseNameAr}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة تقرير التشخيص عبر"))
}

@Composable
private fun AgriHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(26.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_agri_hero),
            contentDescription = agroText("منظر زراعي متوسطي لحقول الزيتون والطماطم والقمح"),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xF20B2413),
                            Color(0xCC0B3D20),
                            Color(0x6616A34A)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 17.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                color = Color(0xFF9BCB78).copy(alpha = 0.18f),
                shape = RoundedCornerShape(17.dp),
                border = BorderStroke(1.dp, Color(0xFF9BCB78).copy(alpha = 0.52f))
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Color(0xFF9BCB78),
                    modifier = Modifier.padding(12.dp).size(27.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AGROPRO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF9BCB78)
                )
                Text(
                    text = agroText("اعتنِ بمحصولك بذكاء أكبر"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2
                )
                Text(
                    text = agroText("مساعدك الزراعي الذكي"),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PlantCaptureCard(
    selectedImages: List<SelectedPlantImage>,
    farmerNotes: String,
    isLoading: Boolean,
    cameraPermissionStatus: PermissionStatus,
    hasRequestedCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onNotesChange: (String) -> Unit,
    onRemoveImage: (Long) -> Unit,
    onPickFromGallery: () -> Unit,
    onCaptureCamera: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onOpenLiveAssistant: () -> Unit,
    recentDiagnosisCount: Int,
    onToggleDiagnosisHistory: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth().testTag("plant_capture_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFC8F34A),
                    modifier = Modifier.size(27.dp)
                )
                Text(
                    text = agroText("مساعد AgroPro الذكي"),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        agroText("متصل"),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }

            Text(
                text = agroText("اسأل عن نباتاتك، الأمراض، الري أو التسميد."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = farmerNotes,
                        onValueChange = onNotesChange,
                        placeholder = {
                            Text(
                                agroText("اكتب سؤالك أو ملاحظتك عن النبات هنا..."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagnosis_notes_input"),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (selectedImages.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedImages, key = { it.id }) { item ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 76.dp, height = 76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        bitmap = item.bitmap.asImageBitmap(),
                                        contentDescription = item.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.72f),
                                        shape = CircleShape,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(3.dp)
                                            .size(23.dp).clickable { onRemoveImage(item.id) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Close, null, tint = Color.White,
                                                modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAnalyzeClick,
                            enabled = !isLoading && selectedImages.isNotEmpty(),
                            modifier = Modifier.size(46.dp).testTag("analyze_plant_button"),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC8F34A),
                                contentColor = Color(0xFF172313)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(19.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF172313)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = agroText("إرسال"),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = onOpenLiveAssistant,
                            modifier = Modifier.height(42.dp).testTag("open_agro_live_button"),
                            shape = CircleShape,
                            color = Color(0xFF185A3A)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text("Live", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.GraphicEq, null, tint = Color(0xFFBDECCB),
                                    modifier = Modifier.size(17.dp))
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            IconButton(
                                onClick = onCaptureCamera,
                                enabled = !isLoading && selectedImages.size < 3,
                                modifier = Modifier.size(40.dp).testTag("camera_capture_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = agroText("التقط صورة"),
                                    tint = Color(0xFFB9CDBD), modifier = Modifier.size(22.dp))
                            }
                            IconButton(
                                onClick = onPickFromGallery,
                                enabled = !isLoading && selectedImages.size < 3,
                                modifier = Modifier.size(40.dp).testTag("gallery_pick_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = agroText("اختيار من المعرض"),
                                    tint = Color(0xFFB9CDBD), modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !cameraPermissionStatus.isGranted &&
                    (cameraPermissionStatus.shouldShowRationale || hasRequestedCameraPermission)
            ) {
                val showRationale = cameraPermissionStatus.shouldShowRationale
                val semantic = AgriTheme.semantic
                Surface(
                    color = if (showRationale) semantic.warningContainer else semantic.dangerContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().testTag("camera_permission_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null,
                            tint = if (showRationale) semantic.warning else semantic.danger)
                        Text(
                            text = if (showRationale) stringResource(R.string.camera_permission_rationale_body)
                            else stringResource(R.string.camera_permission_denied_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (showRationale) semantic.onWarningContainer else semantic.onDangerContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (showRationale) onRequestCameraPermission() else onOpenAppSettings()
                            },
                            modifier = Modifier.testTag(
                                if (showRationale) "camera_permission_rationale_button"
                                else "camera_permission_settings_button"
                            )
                        ) {
                            Text(
                                if (showRationale) stringResource(R.string.camera_permission_grant_action)
                                else stringResource(R.string.camera_permission_open_settings),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (recentDiagnosisCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onToggleDiagnosisHistory,
                        modifier = Modifier.size(34.dp).testTag("toggle_diagnosis_history")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = agroText("سجل التشخيصات"),
                            tint = Color(0xFFB9CDBD),
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosisResultCard(
    result: PlantDiagnosisResult,
    onShareReport: () -> Unit,
    onOpenInNpk: () -> Unit,
    onAddToNotebook: () -> Unit
) {
    var showDetails by remember(result.diseaseNameAr) { mutableStateOf(false) }
    val semantic = AgriTheme.semantic
    val severityColors = when (result.severity) {
        "حرجة", "مرتفعة" -> semantic.danger to semantic.dangerContainer
        "متوسطة" -> semantic.warning to semantic.warningContainer
        else -> semantic.safe to semantic.safeContainer
    }
    val severityFraction = when (result.severity) {
        "حرجة" -> 0.95f
        "مرتفعة" -> 0.78f
        "متوسطة" -> 0.52f
        else -> 0.25f
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diagnosis_result_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("diagnosed_crop_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = result.cropNameAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("diagnosed_pathogen_chip")
                    ) {
                        Text(
                            text = result.cause,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // نسبة الثقة
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = agroText("ثقة ${result.confidencePercent}%"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // تنبيه وضوح الصورة وطلب زاوية إضافية عند الحاجة
            if (result.clarificationNeeded.isNotBlank()) {
                Surface(
                    color = semantic.warningContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = semantic.warning
                        )
                        Text(
                            text = agroText("لرفع دقة التشخيص: ${result.clarificationNeeded}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.onWarningContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = result.diseaseNameAr,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = result.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showDetails = !showDetails },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (showDetails) "إخفاء التوصيات والتفاصيل" else "حلّل واحصل على التوصيات",
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = showDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Severity indicator
            Surface(
                color = severityColors.second,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = agroText("درجة الخطورة الوبائية"),
                            style = MaterialTheme.typography.labelMedium,
                            color = severityColors.first
                        )
                        Text(
                            text = result.severity,
                            style = MaterialTheme.typography.labelLarge,
                            color = severityColors.first
                        )
                    }
                    LinearProgressIndicator(
                        progress = { severityFraction },
                        color = severityColors.first,
                        trackColor = severityColors.first.copy(alpha = 0.22f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }

            if (result.summary.isNotBlank()) {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoMetricTile(
                    icon = Icons.Default.Medication,
                    label = "المادة الفعالة الموصى بها",
                    value = result.activeIngredient,
                    modifier = Modifier.weight(1f)
                )
                InfoMetricTile(
                    icon = Icons.Default.Schedule,
                    label = "فترة التحريم (PHI)",
                    value = result.phiDays,
                    modifier = Modifier.weight(1f)
                )
            }

            ExpandableSuggestions(
                items = result.recommendations,
                title = "بروتوكول التدخل والوقاية"
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // أزرار الربط الذكي مع التجارب وحاسبة NPK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddToNotebook,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("add_treatment_to_notebook_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = agroText("خطة علاج (PHI)"),
                        maxLines = 2,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onOpenInNpk,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("open_diagnosis_in_npk_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = agroText("حاسبة NPK"),
                        maxLines = 2,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // زر مشاركة التقرير
            OutlinedButton(
                onClick = onShareReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_diagnosis_report_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = agroText("مشاركة تقرير التشخيص (واتساب / نص)"),
                    fontWeight = FontWeight.Bold
                )
            }
                }
            }
        }
    }
}

@Composable
private fun InfoMetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DiagnosisHistoryRow(
    record: DiagnosisHistoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.diagnosedAt) {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(record.diagnosedAt))
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val savedPath = record.photoPath
            if (!savedPath.isNullOrBlank() && java.io.File(savedPath).exists()) {
                Image(
                    painter = coil.compose.rememberAsyncImagePainter(model = java.io.File(savedPath)),
                    contentDescription = agroText("صورة التشخيص المحفوظة: ${record.diseaseNameAr}"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                val cropPrefix = if (record.cropNameAr.isNotBlank()) "${record.cropNameAr}: " else ""
                Text(
                    text = "$cropPrefix${record.diseaseNameAr}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${record.scientificName} • ${record.cause} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = agroText("المادة الفعالة: ${record.activeIngredient} (تحريم: ${record.phiDays})"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = agroText("حذف التشخيص"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FlashcardProgressBanner(
    masteredCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) masteredCount.toFloat() / totalCount.toFloat() else 0f
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = agroText("وضع مراجعة طلبة العلوم الزراعية"),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = agroText("$masteredCount / $totalCount متقنة"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun DiseaseFlashcardItem(
    card: DiseaseFlashcard,
    isFlipped: Boolean,
    isMastered: Boolean,
    onToggleFlip: () -> Unit,
    onToggleMastered: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isMastered) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleFlip)
            .testTag("flashcard_${card.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = card.cropAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = card.pathogenType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleMastered,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("master_card_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = if (isMastered) "إلغاء علامة الإتقان" else "تحديد كبطاقة متقنة",
                        tint = if (isMastered) AgriTheme.semantic.safe else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            if (card.drawableRes != null) {
                Image(
                    painter = painterResource(id = card.drawableRes),
                    contentDescription = agroText("رسم علمي تشخيصي لمحصول ${card.cropAr}"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = agroText("بطاقة تصنيف مخبرية - ${card.cropAr}"),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = agroText("الخطورة الشائعة: ${card.severityTypical}"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = agroText("الأعراض الحقلية المميزة:"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = card.keySymptoms,
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(
                visible = isFlipped,
                enter = androidx.compose.animation.fadeIn(spring()) +
                    androidx.compose.animation.expandVertically(spring())
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = card.diseaseNameAr,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = card.scientificName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = agroText("المؤشر التشخيصي: ${card.diagnosticClue}"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = agroText("المادة الفعالة: ${card.activeIngredient}"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = agroText("فترة التحريم (PHI): ${card.phiDays}"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onToggleFlip,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFlipped) Icons.Default.Visibility else Icons.Default.Flip,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isFlipped) "إخفاء الإجابة العلمية" else "اكشف التصنيف العلمي")
                }
            }
        }
    }
}
