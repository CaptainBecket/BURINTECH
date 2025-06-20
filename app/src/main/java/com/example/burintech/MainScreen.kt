package com.example.burintech

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.burintech.dataBase.BarcodeDataBase
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, dataBase: BarcodeDataBase){

    var statusText by rememberSaveable { mutableStateOf<String?>("No Code Scanned") }
    var showBool by remember { mutableStateOf(false) } // Добавляем состояние для отображения таблицы

    val cameraPermission = rememberPermissionState(
        Manifest.permission.CAMERA // Permission being requested
    )
    val storagePermission = rememberPermissionState(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    val GPSPepmission = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarsePermission = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Uri?>(null) }

    val imageUri = remember { mutableStateOf<Uri?>(null) }

    // Создаем временный файл для хранения фотографии
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    // Запуск камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && imageUri.value != null) {
                // Обрабатываем изображение после успешного захвата
                imageAnalyzer(
                    context = context,
                    uri = imageUri.value!!,
                    dataBase = dataBase
                )
            }
        }
    )
    // Запуск выбора фото из галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImage = uri
            imageAnalyzer(context, uri, dataBase)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Make the Box take up the entire screen
            .background(Color.White) // Optional: Add a background color for visibility
    ) {
        // Column to arrange UI elements vertically and center them
        Column(
            modifier = Modifier
                .align(Alignment.Center) // Center the Column within the Box
                .padding(16.dp), // Optional: Add padding
            horizontalAlignment = Alignment.CenterHorizontally, // Center children horizontally
            verticalArrangement = Arrangement.Center // Center children vertically
        ) {
            val textToShow = if (cameraPermission.status.shouldShowRationale) {
                "Для работы этого приложения необходимо разрешение на использование камеры. Пожалуйста, предоставьте разрешение."
            } else if (!cameraPermission.status.isGranted) {

                "Для доступа к этой функции требуется разрешение на использование камеры. Пожалуйста, предоставьте разрешение."
            } else if (!storagePermission.status.isGranted) {

                "Для работы этого приложения необходимо разрешение на управление хранилищем. Пожалуйста, предоставьте разрешение."
            } else if (!GPSPepmission.status.isGranted || !coarsePermission.status.isGranted) {

                "Для работы этого приложения необходим доступ к GPS. Пожалуйста, предоставьте."
            } else {
                // If permission is granted, show the scanned barcode or a default message
                statusText ?: "No Scanned"
            }


            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Title image"
            )
            // Display the determined text
            Text(textToShow, textAlign = TextAlign.Center)
            // Show a button to scan a QR or barcode if permission is granted
            if (cameraPermission.status.isGranted) {
                Button(onClick = {
                    try {
                        val photoFile = createImageFile(context)
                        imageUri.value = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        cameraLauncher.launch(imageUri.value!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Обработка ошибки
                    }

                }) {
                    Text("Сделать изображение")
                }
            } else {
                // Show a button to request camera permission if not granted
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Запросить разрешение на камеру")
                }
            }

            if (storagePermission.status.isGranted) {
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    }) {
                    Text("Выбрать изображение из галереи")
                }
            } else {
                Button(onClick = {
                    storagePermission.launchPermissionRequest()
                }) {
                    Text("Запросить разрешение к хранилищу")
                }
            }
            if (GPSPepmission.status.isGranted){
                Button(onClick = {
                    exportCSV(context, dataBase, onAnswer = {
                        statusText = it
                    })
                }

                ) {
                    Text("Отправить данные на сервер")
                }
            } else{
                Button(onClick = {
                    GPSPepmission.launchPermissionRequest()

                }) {
                    Text("Запросить разрешение к GPS")
                }
            }
            if (coarsePermission.status.isGranted){
                Button(onClick = {
                    showBool = true
                }

                ) {
                    Text("Проверить таблицу")
                }
            } else {
                Button(onClick = {
                    coarsePermission.launchPermissionRequest()
                }) {
                    Text("Запросить разрешение к GPS")
                }

            }


        }
    }
    if (showBool) {
        showTable(
            dataBase,
            onAnswer = {showBool = it}
        )
    }

}

