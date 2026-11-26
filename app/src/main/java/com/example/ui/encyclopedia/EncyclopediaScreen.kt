package com.example.ui.encyclopedia

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.catalog.AgroCategory
import com.example.data.catalog.AgrochemicalItem
import com.example.data.catalog.CompatibilityLevel
import com.example.data.catalog.TankMixResult
import com.example.data.local.PesticideNoteEntity
import com.example.ui.theme.AgriTheme
import com.example.ui.theme.AgriPageHero
import com.example.ui.theme.agriAmbientBackground
import com.example.util.PhotoStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun EncyclopediaScreen(
    viewModel: EncyclopediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editingNoteForItem by remember { mutableStateOf<AgrochemicalItem?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize().agriAmbientBackground(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AgriPageHero(
                    eyebrow = "AGROPRO • KNOWLEDGE",
                    title = "الموسوعة الزراعية",
                    subtitle = "ابحث عن الآفات والمواد الفعالة وطرق المكافحة",
                    icon = Icons.Default.Science
                )
            }
            item {
                TankMixCheckerCard(
                    allItems = state.allItems,
                    productA = state.mixProductA,
                    productB = state.mixProductB,
                    mixResult = state.mixResult,
                    isExpanded = state.isTankMixExpanded,
                    onToggleExpanded = viewModel::toggleTankMixExpanded,
                    onSelectA = viewModel::setMixProductA,
                    onSelectB = viewModel::setMixProductB
                )
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    label = { Text("ابحث بالاسم التجاري، المادة الفعالة، الآفة، أو ملاحظاتك") },
                    placeholder = { Text("مثال: أزوكسيستروبين، عين الطاووس، المن...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.updateSearchQuery("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("encyclopedia_search_input")
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AgroCategory.entries) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.titleAr) },
                            modifier = Modifier.testTag("category_chip_${category.name}")
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المركبات المتوفرة (${state.filteredItems.size} • محفوظة محلياً)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = { showAddCustomDialog = true },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_custom_pesticide_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة منتج")
                    }
                }
            }

            if (state.filteredItems.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "لا توجد مركبات مطابقة للبحث",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "جرّب البحث باسم المادة الفعالة أو اختر تصنيف «الكل».",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(state.filteredItems, key = { it.id }) { item ->
                    val savedNote = state.notesById[item.id]
                    val isCustom = item.id in state.customItemIds
                    AgrochemicalCard(
                        item = item,
                        savedNote = savedNote,
                        isCustom = isCustom,
                        onAddToMix = { viewModel.addItemToMixChecker(item) },
                        onEditNote = { editingNoteForItem = item },
                        onDeleteNote = { viewModel.deletePesticideNote(item.id) },
                        onDeleteCustom = { viewModel.deleteCustomAgrochemical(item.id) }
                    )
                }
            }
        }
    }

    val targetItem = editingNoteForItem
    if (targetItem != null) {
        val existing = state.notesById[targetItem.id]
        PesticideNoteDialog(
            itemName = targetItem.commercialName,
            existingNote = existing,
            onDismiss = { editingNoteForItem = null },
            onSave = { noteText, customDosage, newPhotoUri, existingPhotoPath ->
                scope.launch {
                    val finalPhotoPath = if (newPhotoUri != null) {
                        withContext(Dispatchers.IO) {
                            PhotoStorageHelper.copyUriToInternalFiles(
                                context,
                                newPhotoUri,
                                folder = "pesticide_photos"
                            )
                        }
                    } else {
                        existingPhotoPath
                    }
                    viewModel.savePesticideNote(
                        agrochemicalId = targetItem.id,
                        noteText = noteText,
                        customDosageNote = customDosage,
                        photoPath = finalPhotoPath
                    )
                    editingNoteForItem = null
                }
            }
        )
    }

    if (showAddCustomDialog) {
        AddCustomAgrochemicalDialog(
            onDismiss = { showAddCustomDialog = false },
            onConfirm = { name, active, category, pests, dosage, phi, notes ->
                viewModel.addCustomAgrochemical(name, active, category, pests, dosage, phi, notes)
                showAddCustomDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TankMixCheckerCard(
    allItems: List<AgrochemicalItem>,
    productA: AgrochemicalItem,
    productB: AgrochemicalItem,
    mixResult: TankMixResult,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectA: (AgrochemicalItem) -> Unit,
    onSelectB: (AgrochemicalItem) -> Unit
) {
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    val semantic = AgriTheme.semantic
    val (accentColor, containerColor, onContainerColor, statusIcon) = when (mixResult.level) {
        CompatibilityLevel.COMPATIBLE -> listOf(
            semantic.safe,
            semantic.safeContainer,
            semantic.onSafeContainer,
            Icons.Default.CheckCircle
        )
        CompatibilityLevel.CAUTION -> listOf(
            semantic.warning,
            semantic.warningContainer,
            semantic.onWarningContainer,
            Icons.Default.WarningAmber
        )
        CompatibilityLevel.INCOMPATIBLE -> listOf(
            semantic.danger,
            semantic.dangerContainer,
            semantic.onDangerContainer,
            Icons.Default.Dangerous
        )
    }
    val statusAccent = accentColor as androidx.compose.ui.graphics.Color
    val statusContainer = containerColor as androidx.compose.ui.graphics.Color
    val statusOnContainer = onContainerColor as androidx.compose.ui.graphics.Color
    val statusVector = statusIcon as androidx.compose.ui.graphics.vector.ImageVector

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.5.dp, statusAccent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tank_mix_checker_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "فحص قابلية الخلط (Tank-Mix)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "تحقق من التوافق الكيميائي قبل الخلط في خزان الرش",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_tank_mix_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "طي أداة فحص الخلط" else "توسيع أداة فحص الخلط"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedA,
                        onExpandedChange = { expandedA = it }
                    ) {
                        OutlinedTextField(
                            value = productA.commercialName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المنتج الأول (أ)") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("mix_product_a_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedA,
                            onDismissRequest = { expandedA = false }
                        ) {
                            allItems.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.commercialName) },
                                    onClick = {
                                        onSelectA(option)
                                        expandedA = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedB,
                        onExpandedChange = { expandedB = it }
                    ) {
                        OutlinedTextField(
                            value = productB.commercialName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المنتج الثاني (ب)") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("mix_product_b_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedB,
                            onDismissRequest = { expandedB = false }
                        ) {
                            allItems.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.commercialName) },
                                    onClick = {
                                        onSelectB(option)
                                        expandedB = false
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        color = statusContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tank_mix_verdict_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = statusVector,
                                    contentDescription = null,
                                    tint = statusAccent
                                )
                                Text(
                                    text = mixResult.level.labelAr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusAccent
                                )
                            }
                            Text(
                                text = mixResult.explanationAr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = statusOnContainer
                            )
                            Text(
                                text = "إرشاد الخلط: ${mixResult.mixingAdviceAr}",
                                style = MaterialTheme.typography.labelMedium,
                                color = statusAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgrochemicalCard(
    item: AgrochemicalItem,
    savedNote: PesticideNoteEntity?,
    isCustom: Boolean,
    onAddToMix: () -> Unit,
    onEditNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onDeleteCustom: () -> Unit
) {
    var isExpanded by remember(item.id) { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("agro_card_${item.id}")
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
                Text(
                    text = item.commercialName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = if (isExpanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (savedNote != null) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = "توجد ملاحظة محفوظة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = item.modeCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "طي التفاصيل" else "عرض التفاصيل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "الآفات المستهدفة: ${item.targetPests}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isExpanded) 6 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "المادة الفعالة: ${item.activeIngredient}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "الجرعة الموصى بها: ${item.recommendedDosage}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "فترة التحريم (PHI): ${item.phiDays}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (item.formulationNotes.isNotBlank()) {
                        Text(
                            text = item.formulationNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (savedNote != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saved_note_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.StickyNote2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "ملاحظتك المحفوظة محلياً:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = onDeleteNote,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "حذف الملاحظة",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (savedNote.customDosageNote.isNotBlank()) {
                                    Text(
                                        text = "الجرعة الحقلية المعتمدة: ${savedNote.customDosageNote}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (savedNote.noteText.isNotBlank()) {
                                    Text(
                                        text = savedNote.noteText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                val path = savedNote.photoPath
                                if (!path.isNullOrBlank() && File(path).exists()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = File(path)),
                                        contentDescription = "صورة مرفقة بملاحظة ${item.commercialName}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCustom) {
                            IconButton(
                                onClick = onDeleteCustom,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف المنتج المخصص",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onEditNote,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("edit_note_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (savedNote == null) "ملاحظة ميدانية" else "تعديل الملاحظة", maxLines = 1)
                        }

                        FilledTonalButton(
                            onClick = onAddToMix,
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_mix_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("فحص الخلط", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PesticideNoteDialog(
    itemName: String,
    existingNote: PesticideNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (noteText: String, customDosage: String, newPhotoUri: Uri?, existingPhotoPath: String?) -> Unit
) {
    var noteText by remember { mutableStateOf(existingNote?.noteText.orEmpty()) }
    var customDosage by remember { mutableStateOf(existingNote?.customDosageNote.orEmpty()) }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoPath by remember { mutableStateOf(existingNote?.photoPath) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) newPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ملاحظات مخصصة: $itemName") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customDosage,
                    onValueChange = { customDosage = it },
                    label = { Text("جرعة مزرعتك المجرّبة (اختياري)") },
                    placeholder = { Text("مثال: 80 مل / 100 لتر على الزيتون") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pesticide_dosage_note_input")
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("الملاحظات الحقلية والفعالية") },
                    placeholder = { Text("تاريخ الرش، المورد، نتائج المكافحة...") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pesticide_note_text_input")
                )
                OutlinedButton(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            newPhotoUri != null -> "تم اختيار صورة جديدة ✅"
                            !existingPhotoPath.isNullOrBlank() -> "تغيير الصورة المحفوظة"
                            else -> "إرفاق صورة العبوة أو النتائج"
                        }
                    )
                }
                if (existingPhotoPath != null && newPhotoUri == null) {
                    TextButton(onClick = { existingPhotoPath = null }) {
                        Text("إزالة الصورة المرفقة", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText, customDosage, newPhotoUri, existingPhotoPath) },
                modifier = Modifier.testTag("save_pesticide_note_button")
            ) {
                Text("حفظ دائم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun AddCustomAgrochemicalDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        commercialName: String,
        activeIngredient: String,
        category: AgroCategory,
        targetPests: String,
        recommendedDosage: String,
        phiDays: String,
        notes: String
    ) -> Unit
) {
    var commercialName by remember { mutableStateOf("") }
    var activeIngredient by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AgroCategory.FUNGICIDE) }
    var targetPests by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var phi by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectableCategories = AgroCategory.entries.filter { it != AgroCategory.ALL }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مبيد أو سماد مخصص") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commercialName,
                    onValueChange = { commercialName = it },
                    label = { Text("الاسم التجاري *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_agro_name_input")
                )
                OutlinedTextField(
                    value = activeIngredient,
                    onValueChange = { activeIngredient = it },
                    label = { Text("المادة الفعالة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("التصنيف:", style = MaterialTheme.typography.labelMedium)
                selectableCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.titleAr) }
                    )
                }
                OutlinedTextField(
                    value = targetPests,
                    onValueChange = { targetPests = it },
                    label = { Text("الآفات / الأمراض المستهدفة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("الجرعة الموصى بها") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phi,
                    onValueChange = { phi = it },
                    label = { Text("فترة التحريم (PHI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات التحضير") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(commercialName, activeIngredient, category, targetPests, dosage, phi, notes)
                },
                enabled = commercialName.isNotBlank(),
                modifier = Modifier.testTag("confirm_custom_agro_button")
            ) {
                Text("حفظ في الموسوعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
