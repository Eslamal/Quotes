package com.example.qoutes.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.qoutes.models.Quote

@Database(
    entities = [Quote::class],
    version = 1
)
abstract class QuoteDataBase : RoomDatabase() {
    abstract fun getQuoteDao(): QuoteDao
}
