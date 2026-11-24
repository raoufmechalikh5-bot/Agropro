package com.example.ui.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExperimentEntity
import com.example.data.local.ExperimentRepository
import com.example.data.local.MeasurementEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotebookViewModel(
    private val repository: ExperimentRepository
) : ViewModel() {

    val experiments: StateFlow<List<ExperimentEntity>> = repository.allExperiments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeasurements: StateFlow<List<MeasurementEntity>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedExperimentId = MutableStateFlow<Long?>(null)
    val selectedExperimentId: StateFlow<Long?> = _selectedExperimentId.asStateFlow()

    val selectedExperiment: StateFlow<ExperimentEntity?> =
        combine(experiments, _selectedExperimentId) { list, selectedId ->
            list.firstOrNull { it.id == selectedId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedMeasurements: StateFlow<List<MeasurementEntity>> =
        combine(allMeasurements, _selectedExperimentId) { all, selectedId ->
            if (selectedId == null) emptyList() else all.filter { it.experimentId == selectedId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSampleExperimentsSeeded()
        }
    }

    fun selectExperiment(id: Long?) {
        _selectedExperimentId.value = id
    }

    fun createExperiment(
        title: String,
        cropName: String,
        experimentType: String,
        locationOrLab: String,
        hypothesis: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newId = repository.insertExperiment(
                ExperimentEntity(
                    title = title.trim(),
                    cropName = cropName.trim().ifBlank { "محصول تجريبي" },
                    experimentType = experimentType,
                    locationOrLab = locationOrLab.trim().ifBlank { "الحقل التجريبي" },
                    hypothesis = hypothesis.trim(),
                    startDateMillis = System.currentTimeMillis(),
                    status = "جارية"
                )
            )
            _selectedExperimentId.value = newId
        }
    }

    fun toggleExperimentStatus(experiment: ExperimentEntity) {
        viewModelScope.launch {
            val nextStatus = if (experiment.status == "جارية") "مكتملة" else "جارية"
            repository.updateExperiment(experiment.copy(status = nextStatus))
        }
    }

    fun deleteExperiment(id: Long) {
        viewModelScope.launch {
            if (_selectedExperimentId.value == id) {
                _selectedExperimentId.value = null
            }
            repository.deleteExperiment(id)
        }
    }

    fun addMeasurement(
        experimentId: Long,
        dayNumber: Int,
        plantHeightCm: Double,
        leafCount: Int,
        soilMoisturePercent: Double,
        notes: String,
        photoPath: String?
    ) {
        viewModelScope.launch {
            repository.insertMeasurement(
                MeasurementEntity(
                    experimentId = experimentId,
                    recordedDateMillis = System.currentTimeMillis(),
                    dayNumber = dayNumber,
                    plantHeightCm = plantHeightCm,
                    leafCount = leafCount,
                    soilMoisturePercent = soilMoisturePercent,
                    notes = notes.trim(),
                    photoPath = photoPath
                )
            )
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
        }
    }

    class Factory(
        private val repository: ExperimentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotebookViewModel(repository) as T
        }
    }
}
