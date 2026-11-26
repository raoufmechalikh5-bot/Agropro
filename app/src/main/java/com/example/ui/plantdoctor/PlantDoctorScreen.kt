package com.example.ui.plantdoctor

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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
    onNavigateToFertilizer: () -> Unit = {},
    onNavigateToEncyclopedia: () -> Unit = {},
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
           
            item {
                DashboardWelcomeHeader()
            }
            item {
                AgriHeroBanner()
            }
            item {
                AgriWeatherCard()
            }
            item {
                DashboardShortcuts(
                    onOpenFertilizer = onNavigateToFertilizer,
                    onOpenEncyclopedia = onNavigateToEncyclopedia,
                    onOpenNotebook = onNavigateToNotebook
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("plant_doctor_mode_toggle"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val diagnosisSelected = !isFlashcardMode
                    Surface(
                        modifier = Modifier.weight(1f).height(76.dp).clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.setFlashcardMode(false) }
                            .testTag("tab_ai_diagnosis"),
                        color = if (diagnosisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, if (diagnosisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Icon(Icons.Default.Biotech, null,
                                tint = if (diagnosisSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("التشخيص الذكي", fontWeight = FontWeight.Bold,
                                    color = if (diagnosisSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall)
                                Text("افحص صحة نباتك", color = if (diagnosisSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f).height(76.dp).clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.setFlashcardMode(true) }
                            .testTag("tab_student_flashcards"),
                        color = if (isFlashcardMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, if (isFlashcardMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Icon(Icons.Default.School, null,
                                tint = if (isFlashcardMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("بطاقات المراجعة", fontWeight = FontWeight.Bold,
                                    color = if (isFlashcardMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall)
                                Text("تعلّم أمراض المحاصيل", color = if (isFlashcardMode) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall)
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
                        onSelectSample = { resId, label ->
                            viewModel.loadSampleImage(context, resId, label)
                        },
                        onAnalyzeClick = { viewModel.analyzePlantImage(context) }
                    )
                }

                item {
                    AnimatedContent(
                        targetState = diagnosisState,
                        label = "diagnosis_state_anim"
                    ) { state ->
                        when (state) {
                            PlantDoctorUiState.Idle -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(22.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(15.dp),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.AutoAwesome, null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(23.dp))
                                            }
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("كيف تحصل على تشخيص أدق؟",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold)
                                            Text("صوّر الورقة المصابة بوضوح، ثم أضف صورة للساق أو النبات كاملاً. يمكنك إرفاق 3 صور.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
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
                                            text = "جاري فحص الصور وتحديد المسبب المرضي...",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "يتم تحليل الأعراض عبر الذكاء الاصطناعي وتقدير نسبة الثقة والمادة الفعالة.",
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
                                                text = "تعذر إتمام التشخيص",
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
                                            Text("إعادة المحاولة")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "سجل التشخيصات المحفوظة (متاح دون إنترنت)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    items(recentDiagnoses, key = { it.id }) { record ->
                        DiagnosisHistoryRow(
                            record = record,
                            onDelete = { viewModel.deleteDiagnosisRecord(record.id) }
                        )
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

@Composable
private fun DashboardWelcomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "مساحتك الزراعية الذكية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "تابع محاصيلك واتخذ قرارات أفضل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(17.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardShortcuts(
    onOpenFertilizer: () -> Unit,
    onOpenEncyclopedia: () -> Unit,
    onOpenNotebook: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "أدواتك الزراعية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "وصول سريع",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                DashboardShortcutCard(
                    title = "حاسبة NPK",
                    subtitle = "احسب احتياجات التسميد",
                    icon = Icons.Default.Calculate,
                    onClick = onOpenFertilizer
                )
            }
            item {
                DashboardShortcutCard(
                    title = "الموسوعة",
                    subtitle = "تعرف على أمراض النبات",
                    icon = Icons.Default.MenuBook,
                    onClick = onOpenEncyclopedia
                )
            }
            item {
                DashboardShortcutCard(
                    title = "دفتر المزرعة",
                    subtitle = "ملاحظاتك وخططك",
                    icon = Icons.Default.History,
                    onClick = onOpenNotebook
                )
            }
        }
    }
}

@Composable
private fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(188.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
            .height(194.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_agri_hero),
            contentDescription = "منظر زراعي متوسطي لحقول الزيتون والطماطم والقمح",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x9908120C),
                            0.42f to Color(0x5508120C),
                            1.0f to Color(0xF208120C)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Surface(
                    color = Color(0xFFB7E4C7).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color(0xFFB7E4C7),
                        modifier = Modifier.padding(9.dp).size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "AGROPRO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "SMART AGRICULTURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    color = Color(0xFFB7E4C7).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(0xFFB7E4C7).copy(alpha = 0.38f))
                ) {
                    Text(
                        text = "مساعدك الزراعي",
                        color = Color(0xFFD8F3DC),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "اعتنِ بمحصولك،\nبذكاء أكبر",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "تشخيص أمراض النباتات ومتابعة الحقول في مكان واحد",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.86f)
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color(0xFFB7E4C7),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "التشخيص الذكي",
                    color = Color(0xFF0B2B13),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
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
    onSelectSample: (Int, String) -> Unit,
    onAnalyzeClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        "مساعد التشخيص الذكي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "دع الذكاء الاصطناعي يفحص حالة نباتك",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${selectedImages.size}/3",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    )
                }
            }

            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImages, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(160.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Image(
                                bitmap = item.bitmap.asImageBitmap(),
                                contentDescription = item.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(30.dp)
                                    .clickable { onRemoveImage(item.id) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إزالة الصورة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                color = Color.Black.copy(alpha = 0.68f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (selectedImages.size < 3) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = MaterialTheme.shapes.medium,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(160.dp)
                                    .clickable(onClick = onCaptureCamera)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "إضافة زاوية أخرى",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(enabled = !isLoading, onClick = onPickFromGallery),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "أضف صورة النبات هنا",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "اضغط لاختيار صورة من معرض الهاتف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    "اختيار صورة",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCaptureCamera,
                    enabled = selectedImages.size < 3,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("camera_capture_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("الكاميرا", maxLines = 1)
                }

                OutlinedButton(
                    onClick = onPickFromGallery,
                    enabled = selectedImages.size < 3,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gallery_pick_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("المعرض", maxLines = 1)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_permission_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (showRationale) semantic.warning else semantic.danger
                        )
                        Text(
                            text = if (showRationale) {
                                stringResource(R.string.camera_permission_rationale_body)
                            } else {
                                stringResource(R.string.camera_permission_denied_body)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (showRationale) semantic.onWarningContainer else semantic.onDangerContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (showRationale) {
                                    onRequestCameraPermission()
                                } else {
                                    onOpenAppSettings()
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag(
                                    if (showRationale) {
                                        "camera_permission_rationale_button"
                                    } else {
                                        "camera_permission_settings_button"
                                    }
                                )
                        ) {
                            Text(
                                text = if (showRationale) {
                                    stringResource(R.string.camera_permission_grant_action)
                                } else {
                                    stringResource(R.string.camera_permission_open_settings)
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (showRationale) semantic.warning else semantic.danger
                            )
                        }
                    }
                }
            }

            Text(
                text = "عينات جاهزة للتجربة:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(
                        onClick = { onSelectSample(R.drawable.img_crop_olive, "عينة أوراق زيتون") },
                        label = { Text("ورقة زيتون") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                        modifier = Modifier.testTag("sample_olive_chip")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onSelectSample(R.drawable.img_crop_tomato, "عينة طماطم مصابة") },
                        label = { Text("أوراق طماطم") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                        modifier = Modifier.testTag("sample_tomato_chip")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onSelectSample(R.drawable.img_crop_citrus, "عينة حمضيات") },
                        label = { Text("غصن حمضيات") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                        modifier = Modifier.testTag("sample_citrus_chip")
                    )
                }
            }

            OutlinedTextField(
                value = farmerNotes,
                onValueChange = onNotesChange,
                label = { Text("ملاحظات حقلية اختيارية (نوع المحصول، الرطوبة، العمر)") },
                placeholder = { Text("مثال: طماطم محمية، رطوبة صباحية مرتفعة...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diagnosis_notes_input"),
                maxLines = 2
            )

            Button(
                onClick = onAnalyzeClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("analyze_plant_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .testTag("analyze_button_spinner"),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("جاري التحليل...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("تحليل وتشخيص الحالة (${selectedImages.size})")
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                            text = "ثقة ${result.confidencePercent}%",
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
                            text = "لرفع دقة التشخيص: ${result.clarificationNeeded}",
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
                            text = "درجة الخطورة الوبائية",
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

            if (result.recommendations.isNotEmpty()) {
                Text(
                    text = "بروتوكول التدخل والوقاية:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                result.recommendations.forEachIndexed { idx, rec ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

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
                        .height(48.dp)
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
                        text = "خطة علاج (PHI)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onOpenInNpk,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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
                        text = "حاسبة NPK",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                    text = "مشاركة تقرير التشخيص (واتساب / نص)",
                    fontWeight = FontWeight.Bold
                )
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
                    contentDescription = "صورة التشخيص المحفوظة: ${record.diseaseNameAr}",
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
                    text = "المادة الفعالة: ${record.activeIngredient} (تحريم: ${record.phiDays})",
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
                    contentDescription = "حذف التشخيص",
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
                        text = "وضع مراجعة طلبة العلوم الزراعية",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = "$masteredCount / $totalCount متقنة",
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
                    contentDescription = "رسم علمي تشخيصي لمحصول ${card.cropAr}",
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
                                text = "بطاقة تصنيف مخبرية - ${card.cropAr}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "الخطورة الشائعة: ${card.severityTypical}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = "الأعراض الحقلية المميزة:",
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
                            text = "المؤشر التشخيصي: ${card.diagnosticClue}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "المادة الفعالة: ${card.activeIngredient}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "فترة التحريم (PHI): ${card.phiDays}",
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
