package com.example.ui.calculator

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.catalog.AgronomyCalculationResult
import com.example.data.catalog.AreaUnit
import com.example.data.catalog.ClimateZone
import com.example.data.catalog.CropProfile
import com.example.data.catalog.GrowthStage
import com.example.data.catalog.IrrigationMethod
import com.example.ui.theme.AgriTheme
import java.util.Locale
import kotlin.math.max
import com.example.ui.weather.WeatherContextBanner
import com.example.ui.weather.WeatherContextMode



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    onOpenNpkCalculator: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var cropExpanded by remember { mutableStateOf(false) }
    var stageExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                       item {
                WeatherContextBanner(mode = WeatherContextMode.IRRIGATION)
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "حاسبة التسميد والري الذكية",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "حساب دقيق لوحدات NPK وحجم مياه الري وفق تحليل التربة والمرحلة الفينولوجية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (onOpenNpkCalculator != null) {
                item {
                    FilledTonalButton(
                        onClick = onOpenNpkCalculator,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_fertilizer_npk_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("فتح حاسبة نسب NPK السريعة حسب المحصول والمساحة")
                    }
                }
            }

            // Soil presets for instant Mediterranean testing
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "نماذج تربة متوسطية جاهزة:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = {
                                    viewModel.applySoilPreset("22", "12", "130", 7.9f, "2.4", ClimateZone.SEMI_ARID)
                                },
                                label = { Text("تربة كلسية شبه جافة") },
                                modifier = Modifier.testTag("preset_calcareous_soil")
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    viewModel.applySoilPreset("40", "24", "185", 6.8f, "1.1", ClimateZone.COASTAL)
                                },
                                label = { Text("تربة ساحلية خصبة") },
                                modifier = Modifier.testTag("preset_coastal_soil")
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    viewModel.applySoilPreset("15", "10", "110", 8.2f, "5.2", ClimateZone.ARID_OASIS)
                                },
                                label = { Text("تربة واحات مالحة") },
                                modifier = Modifier.testTag("preset_oasis_soil")
                            )
                        }
                    }
                }
            }

            // Crop, Area, Stage Card
            item {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "بيانات المحصول والمساحة",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Crop Dropdown
                        ExposedDropdownMenuBox(
                            expanded = cropExpanded,
                            onExpandedChange = { cropExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCrop.titleAr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوع المحصول") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("crop_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = cropExpanded,
                                onDismissRequest = { cropExpanded = false }
                            ) {
                                CropProfile.entries.forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop.titleAr) },
                                        onClick = {
                                            viewModel.selectCrop(crop)
                                            cropExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Growth Stage Dropdown
                        ExposedDropdownMenuBox(
                            expanded = stageExpanded,
                            onExpandedChange = { stageExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.selectedStage.titleAr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("المرحلة الفينولوجية للنمو") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = stageExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("stage_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = stageExpanded,
                                onDismissRequest = { stageExpanded = false }
                            ) {
                                GrowthStage.entries.forEach { stage ->
                                    DropdownMenuItem(
                                        text = { Text(stage.titleAr) },
                                        onClick = {
                                            viewModel.selectStage(stage)
                                            stageExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.areaInput,
                                onValueChange = viewModel::updateAreaInput,
                                label = { Text("المساحة") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("area_input")
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                AreaUnit.entries.forEach { unit ->
                                    FilterChip(
                                        selected = state.areaUnit == unit,
                                        onClick = { viewModel.selectAreaUnit(unit) },
                                        label = { Text(unit.labelAr, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Soil Analysis Card
            item {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "معطيات تحليل التربة ونظام الري",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.nitrogenPpmText,
                                onValueChange = viewModel::updateNitrogen,
                                label = { Text("الآزوت N (ppm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("soil_n_input")
                            )
                            OutlinedTextField(
                                value = state.phosphorusPpmText,
                                onValueChange = viewModel::updatePhosphorus,
                                label = { Text("الفوسفور P (ppm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("soil_p_input")
                            )
                            OutlinedTextField(
                                value = state.potassiumPpmText,
                                onValueChange = viewModel::updatePotassium,
                                label = { Text("البوتاسيوم K (ppm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("soil_k_input")
                            )
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "درجة حموضة التربة (pH)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f", state.soilPh),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = state.soilPh,
                                onValueChange = viewModel::updatePh,
                                valueRange = 4.5f..9.0f,
                                modifier = Modifier.testTag("soil_ph_slider")
                            )
                        }

                        OutlinedTextField(
                            value = state.soilEcText,
                            onValueChange = viewModel::updateEc,
                            label = { Text("ملوحة التربة الكهربائية EC (dS/m)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("soil_ec_input")
                        )

                        Text(
                            text = "نظام الري والمناخ:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(IrrigationMethod.entries) { method ->
                                FilterChip(
                                    selected = state.irrigationMethod == method,
                                    onClick = { viewModel.selectIrrigation(method) },
                                    label = { Text(method.labelAr) }
                                )
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ClimateZone.entries) { zone ->
                                FilterChip(
                                    selected = state.climateZone == zone,
                                    onClick = { viewModel.selectClimate(zone) },
                                    label = { Text(zone.labelAr) }
                                )
                            }
                        }
                    }
                }
            }

            // Results Card
            item {
                CalculatorResultsCard(result = state.result)
            }
        }
    }
}

@Composable
private fun CalculatorResultsCard(result: AgronomyCalculationResult) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculator_results_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "المخرجات التسميدية والمائية المحسوبة",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            val semantic = AgriTheme.semantic
            // Irrigation Highlight Tile
            Surface(
                color = semantic.waterContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = semantic.water
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حجم مياه الري اليومي المطلوب (للمساحة الكلية ${formatNum(result.areaHectares)} هكتار)",
                            style = MaterialTheme.typography.labelMedium,
                            color = semantic.water
                        )
                        Text(
                            text = "${formatNum(result.dailyWaterM3)} م³/يوم  (${formatNum(result.dailyWaterLiters)} لتر/يوم)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = semantic.onWaterContainer
                        )
                        if (result.leachingFractionPercent > 0) {
                            Text(
                                text = "يتضمن +${result.leachingFractionPercent}% لغسيل الأملاح المتراكمة",
                                style = MaterialTheme.typography.labelSmall,
                                color = semantic.warning
                            )
                        }
                    }
                }
            }

            // Commercial Fertilizer Quantities
            Text(
                text = "الكميات السمادية الموصى بها (كغ / هكتار):",
                style = MaterialTheme.typography.titleSmall
            )

            NpkBarChart(
                ureaKgHa = result.urea46KgHa,
                mapKgHa = result.dapOrMapKgHa,
                potashKgHa = result.potassiumSulfate50KgHa
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FertilizerMetricBox(
                    title = "يوريا 46% N",
                    kgHa = result.urea46KgHa,
                    netNutrient = "صافي N: ${formatNum(result.netNPerHa)} كغ/هـ",
                    accentColor = semantic.safe,
                    modifier = Modifier.weight(1f)
                )
                FertilizerMetricBox(
                    title = "ماب 12-61-0",
                    kgHa = result.dapOrMapKgHa,
                    netNutrient = "صافي P₂O₅: ${formatNum(result.netPPerHa)} كغ/هـ",
                    accentColor = semantic.warning,
                    modifier = Modifier.weight(1f)
                )
                FertilizerMetricBox(
                    title = "سلفات K₂O 50%",
                    kgHa = result.potassiumSulfate50KgHa,
                    netNutrient = "صافي K₂O: ${formatNum(result.netKPerHa)} كغ/هـ",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "إجمالي الأسمدة التجارية للمساحة الكلية في هذه المرحلة: ${formatNum(result.totalFertilizerKg)} كغ",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "توصيات المهندس الزراعي:",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            result.advisories.forEach { tip ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NpkBarChart(
    ureaKgHa: Double,
    mapKgHa: Double,
    potashKgHa: Double
) {
    val maxVal = max(1.0, maxOf(ureaKgHa, mapKgHa, potashKgHa))
    val semantic = AgriTheme.semantic
    val nColor = semantic.safe
    val pColor = semantic.warning
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            val barHeight = 18.dp.toPx()
            val gap = 12.dp.toPx()
            val items = listOf(
                ureaKgHa to nColor,
                mapKgHa to pColor,
                potashKgHa to secondaryColor
            )
            items.forEachIndexed { idx, (value, color) ->
                val y = idx * (barHeight + gap)
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(0f, y),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                val ratio = (value / maxVal).toFloat().coerceIn(0.03f, 1f)
                val barW = size.width * ratio
                // Draw from right side in RTL visual alignment
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width - barW, y),
                    size = Size(barW, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
    }
}

@Composable
private fun FertilizerMetricBox(
    title: String,
    kgHa: Double,
    netNutrient: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatNum(kgHa)} كغ/هـ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = netNutrient,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatNum(value: Double): String {
    return if (value >= 100.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
