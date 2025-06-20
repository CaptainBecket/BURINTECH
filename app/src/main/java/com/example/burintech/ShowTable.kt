package com.example.burintech

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.burintech.dataBase.BarcodeDataBase


@Composable
fun showTable(
    db: BarcodeDataBase,
    onAnswer: (Boolean) -> Unit
) {
    val barcodes by db.getDao().getAllBarcodes().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize() // Make the Box take up the entire screen
            .background(Color.White)

    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally, // Center children horizontally
            verticalArrangement = Arrangement.Center // Center children vertically
        ){
            Button(
                onClick = {
                    onAnswer(false)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Text("Назад")
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("ID",  modifier = Modifier.width(50.dp))
                Text("Файл",  modifier = Modifier.width(120.dp))
                Text("Штрих-код",  modifier = Modifier.width(120.dp))
                Text("Место", modifier = Modifier.width(100.dp))
                Text("Координаты",  modifier = Modifier.width(150.dp))
                Text("Дата",  modifier = Modifier.width(100.dp))
            }
            LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 50.dp)) {
                items(barcodes) { barcode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("${barcode.id}", modifier = Modifier.width(50.dp))
                        Text(barcode.fileName, modifier = Modifier.width(120.dp), maxLines = 1)
                        Text(barcode.materialBarcode, modifier = Modifier.width(120.dp))
                        Text(barcode.placeQRCode, modifier = Modifier.width(100.dp))
                        Text(barcode.barcodeCoordinates, modifier = Modifier.width(150.dp))
                        Text(barcode.date, modifier = Modifier.width(100.dp))
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }

            }


        }

    }

}