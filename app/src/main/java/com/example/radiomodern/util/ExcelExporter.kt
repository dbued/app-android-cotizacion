package com.example.radiomodern.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.radiomodern.data.model.Quotation
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    private val currencyFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportToExcel(context: Context, quotations: List<Quotation>): Uri? {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Cotizaciones")

        createHeaderRow(workbook, sheet)
        populateDataRows(workbook, sheet, quotations)
        autoSizeColumns(sheet, quotations.size)

        val fileName = "cotizaciones_${fileTimestamp.format(Date())}.xls"
        return saveWorkbook(context, workbook, fileName)
    }

    private fun createHeaderRow(workbook: HSSFWorkbook, sheet: org.apache.poi.ss.usermodel.Sheet) {
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Descripción", "Precio", "Fecha", "Hora", "Foto")

        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.INDIGO.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN

            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
                fontHeightInPoints = 11
            }
            setFont(font)
        }

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
    }

    private fun populateDataRows(
        workbook: HSSFWorkbook,
        sheet: org.apache.poi.ss.usermodel.Sheet,
        quotations: List<Quotation>
    ) {
        val dataStyle = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.LEFT
        }

        val priceStyle = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.RIGHT
        }

        val greenFont = workbook.createFont().apply {
            color = IndexedColors.DARK_GREEN.index
            bold = true
            fontHeightInPoints = 11
        }
        priceStyle.setFont(greenFont)

        val altRowStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.LEFT
        }

        val altPriceStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.RIGHT
        }
        altPriceStyle.setFont(greenFont)

        quotations.forEachIndexed { index, quotation ->
            val row = sheet.createRow(index + 1)
            val isAlt = index % 2 == 1
            val rowStyle = if (isAlt) altRowStyle else dataStyle
            val rowPriceStyle = if (isAlt) altPriceStyle else priceStyle

            row.createCell(0).apply {
                setCellValue(quotation.id.toDouble())
                cellStyle = rowStyle
            }
            row.createCell(1).apply {
                setCellValue(quotation.description)
                cellStyle = rowStyle
            }
            row.createCell(2).apply {
                setCellValue(quotation.price)
                cellStyle = rowPriceStyle
            }
            row.createCell(3).apply {
                setCellValue(dateFormat.format(Date(quotation.createdAt)))
                cellStyle = rowStyle
            }
            row.createCell(4).apply {
                setCellValue(timeFormat.format(Date(quotation.createdAt)))
                cellStyle = rowStyle
            }
            row.createCell(5).apply {
                setCellValue(if (quotation.photoUri != null) "Sí" else "No")
                cellStyle = rowStyle
            }
        }
    }

    private fun autoSizeColumns(sheet: org.apache.poi.ss.usermodel.Sheet, rowCount: Int) {
        for (i in 0..5) {
            sheet.setColumnWidth(i, 5000)
        }
        sheet.setColumnWidth(1, 12000)
    }

    private fun saveWorkbook(context: Context, workbook: HSSFWorkbook, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/vnd.ms-excel")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

        return uri
    }
}
