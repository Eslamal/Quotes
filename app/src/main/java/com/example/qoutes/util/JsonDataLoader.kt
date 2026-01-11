package com.example.qoutes.util

import android.content.Context
import com.example.qoutes.db.QuoteDao
import com.example.qoutes.models.Quote
import com.example.qoutes.store.Preference
import com.example.qoutes.store.PreferenceStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class JsonDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quoteDao: QuoteDao,
    private val preferenceStore: PreferenceStore
) {

    private val jsonFiles = listOf(
        "prayers.json",
        "wisdom.json",
        "proverbs.json",
        "books.json",
        "motivation.json",
        "success.json",
        "stoicism.json",
        "technology.json"
    )

    fun loadDataIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val isLoaded = preferenceStore.getPreference(Preference(androidx.datastore.preferences.core.booleanPreferencesKey("data_loaded_v1"), false))

            if (!isLoaded) {
                try {
                    val gson = Gson()
                    val allQuotes = mutableListOf<Quote>()

                    jsonFiles.forEach { fileName ->
                        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                        val listType = object : TypeToken<List<Quote>>() {}.type
                        val quotes: List<Quote> = gson.fromJson(jsonString, listType)

                        val categoryName = fileName.replace(".json", "")

                        val quotesWithCategory = quotes.map { it.copy(category = it.category.ifEmpty { categoryName }, isBookmarked = false) }
                        allQuotes.addAll(quotesWithCategory)
                    }

                    if (allQuotes.isNotEmpty()) {
                        quoteDao.insertAll(allQuotes)
                        preferenceStore.putPreference(Preference(androidx.datastore.preferences.core.booleanPreferencesKey("data_loaded_v1"), true), true)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}