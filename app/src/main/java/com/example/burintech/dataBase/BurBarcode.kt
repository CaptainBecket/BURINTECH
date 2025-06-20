package com.example.burintech.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BarcodeTable")
data class BurBarcode(
    @PrimaryKey
    val id: Int? = null,
    @ColumnInfo(name = "FileName")
    val fileName: String,
    @ColumnInfo(name = "MaterialName")
    val materialBarcode: String,
    @ColumnInfo(name = "Place")
    val placeQRCode: String,
    @ColumnInfo(name = "GPS")
    val gps: String,
    val barcodeCoordinates: String,
    val date: String
)
