package com.example.qoutes.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qoutes.models.Quote

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<Quote>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quote: Quote): Long

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY RANDOM()")
    fun getQuotesByCategory(category: String): LiveData<List<Quote>>

    @Query("SELECT * FROM quotes WHERE isBookmarked = 1")
    fun getSavedQuotes(): LiveData<List<Quote>>

    @Delete
    suspend fun deleteSavedQuote(quote: Quote)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteForNotification(): Quote?
}