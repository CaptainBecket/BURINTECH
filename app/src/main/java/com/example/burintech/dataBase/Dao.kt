package com.example.burintech.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    fun insertBarcode(barcodes: BurBarcode)

    @Query("SELECT * FROM BarcodeTable")
    fun getAllBarcodes() : Flow<List<BurBarcode>>

    @Delete
    fun deleteBarcode(barcodes: BurBarcode)
}