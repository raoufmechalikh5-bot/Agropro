package com.example.ui.encyclopedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.AgroCategory
import com.example.data.catalog.AgrochemicalCatalog
import com.example.data.catalog.AgrochemicalItem
import com.example.data.catalog.ChemicalCompatibilityGroup
import com.example.data.catalog.TankMixResult
import com.example.data.local.CustomAgrochemicalEntity
import com.example.data.local.ExperimentRepository
import com.example.data.local.PesticideNoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EncyclopediaUiState(
    val searchQuery: String = "",
    val selectedCategory: AgroCategory = AgroCategory.ALL,
    val allItems: List<AgrochemicalItem> = AgrochemicalCatalog.items,
    val filteredItems: List<AgrochemicalItem> = AgrochemicalCatalog.items,
    val notesById: Map<String, PesticideNoteEntity> = emptyMap(),
    val customItemIds: Set<String> = emptySet(),
    val mixProductA: AgrochemicalItem = AgrochemicalCatalog.items.first { it.id == "kocide_2000" },
    val mixProductB: AgrochemicalItem = AgrochemicalCatalog.items.first { it.id == "dimethoate" },
    val mixResult: TankMixResult = AgrochemicalCatalog.checkCompatibility(
        AgrochemicalCatalog.items.first { it.id == "kocide_2000" },
        AgrochemicalCatalog.items.first { it.id == "dimethoate" }
    ),
    val isTankMixExpanded: Boolean = true,
    val nextSlotIsA: Boolean = true
)

class EncyclopediaViewModel(
    private val repository: ExperimentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(AgroCategory.ALL)
    private val _mixPair = MutableStateFlow(
        Triple(
            AgrochemicalCatalog.items.first { it.id == "kocide_2000" },
            AgrochemicalCatalog.items.first { it.id == "dimethoate" },
            true
        )
    )
    private val _isTankMixExpanded = MutableStateFlow(true)

    val uiState: StateFlow<EncyclopediaUiState> = combine(
        _searchQuery,
        _selectedCategory,
        repository.allPesticideNotes,
        repository.customAgrochemicals,
        combine(_mixPair, _isTankMixExpanded) { pair, expanded -> pair to expanded }
    ) { query, category, notesList, customEntities, (mixTriple, expanded) ->
        val customMapped = customEntities.map { entity ->
            val cat = AgroCategory.entries.firstOrNull { it.name == entity.categoryName }
                ?: AgroCategory.FUNGICIDE
            AgrochemicalItem(
                id = "custom_${entity.id}",
                commercialName = entity.commercialName,
                activeIngredient = entity.activeIngredient,
                category = cat,
                targetPests = entity.targetPests,
                recommendedDosage = entity.recommendedDosage,
                phiDays = entity.phiDays,
                modeCode = "مضاف محلياً",
                group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
                formulationNotes = entity.notes
            )
        }
        val combinedItems = customMapped + AgrochemicalCatalog.items
        val notesMap = notesList.associateBy { it.agrochemicalId }
        val filtered = filterItems(combinedItems, notesMap, query, category)
        val (prodA, prodB, nextIsA) = mixTriple

        EncyclopediaUiState(
            searchQuery = query,
            selectedCategory = category,
            allItems = combinedItems,
            filteredItems = filtered,
            notesById = notesMap,
            customItemIds = customMapped.map { it.id }.toSet(),
            mixProductA = prodA,
            mixProductB = prodB,
            mixResult = AgrochemicalCatalog.checkCompatibility(prodA, prodB),
            isTankMixExpanded = expanded,
            nextSlotIsA = nextIsA
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EncyclopediaUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: AgroCategory) {
        _selectedCategory.value = category
    }

    fun toggleTankMixExpanded() {
        _isTankMixExpanded.update { !it }
    }

    fun setMixProductA(item: AgrochemicalItem) {
        _mixPair.update { Triple(item, it.second, it.third) }
    }

    fun setMixProductB(item: AgrochemicalItem) {
        _mixPair.update { Triple(it.first, item, it.third) }
    }

    fun addItemToMixChecker(item: AgrochemicalItem) {
        _isTankMixExpanded.value = true
        _mixPair.update { (a, b, nextIsA) ->
            if (nextIsA) Triple(item, b, false) else Triple(a, item, true)
        }
    }

    fun savePesticideNote(
        agrochemicalId: String,
        noteText: String,
        customDosageNote: String,
        photoPath: String?
    ) {
        viewModelScope.launch {
            if (noteText.isBlank() && customDosageNote.isBlank() && photoPath == null) {
                repository.deletePesticideNote(agrochemicalId)
            } else {
                repository.upsertPesticideNote(
                    PesticideNoteEntity(
                        agrochemicalId = agrochemicalId,
                        noteText = noteText.trim(),
                        customDosageNote = customDosageNote.trim(),
                        photoPath = photoPath,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deletePesticideNote(agrochemicalId: String) {
        viewModelScope.launch {
            repository.deletePesticideNote(agrochemicalId)
        }
    }

    fun addCustomAgrochemical(
        commercialName: String,
        activeIngredient: String,
        category: AgroCategory,
        targetPests: String,
        recommendedDosage: String,
        phiDays: String,
        notes: String
    ) {
        if (commercialName.isBlank()) return
        viewModelScope.launch {
            repository.insertCustomAgrochemical(
                CustomAgrochemicalEntity(
                    commercialName = commercialName.trim(),
                    activeIngredient = activeIngredient.trim().ifBlank { "مادة فعالة مخصصة" },
                    categoryName = category.name,
                    targetPests = targetPests.trim().ifBlank { "آفات عامة" },
                    recommendedDosage = recommendedDosage.trim().ifBlank { "حسب النشرة المرفقة" },
                    phiDays = phiDays.trim().ifBlank { "غير محدد" },
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteCustomAgrochemical(itemId: String) {
        val rawId = itemId.removePrefix("custom_").toLongOrNull() ?: return
        viewModelScope.launch {
            repository.deleteCustomAgrochemical(rawId)
            repository.deletePesticideNote(itemId)
        }
    }

    private fun filterItems(
        items: List<AgrochemicalItem>,
        notesMap: Map<String, PesticideNoteEntity>,
        query: String,
        category: AgroCategory
    ): List<AgrochemicalItem> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchesCategory = category == AgroCategory.ALL || item.category == category
            val userNote = notesMap[item.id]?.noteText?.lowercase().orEmpty()
            val matchesQuery = q.isEmpty() ||
                item.commercialName.lowercase().contains(q) ||
                item.activeIngredient.lowercase().contains(q) ||
                item.targetPests.lowercase().contains(q) ||
                userNote.contains(q)
            matchesCategory && matchesQuery
        }
    }

    class Factory(
        private val repository: ExperimentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EncyclopediaViewModel(repository) as T
        }
    }
}
