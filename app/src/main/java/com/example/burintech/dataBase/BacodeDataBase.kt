package com.example.burintech.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BurBarcode::class],
    version = 1
)
abstract class BarcodeDataBase : RoomDatabase(){
    abstract fun getDao(): Dao

    companion object{
        fun getDb(context: Context): BarcodeDataBase{
            return Room.databaseBuilder(
                context.applicationContext,
                BarcodeDataBase::class.java,
                "BarcodeDataBase.db"
            ).build()
        }
    }
}