package com.example.burintech

import android.content.Context
import android.widget.Toast


import com.example.burintech.dataBase.BarcodeDataBase
import com.example.burintech.dataBase.BurBarcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

@Suppress("UNCHECKED_CAST")
fun exportCSV(
    context: Context,
    database: BarcodeDataBase,
    onAnswer: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val barcodes = database.getDao().getAllBarcodes().toList()
            val csvData = convertToCsv(barcodes as List<BurBarcode>)
            saveCsvToFile(context, csvData, "database_export_${System.currentTimeMillis()}.csv")
            onAnswer("database_export_${System.currentTimeMillis()}.csv")
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
                onAnswer("Ошибка экспорта: ${e.message}")
            }
        }
    }
}
fun convertToCsv(data: List<BurBarcode>): String {
    val csv = StringBuilder()
    // Заголовки столбцов (названия полей)
    val headers = arrayOf("id", "filename", "barcode", "QR", "GPS", "Coordinates", "Date") // замените на ваши поля
    csv.append(headers.joinToString(",")).append("\n")
    // Данные
    data.forEach { entity ->
        val row = arrayOf(
            entity.id.toString(),
            entity.fileName,
            entity.materialBarcode,
            entity.placeQRCode,
            entity.gps,
            entity.barcodeCoordinates,
            entity.date
        )
        csv.append(row.joinToString(",")).append("\n")
    }

    return csv.toString()
}
fun saveCsvToFile(context: Context, csvData: String, fileName: String) {
    try {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            writer.write(csvData)
        }
        // Можно показать уведомление об успешном сохранении
        Toast.makeText(context, "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
    }
}