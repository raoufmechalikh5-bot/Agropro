package com.example.ui.plantdoctor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.PlantDoctorRepository
import com.example.data.ai.PlantDoctorUiState
import com.example.data.ai.SelectedPlantImage
import com.example.data.catalog.DiseaseFlashcard
import com.example.data.catalog.FlashcardCatalog
import com.example.data.local.DiagnosisHistoryEntity
import com.example.data.local.ExperimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class PlantDoctorViewModel(
    private val aiRepository: PlantDoctorRepository,
    private val localRepository: ExperimentRepository
) : ViewModel() {

    private val _selectedImages = MutableStateFlow<List<SelectedPlantImage>>(emptyList())
    val selectedImages: StateFlow<List<SelectedPlantImage>> = _selectedImages.asStateFlow()

    private val _farmerNotes = MutableStateFlow("")
    val farmerNotes: StateFlow<String> = _farmerNotes.asStateFlow()

    private val _diagnosisState = MutableStateFlow<PlantDoctorUiState>(PlantDoctorUiState.Idle)
    val diagnosisState: StateFlow<PlantDoctorUiState> = _diagnosisState.asStateFlow()

    private val _isFlashcardMode = MutableStateFlow(false)
    val isFlashcardMode: StateFlow<Boolean> = _isFlashcardMode.asStateFlow()

    private val _selectedCropFilter = MutableStateFlow("الكل")
    val selectedCropFilter: StateFlow<String> = _selectedCropFilter.asStateFlow()

    private val _flippedCardIds = MutableStateFlow<Set<String>>(emptySet())
    val flippedCardIds: StateFlow<Set<String>> = _flippedCardIds.asStateFlow()

    private val _masteredCardIds = MutableStateFlow<Set<String>>(emptySet())
    val masteredCardIds: StateFlow<Set<String>> = _masteredCardIds.asStateFlow()

    val filteredFlashcards: StateFlow<List<DiseaseFlashcard>> = _selectedCropFilter
        .combine(MutableStateFlow(FlashcardCatalog.cards)) { filter, cards ->
            if (filter == "الكل") cards else cards.filter { it.cropAr == filter }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FlashcardCatalog.cards)

    val recentDiagnoses: StateFlow<List<DiagnosisHistoryEntity>> = localRepository.recentDiagnoses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFlashcardMode(enabled: Boolean) {
        _isFlashcardMode.value = enabled
    }

    fun selectCropFilter(crop: String) {
        _selectedCropFilter.value = crop
    }

    fun toggleCardFlip(cardId: String) {
        _flippedCardIds.value = _flippedCardIds.value.let { current ->
            if (cardId in current) current - cardId else current + cardId
        }
    }

    fun toggleCardMastered(cardId: String) {
        _masteredCardIds.value = _masteredCardIds.value.let { current ->
            if (cardId in current) current - cardId else current + cardId
        }
    }

    fun updateFarmerNotes(notes: String) {
        _farmerNotes.value = notes
    }

    fun removeImage(imageId: Long) {
        _selectedImages.value = _selectedImages.value.filterNot { it.id == imageId }
        if (_selectedImages.value.isEmpty()) {
            _diagnosisState.value = PlantDoctorUiState.Idle
        }
    }

    fun clearAllImages() {
        _selectedImages.value = emptyList()
        _diagnosisState.value = PlantDoctorUiState.Idle
    }

    fun loadSampleImage(context: Context, @DrawableRes resId: Int, label: String) {
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                val decoded = BitmapFactory.decodeResource(context.resources, resId)
                decoded?.let { scaleBitmap(it, 768) }
            }
            if (bmp != null) {
                _selectedImages.value = listOf(SelectedPlantImage(bitmap = bmp, label = label))
                _diagnosisState.value = PlantDoctorUiState.Idle
            }
        }
    }

    fun loadImageFromUri(context: Context, uri: Uri, label: String) {
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                try {
                    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    raw?.let { scaleBitmap(it, 768) }
                } catch (_: Exception) {
                    null
                }
            }
            if (bmp != null) {
                val current = _selectedImages.value
                val updated = if (current.size >= 3) {
                    current.drop(1) + SelectedPlantImage(bitmap = bmp, label = label)
                } else {
                    current + SelectedPlantImage(bitmap = bmp, label = label)
                }
                _selectedImages.value = updated
                _diagnosisState.value = PlantDoctorUiState.Idle
            } else {
                _diagnosisState.value = PlantDoctorUiState.Error("تعذر قراءة الصورة المختارة. يرجى تجربة صورة أخرى.")
            }
        }
    }

    fun analyzePlantImage(context: Context? = null) {
        val images = _selectedImages.value
        if (images.isEmpty()) {
            _diagnosisState.value = PlantDoctorUiState.Error("يرجى اختيار أو التقاط صورة واحدة على الأقل للورقة أو الثمرة.")
            return
        }

        viewModelScope.launch {
            _diagnosisState.value = PlantDoctorUiState.Loading
            val bitmaps = images.map { it.bitmap }
            val result = aiRepository.diagnosePlantImages(bitmaps, _farmerNotes.value)
            result.fold(
                onSuccess = { diagnosis ->
                    val savedPhotoPath = if (context != null) {
                        withContext(Dispatchers.IO) {
                            com.example.util.PhotoStorageHelper.saveBitmapToInternalFiles(
                                context,
                                bitmaps.first(),
                                "diagnosis_photos"
                            )
                        }
                    } else null
                    _diagnosisState.value = PlantDoctorUiState.Success(diagnosis)
                    localRepository.saveDiagnosis(
                        DiagnosisHistoryEntity(
                            cropNameAr = diagnosis.cropNameAr,
                            diseaseNameAr = diagnosis.diseaseNameAr,
                            scientificName = diagnosis.scientificName,
                            cause = diagnosis.cause,
                            severity = diagnosis.severity,
                            activeIngredient = diagnosis.activeIngredient,
                            phiDays = diagnosis.phiDays,
                            summary = diagnosis.summary,
                            photoPath = savedPhotoPath
                        )
                    )
                },
                onFailure = { error ->
                    _diagnosisState.value = PlantDoctorUiState.Error(
                        error.message ?: "حدث خطأ غير متوقع أثناء التحليل."
                    )
                }
            )
        }
    }

    fun deleteDiagnosisRecord(id: Long) {
        viewModelScope.launch {
            localRepository.deleteDiagnosis(id)
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / largest.toFloat()
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return bitmap.scale(width, height)
    }

    class Factory(
        private val aiRepository: PlantDoctorRepository,
        private val localRepository: ExperimentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlantDoctorViewModel(aiRepository, localRepository) as T
        }
    }
}
