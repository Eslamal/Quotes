package com.example.qoutes.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qoutes.models.Quote
import com.example.qoutes.repository.QuoteRepository
import com.example.qoutes.store.Preference
import com.example.qoutes.util.CheckInternet
import com.example.qoutes.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val internet: CheckInternet,
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    val quote: MutableLiveData<Resource<Quote>> = MutableLiveData()
    val bookmarked: MutableLiveData<Boolean> = MutableLiveData(false)

    private var currentQuotesList: List<Quote> = emptyList()

    fun loadQuotesForCategory(categoryId: String) {
        quote.postValue(Resource.Loading())

        quoteRepository.getQuotesByCategory(categoryId).observeForever { quotes ->
            if (quotes.isNotEmpty()) {
                currentQuotesList = quotes
                if (quote.value !is Resource.Success) {
                    showRandomQuoteFromList()
                }
            } else {
                quote.postValue(Resource.Error("لا توجد اقتباسات في هذا القسم حالياً"))
            }
        }
    }

    fun showRandomQuoteFromList() {
        if (currentQuotesList.isNotEmpty()) {
            val randomIndex = Random().nextInt(currentQuotesList.size)
            val randomQuote = currentQuotesList[randomIndex]

            bookmarked.postValue(randomQuote.isBookmarked)

            quote.postValue(Resource.Success(randomQuote))
        }
    }

    fun saveQuote(quote: Quote) = viewModelScope.launch {
        quote.isBookmarked = true
        quoteRepository.upsert(quote)
        bookmarked.postValue(true)
    }

    fun deleteQuote(quote: Quote) = viewModelScope.launch {
        quote.isBookmarked = false
        quoteRepository.upsert(quote)
        bookmarked.postValue(false)
    }


    fun getSavedQuotes() = quoteRepository.getSavedQuotes()

    fun <T> saveSetting(pref: Preference<T>, value: T) = viewModelScope.launch {
        quoteRepository.saveSetting(pref, value)
    }

    fun <T> getSetting(pref: Preference<T>): T = runBlocking {
        return@runBlocking quoteRepository.getSetting(pref)
    }
}