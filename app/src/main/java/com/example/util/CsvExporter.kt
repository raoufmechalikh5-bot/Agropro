package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.ExperimentEntity
import com.example.data.local.MeasurementEntity
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun buildExperimentCsv(
        experiment: ExperimentEntity,
        measurements: List<MeasurementEntity>
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel renders Arabic text cleanly
        sb.append('\uFEFF')
        sb.appendLine("عنوان التجربة,${escapeCsv(experiment.title)}")
        sb.appendLine("المحصول,${escapeCsv(experiment.cropName)}")
        sb.appendLine("نوع التجربة,${escapeCsv(experiment.experimentType)}")
        sb.appendLine("الموقع / المخبر,${escapeCsv(experiment.locationOrLab)}")
        sb.appendLine("الفرضية العلمية,${escapeCsv(experiment.hypothesis)}")
        sb.appendLine("الحالة,${escapeCsv(experiment.status)}")
        sb.appendLine()
        sb.appendLine("اليوم,التاريخ,ارتفاع النبات (سم),عدد الأوراق,رطوبة التربة (%),الملاحظات الميدانية")

        measurements.forEach { m ->
            val dateStr = dateFormatter.format(Date(m.recordedDateMillis))
            sb.appendLine(
                "${m.dayNumber},$dateStr,${m.plantHeightCm},${m.leafCount},${m.soilMoisturePercent},${escapeCsv(m.notes)}"
            )
        }
        return sb.toString()
    }

    fun writeCsvToStream(outputStream: OutputStream, csvContent: String) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(csvContent)
            writer.flush()
        }
    }

    fun writeTempCsvForShare(
        context: Context,
        experiment: ExperimentEntity,
        measurements: List<MeasurementEntity>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = "agripro_exp_${experiment.id}_${System.currentTimeMillis()}.csv"
        val file = File(exportDir, safeName)
        file.outputStream().use { out ->
            writeCsvToStream(out, buildExperimentCsv(experiment, measurements))
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        val cleaned = value.replace("\r", " ").replace("\n", " ")
        return if (cleaned.contains(",") || cleaned.contains("\"")) {
            "\"${cleaned.replace("\"", "\"\"")}\""
        } else {
            cleaned
        }
    }
}

object PhotoStorageHelper {
    fun copyUriToInternalFiles(
        context: Context,
        sourceUri: Uri,
        folder: String = "experiment_photos"
    ): String? {
        return try {
            val dir = File(context.filesDir, folder).apply { mkdirs() }
            val dest = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun saveBitmapToInternalFiles(
        context: Context,
        bitmap: android.graphics.Bitmap,
        folder: String = "diagnosis_photos"
    ): String? {
        return try {
            val dir = File(context.filesDir, folder).apply { mkdirs() }
            val dest = File(dir, "diag_${System.currentTimeMillis()}.jpg")
            dest.outputStream().use { output ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, output)
            }
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun createTempCameraFile(context: Context): File {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        return File(dir, "capture_${System.currentTimeMillis()}.jpg")
    }
}
