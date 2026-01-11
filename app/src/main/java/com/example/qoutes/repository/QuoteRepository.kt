package com.example.qoutes.repository

import com.example.qoutes.api.QuoteAPI
import com.example.qoutes.db.QuoteDao
import com.example.qoutes.models.Quote
import com.example.qoutes.store.Preference
import com.example.qoutes.store.PreferenceStore
import javax.inject.Inject

class QuoteRepository @Inject constructor(
    private val dao: QuoteDao,
    private val api: QuoteAPI,
    private val preferenceStore: PreferenceStore
) {

    suspend fun getRandomQuote() = api.getRandomQuote()

    suspend fun getRandomQuoteForNotification() = dao.getRandomQuoteForNotification()

    suspend fun getQuoteOfTheDay() = api.getQuoteOfTheDay()

    suspend fun upsert(quote: Quote) = dao.upsert(quote)

    fun getQuotesByCategory(category: String) = dao.getQuotesByCategory(category)

    suspend fun deleteQuote(quote: Quote) =
        dao.deleteSavedQuote(quote)

    fun getSavedQuotes() = dao.getSavedQuotes()

    suspend fun <T> getSetting(preference: Preference<T>) = preferenceStore.getPreference(preference)

    suspend fun <T> saveSetting(preference: Preference<T>, value: T) =
        preferenceStore.putPreference(preference, value)
}
