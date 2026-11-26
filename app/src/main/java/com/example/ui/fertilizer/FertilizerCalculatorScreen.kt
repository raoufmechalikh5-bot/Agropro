package com.example.ui.fertilizer

import com.example.ui.components.ExpandableSuggestions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.FertilizerCalculationEntity
import com.example.ui.theme.AgriTheme
import com.example.ui.theme.AgriPageHero
import com.example.ui.theme.agriAmbientBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import com.example.ui.weather.WeatherContextBanner
import com.example.ui.weather.WeatherContextMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerCalculatorScreen(
    viewModel: FertilizerCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedCalculations by viewModel.savedCalculations.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var cropDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize().agriAmbientBackground(),
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
                WeatherContextBanner(mode = WeatherContextMode.FERTILIZER)
            }

            item {
                AgriPageHero(
                    eyebrow = "AGROPRO • NUTRITION",
                    title = "حاسبة NPK الذكية",
                    subtitle = "احسب احتياجات التسميد حسب محصولك ومساحة أرضك",
                    icon = Icons.Default.Grass
                )
            }

            // Crop Type & Stage Input Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                                text = "1. اختيار نوع المحصول ومرحلة النمو",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = cropDropdownExpanded,
                            onExpandedChange = { cropDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCrop.titleAr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.fertilizer_calc_crop_label)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("fertilizer_crop_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = cropDropdownExpanded,
                                onDismissRequest = { cropDropdownExpanded = false }
                            ) {
                                FertilizerCrop.entries.forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop.titleAr) },
                                        onClick = {
                                            viewModel.selectCrop(crop)
                                            cropDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("fertilizer_crop_option_${crop.name}")
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.fertilizer_calc_stage_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(FertilizerStage.entries) { stage ->
                                FilterChip(
                                    selected = state.selectedStage == stage,
                                    onClick = { viewModel.selectStage(stage) },
                                    label = { Text(stage.titleAr, maxLines = 1) },
                                    modifier = Modifier.testTag("fertilizer_stage_chip_${stage.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Area Size Input Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "2. تحديد مساحة الأرض",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        OutlinedTextField(
                            value = state.areaInput,
                            onValueChange = viewModel::updateAreaInput,
                            label = { Text(stringResource(R.string.fertilizer_calc_area_label)) },
                            suffix = { Text(state.areaUnit.shortLabelAr) },
                            isError = !state.isAreaValid,
                            supportingText = if (!state.isAreaValid) {
                                { Text(stringResource(R.string.fertilizer_calc_area_error)) }
                            } else {
                                {
                                    Text(
                                        text = "المساحة المعادلة: ${formatMetric(state.result.areaHectares)} هكتار",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
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
                                .testTag("fertilizer_area_input")
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fertilizer_area_unit_row")
                        ) {
                            FertilizerAreaUnit.entries.forEachIndexed { index, unit ->
                                SegmentedButton(
                                    selected = state.areaUnit == unit,
                                    onClick = { viewModel.selectAreaUnit(unit) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = FertilizerAreaUnit.entries.size
                                    ),
                                    modifier = Modifier.testTag("fertilizer_unit_${unit.name}")
                                ) {
                                    Text(
                                        text = unit.shortLabelAr,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        val quickPresets = when (state.areaUnit) {
                            FertilizerAreaUnit.HECTARE -> listOf("0.5", "1.0", "2.5", "5.0", "10")
                            FertilizerAreaUnit.DUNAM -> listOf("1", "5", "10", "25", "50")
                            FertilizerAreaUnit.SQUARE_METER -> listOf("250", "500", "1000", "2500", "5000")
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickPresets) { preset ->
                                AssistChip(
                                    onClick = { viewModel.applyQuickArea(preset) },
                                    label = { Text("$preset ${state.areaUnit.shortLabelAr}") },
                                    modifier = Modifier.testTag("fertilizer_area_preset_$preset")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = state.plotLabelInput,
                            onValueChange = viewModel::updatePlotLabel,
                            label = { Text(stringResource(R.string.fertilizer_calc_plot_label)) },
                            placeholder = { Text(stringResource(R.string.fertilizer_calc_plot_hint)) },
                            trailingIcon = {
                                if (state.plotLabelInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.updatePlotLabel("") },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح اسم الحقل")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fertilizer_plot_label_input")
                        )
                    }
                }
            }

            // Recommended NPK Ratio Result Card
            item {
                NpkRecommendationCard(
                    crop = state.selectedCrop,
                    stage = state.selectedStage,
                    isAreaValid = state.isAreaValid,
                    result = state.result
                )
            }

            // Commercial Fertilizers & Agronomy Guidance Card
            item {
                AnimatedVisibility(visible = state.isAreaValid) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CommercialFertilizerBreakdownCard(
                            crop = state.selectedCrop,
                            result = state.result
                        )

                        Button(
                            onClick = viewModel::saveCurrentCalculation,
                            enabled = state.isAreaValid,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_fertilizer_calculation_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.fertilizer_calc_save_offline))
                        }

                        AnimatedVisibility(visible = state.lastSavedAtMillis != null) {
                            val semantic = AgriTheme.semantic
                            Surface(
                                color = semantic.safeContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fertilizer_saved_confirmation")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = semantic.safe
                                    )
                                    Text(
                                        text = stringResource(R.string.fertilizer_calc_saved_banner),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.onSafeContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Offline Saved Calculations History Section
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
                        text = stringResource(R.string.fertilizer_calc_history_heading),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (savedCalculations.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fertilizer_history_empty")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdded,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.fertilizer_calc_history_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(savedCalculations, key = { it.id }) { record ->
                    SavedFertilizerCalculationCard(
                        record = record,
                        onLoad = { viewModel.loadSavedCalculation(record) },
                        onDelete = { viewModel.deleteSavedCalculation(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FertilizerHeroHeader() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.fertilizer_calc_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.fertilizer_calc_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NpkRecommendationCard(
    crop: FertilizerCrop,
    stage: FertilizerStage,
    isAreaValid: Boolean,
    result: FertilizerNpkResult
) {
    val semantic = AgriTheme.semantic
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fertilizer_npk_result_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.fertilizer_calc_ratio_heading),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stage.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Hero Ratio Box
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fertilizer_npk_ratio_banner")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "النسبة المتوازنة (N : P₂O₅ : K₂O)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = result.formattedRatio,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("fertilizer_npk_ratio_value")
                    )
                    Text(
                        text = "التركيبة التجارية المقترحة: ${crop.commercialBlendHint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            NpkProportionStackedBar(
                nShare = result.nSharePercent,
                pShare = result.pSharePercent,
                kShare = result.kSharePercent
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = stringResource(R.string.fertilizer_calc_area_needs_heading),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutrientRatioTile(
                    symbol = "N",
                    nameAr = "آزوت",
                    sharePercent = result.nSharePercent,
                    totalKg = if (isAreaValid) result.totalNKg else 0.0,
                    perHaKg = result.nPerHaKg,
                    accentColor = semantic.safe,
                    containerColor = semantic.safeContainer,
                    onContainerColor = semantic.onSafeContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("npk_tile_n")
                )
                NutrientRatioTile(
                    symbol = "P₂O₅",
                    nameAr = "فوسفور",
                    sharePercent = result.pSharePercent,
                    totalKg = if (isAreaValid) result.totalPKg else 0.0,
                    perHaKg = result.pPerHaKg,
                    accentColor = semantic.warning,
                    containerColor = semantic.warningContainer,
                    onContainerColor = semantic.onWarningContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("npk_tile_p")
                )
                NutrientRatioTile(
                    symbol = "K₂O",
                    nameAr = "بوتاسيوم",
                    sharePercent = result.kSharePercent,
                    totalKg = if (isAreaValid) result.totalKKg else 0.0,
                    perHaKg = result.kPerHaKg,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("npk_tile_k")
                )
            }
        }
    }
}

@Composable
private fun NpkProportionStackedBar(
    nShare: Int,
    pShare: Int,
    kShare: Int
) {
    val semantic = AgriTheme.semantic
    val nColor = semantic.safe
    val pColor = semantic.warning
    val kColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(50))
        ) {
            drawRoundRect(
                color = trackColor,
                size = size,
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            )
            val total = (nShare + pShare + kShare).coerceAtLeast(1).toFloat()
            val nWidth = size.width * (nShare / total)
            val pWidth = size.width * (pShare / total)
            val kWidth = (size.width - nWidth - pWidth).coerceAtLeast(0f)

            var currentX = 0f
            if (nWidth > 0f) {
                drawRect(
                    color = nColor,
                    topLeft = Offset(currentX, 0f),
                    size = Size(nWidth, size.height)
                )
                currentX += nWidth
            }
            if (pWidth > 0f) {
                drawRect(
                    color = pColor,
                    topLeft = Offset(currentX, 0f),
                    size = Size(pWidth, size.height)
                )
                currentX += pWidth
            }
            if (kWidth > 0f) {
                drawRect(
                    color = kColor,
                    topLeft = Offset(currentX, 0f),
                    size = Size(kWidth, size.height)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = nColor, label = "آزوت N ($nShare%)")
            LegendItem(color = pColor, label = "فوسفور P ($pShare%)")
            LegendItem(color = kColor, label = "بوتاسيوم K ($kShare%)")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NutrientRatioTile(
    symbol: String,
    nameAr: String,
    sharePercent: Int,
    totalKg: Double,
    perHaKg: Double,
    accentColor: Color,
    containerColor: Color,
    onContainerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "$sharePercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Text(
                text = nameAr,
                style = MaterialTheme.typography.labelSmall,
                color = onContainerColor.copy(alpha = 0.8f)
            )
            Text(
                text = "${formatMetric(totalKg)} كغ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onContainerColor
            )
            Text(
                text = "${formatMetric(perHaKg)} كغ/هـ",
                style = MaterialTheme.typography.labelSmall,
                color = onContainerColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CommercialFertilizerBreakdownCard(
    crop: FertilizerCrop,
    result: FertilizerNpkResult
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fertilizer_commercial_card")
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
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.fertilizer_calc_commercial_heading),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            val semantic = AgriTheme.semantic
            CommercialFertilizerRow(
                name = "يوريا 46% (مصدر النيتروجين N)",
                kg = result.urea46Kg,
                accentColor = semantic.safe
            )
            CommercialFertilizerRow(
                name = "ماب 12-61-0 (مصدر الفوسفور P₂O₅)",
                kg = result.map61Kg,
                accentColor = semantic.warning
            )
            CommercialFertilizerRow(
                name = "سلفات البوتاسيوم 0-0-50 (مصدر K₂O)",
                kg = result.potassiumSulfate50Kg,
                accentColor = MaterialTheme.colorScheme.secondary
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "إجمالي الأسمدة التجارية المطلوبة للمساحة: ${formatMetric(result.totalCommercialKg)} كغ",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            ExpandableSuggestions(
                items = listOf(crop.agronomyTipAr),
                title = "توصية المحصول • pH مثالي: ${crop.idealPhRange}"
            )
        }
    }
}

@Composable
private fun CommercialFertilizerRow(
    name: String,
    kg: Double,
    accentColor: Color
) {
    val bags25Kg = ceil(kg / 25.0).toInt().coerceAtLeast(if (kg > 0.0) 1 else 0)
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "حوالي $bags25Kg كيس (وزن 25 كغ)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${formatMetric(kg)} كغ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun SavedFertilizerCalculationCard(
    record: FertilizerCalculationEntity,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.savedAt) {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(record.savedAt))
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoad)
            .testTag("saved_fertilizer_card_${record.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val title = if (record.plotLabel.isNotBlank()) {
                    "${record.plotLabel} • ${record.cropTitleAr.substringBefore(" (")}"
                } else {
                    record.cropTitleAr
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatMetric(record.areaValue)} ${record.areaUnitLabelAr} • ${record.stageTitleAr}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "NPK: ${record.npkRatio}  |  إجمالي الأسمدة: ${formatMetric(record.totalCommercialKg)} كغ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$dateStr • اضغط لإعادة التحميل",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_saved_fertilizer_${record.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف الحساب المحفوظ",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatMetric(value: Double): String {
    return when {
        value >= 100.0 -> String.format(Locale.US, "%.0f", value)
        value >= 10.0 -> String.format(Locale.US, "%.1f", value)
        value > 0.0 -> String.format(Locale.US, "%.2f", value)
        else -> "0"
    }
}
