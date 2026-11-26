package com.example.ui.notebook

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.local.ExperimentEntity
import com.example.data.local.MeasurementEntity
import com.example.ui.theme.AgriTheme
import com.example.util.CsvExporter
import com.example.util.PhotoStorageHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import com.example.ui.weather.WeatherContextBanner
import com.example.ui.weather.WeatherContextMode


@Composable
fun NotebookScreen(
    viewModel: NotebookViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val experiments by viewModel.experiments.collectAsStateWithLifecycle()
    val allMeasurements by viewModel.allMeasurements.collectAsStateWithLifecycle()
    val selectedExperiment by viewModel.selectedExperiment.collectAsStateWithLifecycle()
    val selectedMeasurements by viewModel.selectedMeasurements.collectAsStateWithLifecycle()

    var showCreateExperimentDialog by remember { mutableStateOf(false) }
    var showAddMeasurementDialog by remember { mutableStateOf(false) }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val exp = selectedExperiment
        if (uri != null && exp != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val csv = CsvExporter.buildExperimentCsv(exp, selectedMeasurements)
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            CsvExporter.writeCsvToStream(out, csv)
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
                Toast.makeText(
                    context,
                    if (ok) "تم تصدير جدول القياسات بنجاح (CSV/Excel)" else "تعذر حفظ الملف.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    if (selectedExperiment != null) {
        BackHandler {
            viewModel.selectExperiment(null)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val currentExp = selectedExperiment
        if (currentExp == null) {
            // Experiments List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                        item {
                WeatherContextBanner(mode = WeatherContextMode.NOTEBOOK)
            }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "مفكرة التجارب الميدانية والمخبرية",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "سجّل القياسات الدورية، الرسوم البيانية، وصدّر الجداول بصيغة CSV/Excel دون الحاجة للإنترنت",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (experiments.isEmpty()) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "لا توجد تجارب مسجلة بعد",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "اضغط على زر «تجربة جديدة» لبدء تتبع تجارب الإنبات أو التسميد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(experiments, key = { it.id }) { exp ->
                        val count = allMeasurements.count { it.experimentId == exp.id }
                        ExperimentSummaryCard(
                            experiment = exp,
                            measurementCount = count,
                            onClick = { viewModel.selectExperiment(exp.id) },
                            onDelete = { viewModel.deleteExperiment(exp.id) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showCreateExperimentDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_new_experiment")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("تجربة جديدة")
                }
            }
        } else {
            // Experiment Detail View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.selectExperiment(null) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("back_to_experiments_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("كل التجارب")
                        }

                        FilterChip(
                            selected = currentExp.status == "مكتملة",
                            onClick = { viewModel.toggleExperimentStatus(currentExp) },
                            label = { Text(currentExp.status) },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            },
                            modifier = Modifier.testTag("toggle_exp_status_chip")
                        )
                    }
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentExp.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentExp.cropName} • ${currentExp.experimentType}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "الموقع: ${currentExp.locationOrLab}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentExp.hypothesis.isNotBlank()) {
                                Text(
                                    text = "الفرضية: ${currentExp.hypothesis}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val fileName = "AgriPro_Exp_${currentExp.id}.csv"
                                        saveCsvLauncher.launch(fileName)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("export_csv_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("تصدير CSV / Excel", maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val file = withContext(Dispatchers.IO) {
                                                CsvExporter.writeTempCsvForShare(
                                                    context,
                                                    currentExp,
                                                    selectedMeasurements
                                                )
                                            }
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                putExtra(Intent.EXTRA_SUBJECT, currentExp.title)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "مشاركة جدول التجربة")
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("share_csv_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة CSV")
                                    Spacer(Modifier.width(6.dp))
                                    Text("مشاركة")
                                }
                            }
                        }
                    }
                }

                // Growth & Moisture Responsive Chart
                item {
                    ExperimentGrowthChartCard(measurements = selectedMeasurements)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل القياسات الميدانية (${selectedMeasurements.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                items(selectedMeasurements, key = { it.id }) { m ->
                    MeasurementRowCard(
                        measurement = m,
                        onDelete = { viewModel.deleteMeasurement(m.id) }
                    )
                }
            }

            FloatingActionButton(
                onClick = { showAddMeasurementDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_measurement")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("إضافة قياس")
                }
            }
        }
    }

    if (showCreateExperimentDialog) {
        CreateExperimentDialog(
            onDismiss = { showCreateExperimentDialog = false },
            onConfirm = { title, crop, type, loc, hypothesis ->
                viewModel.createExperiment(title, crop, type, loc, hypothesis)
                showCreateExperimentDialog = false
            }
        )
    }

    val activeExp = selectedExperiment
    if (showAddMeasurementDialog && activeExp != null) {
        val suggestedDay = (selectedMeasurements.maxOfOrNull { it.dayNumber } ?: 0) + 7
        AddMeasurementDialog(
            suggestedDay = suggestedDay,
            onDismiss = { showAddMeasurementDialog = false },
            onConfirm = { day, height, leaves, moisture, notes, photoUri ->
                scope.launch {
                    val savedPath = if (photoUri != null) {
                        withContext(Dispatchers.IO) {
                            PhotoStorageHelper.copyUriToInternalFiles(context, photoUri)
                        }
                    } else null
                    viewModel.addMeasurement(
                        experimentId = activeExp.id,
                        dayNumber = day,
                        plantHeightCm = height,
                        leafCount = leaves,
                        soilMoisturePercent = moisture,
                        notes = notes,
                        photoPath = savedPath
                    )
                    showAddMeasurementDialog = false
                }
            }
        )
    }
}

@Composable
private fun ExperimentSummaryCard(
    experiment: ExperimentEntity,
    measurementCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(experiment.startDateMillis) {
        SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(experiment.startDateMillis))
    }
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("experiment_card_${experiment.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = experiment.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${experiment.cropName} • ${experiment.experimentType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف التجربة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val semantic = AgriTheme.semantic
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (experiment.status == "مكتملة") semantic.safeContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = experiment.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (experiment.status == "مكتملة") semantic.onSafeContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "$measurementCount قياسات مسجلة • بدء $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExperimentGrowthChartCard(measurements: List<MeasurementEntity>) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "منحنى تطور النمو والرطوبة",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (measurements.size < 2) {
                Text(
                    text = "أضف قياسين على الأقل لعرض الرسم البياني لتطور الارتفاع ورطوبة التربة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxHeight = max(10.0, measurements.maxOf { it.plantHeightCm })
                val gridColor = MaterialTheme.colorScheme.outlineVariant
                val semantic = AgriTheme.semantic
                val heightLineColor = semantic.safe
                val moistureLineColor = semantic.water

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● ارتفاع النبات (سم)",
                        style = MaterialTheme.typography.labelSmall,
                        color = heightLineColor
                    )
                    Text(
                        text = "● رطوبة التربة (%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = moistureLineColor
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 6.dp)
                ) {
                    val padH = 14.dp.toPx()
                    val padV = 12.dp.toPx()
                    val chartW = size.width - padH * 2
                    val chartH = size.height - padV * 2

                    // Horizontal grid lines
                    for (i in 0..4) {
                        val y = padV + (chartH / 4f) * i
                        drawLine(
                            color = gridColor.copy(alpha = 0.45f),
                            start = Offset(padH, y),
                            end = Offset(padH + chartW, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val heightPath = Path()
                    val moisturePath = Path()

                    measurements.forEachIndexed { idx, m ->
                        val x = padH + (idx.toFloat() / (measurements.size - 1)) * chartW
                        val hNorm = (m.plantHeightCm / maxHeight).toFloat().coerceIn(0f, 1f)
                        val mNorm = (m.soilMoisturePercent / 100.0).toFloat().coerceIn(0f, 1f)

                        val hy = padV + chartH * (1f - hNorm)
                        val my = padV + chartH * (1f - mNorm)

                        if (idx == 0) {
                            heightPath.moveTo(x, hy)
                            moisturePath.moveTo(x, my)
                        } else {
                            heightPath.lineTo(x, hy)
                            moisturePath.lineTo(x, my)
                        }
                    }

                    drawPath(
                        path = moisturePath,
                        color = moistureLineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = heightPath,
                        color = heightLineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    measurements.forEachIndexed { idx, m ->
                        val x = padH + (idx.toFloat() / (measurements.size - 1)) * chartW
                        val hNorm = (m.plantHeightCm / maxHeight).toFloat().coerceIn(0f, 1f)
                        val mNorm = (m.soilMoisturePercent / 100.0).toFloat().coerceIn(0f, 1f)
                        drawCircle(
                            color = heightLineColor,
                            radius = 4.5.dp.toPx(),
                            center = Offset(x, padV + chartH * (1f - hNorm))
                        )
                        drawCircle(
                            color = moistureLineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, padV + chartH * (1f - mNorm))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "اليوم ${measurements.first().dayNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "أقصى ارتفاع: ${String.format(Locale.US, "%.1f", maxHeight)} سم",
                        style = MaterialTheme.typography.labelSmall,
                        color = heightLineColor
                    )
                    Text(
                        text = "اليوم ${measurements.last().dayNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasurementRowCard(
    measurement: MeasurementEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(measurement.recordedDateMillis) {
        SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(measurement.recordedDateMillis))
    }

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "اليوم ${measurement.dayNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف القياس",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "الارتفاع",
                    value = "${measurement.plantHeightCm} سم",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "الأوراق",
                    value = "${measurement.leafCount}",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "الرطوبة",
                    value = "${measurement.soilMoisturePercent}%",
                    modifier = Modifier.weight(1f)
                )
            }

            if (measurement.notes.isNotBlank()) {
                Text(
                    text = measurement.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val path = measurement.photoPath
            if (!path.isNullOrBlank() && File(path).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(path)),
                    contentDescription = "صورة مرفقة للقياس في اليوم ${measurement.dayNumber}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CreateExperimentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, crop: String, type: String, location: String, hypothesis: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("البطاطا") }
    var type by remember { mutableStateOf("تجربة تسميد ميدانية") }
    var location by remember { mutableStateOf("الحقل التجريبي - قطاع 1") }
    var hypothesis by remember { mutableStateOf("") }

    val types = listOf("تجربة تسميد ميدانية", "تجربة مخبرية (إنبات)", "مقارنة أصناف", "مكافحة حيوية")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء تجربة زراعية جديدة") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التجربة *") },
                    placeholder = { Text("مثال: تأثير هيومات البوتاسيوم على البطاطا") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exp_title_input")
                )
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("المحصول المدروس") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("نوع التجربة:", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) }
                        )
                    }
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("الموقع أو المخبر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hypothesis,
                    onValueChange = { hypothesis = it },
                    label = { Text("الفرضية العلمية") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, crop, type, location, hypothesis) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_exp_button")
            ) {
                Text("حفظ التجربة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AddMeasurementDialog(
    suggestedDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, height: Double, leaves: Int, moisture: Double, notes: String, photoUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var dayText by remember { mutableStateOf(suggestedDay.toString()) }
    var heightText by remember { mutableStateOf("25.0") }
    var leavesText by remember { mutableStateOf("12") }
    var moistureText by remember { mutableStateOf("60.0") }
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var hasRequestedCameraPermission by rememberSaveable { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            selectedPhotoUri = uri
        }
    }

    val launchCameraCapture = remember(context, takePicture) {
        {
            val file = PhotoStorageHelper.createTempCameraFile(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        }
    }

    val semantic = AgriTheme.semantic

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة قياس دوري جديد") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it },
                        label = { Text("رقم اليوم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("الارتفاع (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_height_input")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = leavesText,
                        onValueChange = { leavesText = it },
                        label = { Text("عدد الأوراق") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("رطوبة التربة (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات ميدانية") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                launchCameraCapture()
                            } else {
                                hasRequestedCameraPermission = true
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("كاميرا", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(if (selectedPhotoUri == null) "من المعرض" else "مرفقة ✅", maxLines = 1)
                    }
                }
                if (!cameraPermissionState.status.isGranted &&
                    (cameraPermissionState.status.shouldShowRationale || hasRequestedCameraPermission)
                ) {
                    Surface(
                        color = if (cameraPermissionState.status.shouldShowRationale) {
                            semantic.warningContainer
                        } else {
                            semantic.dangerContainer
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.camera_permission_denied_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (cameraPermissionState.status.shouldShowRationale) {
                                    semantic.onWarningContainer
                                } else {
                                    semantic.onDangerContainer
                                }
                            )
                            TextButton(
                                onClick = {
                                    if (cameraPermissionState.status.shouldShowRationale) {
                                        cameraPermissionState.launchPermissionRequest()
                                    } else {
                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    text = if (cameraPermissionState.status.shouldShowRationale) {
                                        stringResource(R.string.camera_permission_grant_action)
                                    } else {
                                        stringResource(R.string.camera_permission_open_settings)
                                    }
                                )
                            }
                        }
                    }
                }
                if (selectedPhotoUri != null) {
                    Text(
                        text = "سيتم حفظ الصورة نهائياً في ذاكرة الجهاز مع القياس.",
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.safe
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        dayText.toIntOrNull() ?: suggestedDay,
                        heightText.toDoubleOrNull() ?: 0.0,
                        leavesText.toIntOrNull() ?: 0,
                        moistureText.toDoubleOrNull() ?: 50.0,
                        notes,
                        selectedPhotoUri
                    )
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("confirm_add_measurement_button")
            ) {
                Text("تسجيل القياس")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) { Text("إلغاء") }
        }
    )
}
