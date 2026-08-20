package com.example.radiomodern.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.radiomodern.data.model.Quotation
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportToCsv(context: Context, quotations: List<Quotation>): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "cotizaciones_$timestamp.csv"

        val csvContent = buildString {
            appendLine("ID,Descripción,Precio,Fecha,Hora,Foto")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            quotations.forEach { q ->
                val date = dateFormat.format(Date(q.createdAt))
                val time = timeFormat.format(Date(q.createdAt))
                val photo = q.photoUri?.let { "Sí" } ?: "No"
                val escapedDescription = "\"${q.description.replace("\"", "\"\"")}\""
                appendLine("${q.id},$escapedDescription,${q.price},$date,$time,$photo")
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, fileName, csvContent)
        } else {
            saveToFile(context, fileName, csvContent)
        }
    }

    private fun saveWithMediaStore(context: Context, fileName: String, content: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
        }

        return uri
    }

    @Suppress("DEPRECATION")
    private fun saveToFile(context: Context, fileName: String, content: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { it.write(content.toByteArray()) }
        return Uri.fromFile(file)
    }
}
