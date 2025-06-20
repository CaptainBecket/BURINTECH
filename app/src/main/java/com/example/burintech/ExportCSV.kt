package com.example.burintech

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.burintech.dataBase.BarcodeDataBase
import com.example.burintech.dataBase.BurBarcode
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toCollection
import java.io.OutputStreamWriter


@Suppress("UNCHECKED_CAST")
fun exportCSV(
    context: Context,
    database: BarcodeDataBase,
    onAnswer: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val barcodes = database.getDao().getAllBarcodes().first()
            val fileName = "barcode_export_${System.currentTimeMillis()}.csv"

            val uri = createFileUsingMediaStore(context, fileName)

            if (uri != null) {
                exportToCsvViaMediaStore(context, uri, barcodes)
                withContext(Dispatchers.Main) {
                    onAnswer("Файл сохранен: $fileName")
                    Toast.makeText(
                        context,
                        "Экспорт завершен: $fileName",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onAnswer("Ошибка: не удалось создать файл")
                    Toast.makeText(
                        context,
                        "Ошибка создания файла",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onAnswer("Ошибка: ${e.localizedMessage}")
                Toast.makeText(
                    context,
                    "Ошибка экспорта: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private fun createFileUsingMediaStore(context: Context, fileName: String): Uri? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/BurintechExport")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
    return resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
}
private fun exportToCsvViaMediaStore(
    context: Context,
    uri: Uri,
    data: List<BurBarcode>
) {
    val resolver = context.contentResolver

    resolver.openOutputStream(uri)?.use { outputStream ->
        OutputStreamWriter(outputStream).use { outputStreamWriter ->
            CSVWriter(outputStreamWriter).use { writer ->
                writer.writeNext(arrayOf(
                    "ID", "Filename", "Barcode",
                    "QR Code", "GPS", "Coordinates", "Date"
                ))
                data.forEach { entity ->
                    writer.writeNext(arrayOf(
                        entity.id.toString(),
                        entity.fileName,
                        entity.materialBarcode,
                        entity.placeQRCode,
                        entity.gps,
                        entity.barcodeCoordinates,
                        entity.date
                    ))
                }
            }
        }
    }
    // Для Android Q+ снимаем флаг IS_PENDING
    val completeValues = ContentValues().apply {
        put(MediaStore.MediaColumns.IS_PENDING, 0)
    }
    resolver.update(uri, completeValues, null, null)
}
