package com.example.burintech

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.burintech.dataBase.BarcodeDataBase
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections.max
import java.util.Collections.min
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.burintech.dataBase.BurBarcode


data class BarcodeResult(
    val value: String,
    val x: Int?,
    val y: Int?,
    val width: Int?,
    val height: Int?
)

// Расширение для преобразования Task в suspend функцию
private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
            }
        }
    }
}
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
fun imageAnalyzer(
    context: Context,
    uri: Uri,
    dataBase: BarcodeDataBase
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val originalImage = InputImage.fromBitmap(bitmap, 0)

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_UNKNOWN,
                    Barcode.FORMAT_ALL_FORMATS,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_AZTEC
                )
                .enableAllPotentialBarcodes()
                .build()

            val scanner = BarcodeScanning.getClient(options)

            lateinit var placeQR:String

            // 1. Сканируем оригинальное изображение
            val originalBarcodes = try {
                scanner.process(originalImage).await()
            } catch (e: Exception) {
                emptyList<Barcode>()
            }
            val results = mutableListOf<BarcodeResult>()

            if (originalBarcodes.isNotEmpty()) {
                originalBarcodes.mapNotNull { barcode ->
                    barcode.rawValue?.let { value ->
                        barcode.boundingBox?.let { rect ->
                            results.add(
                                BarcodeResult(
                                    value = value,
                                    x = rect.left,
                                    y = rect.top,
                                    width = rect.width(),
                                    height = rect.height()
                                )
                            )
                        }
                    }
                }
            }

            // 2. Если не найдено, делим на фрагменты и сканируем каждый
            val rows = 3
            val cols = 3
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    val width = bitmap.width / cols
                    val height = bitmap.height / rows
                    val left = i * width
                    val top = j * height
                    val right = left + width
                    val bottom = top + height

                    if (right > bitmap.width || bottom > bitmap.height) continue

                    try {
                        val fragment = Bitmap.createBitmap(
                            bitmap,
                            left.coerceAtLeast(0),
                            top.coerceAtLeast(0),
                            width.coerceAtMost(bitmap.width - left),
                            height.coerceAtMost(bitmap.height - top)
                        )

                        val fragmentImage = InputImage.fromBitmap(fragment, 0)
                        val fragmentBarcodes = try {
                            scanner.process(fragmentImage).await()
                        } catch (e: Exception) {
                            continue
                        }

                        fragmentBarcodes.forEach { barcode ->
                            barcode.rawValue?.let { value ->
                                barcode.boundingBox?.let { rect ->
                                    results.add(
                                        BarcodeResult(
                                            value = value,
                                            x = left + rect.left,
                                            y = top + rect.top,
                                            width = rect.width(),
                                            height = rect.height()
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BarcodeScan", "Ошибка обработки фрагмента: ${e.message}")
                    }
                }
            }

            val qRs = mutableListOf<String>()
            val coordinateY = mutableListOf<Int>()
            results.forEach{
                if (it.value.startsWith("metal", ignoreCase = true)){
                    qRs.add(it.value)
                    it.y?.let { it1 -> coordinateY.add(it1.toInt()) }
                }
            }
            results.removeAll { it.value.startsWith("metal", ignoreCase = true) }
            val coordinatesStatic : List<Int> = coordinateY

            placeQR = if (qRs.size>1){
                if (qRs[0].endsWith("L")){
                    qRs[coordinatesStatic.indexOf(max(coordinatesStatic))]
                } else {
                    qRs[coordinatesStatic.indexOf(min(coordinatesStatic))]
                }

            } else if (qRs.size == 1){
                qRs[0]

            } else {
                "No value"
            }
            var gpsValue = ""

            getLocation(context){ lat, lon, error ->
                gpsValue = if (error != null) {
                    "Ufa"
                } else {
                    "Ш:$lat Д:$lon"
                }
            }

            val dateForDb = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")).toString()
            val fileName = "${dateForDb}.jpg"
            saveImage(context, fileName, uri)
            Thread{
                results.forEach{
                    val item = BurBarcode(
                        id = null,
                        fileName = fileName,
                        materialBarcode = it.value,
                        placeQRCode = placeQR,
                        gps = gpsValue,
                        date = dateForDb,
                        barcodeCoordinates = "x = ${it.x.toString()}  y = ${it.y.toString()}"
                    )
                    dataBase.getDao().insertBarcode(item)
                }
            }.start()


        } catch (e: Exception) {
            Log.e("BarcodeScan", "Ошибка обработки изображения: ${e.message}")

        }
    }
}