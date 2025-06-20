package com.example.burintech.dataBase

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object Module {
    @Provides
    @Singleton
    fun provideBarcodeDataBase(app: Application) : BarcodeDataBase{
        return  Room.databaseBuilder(
            app,
            BarcodeDataBase::class.java,
            "barcodes.db"
        ).build()

    }
}