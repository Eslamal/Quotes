package com.example.qoutes.api

import com.example.qoutes.models.Quote
import retrofit2.Response
import retrofit2.http.GET

interface QuoteAPI {
    @GET("random")
    suspend fun getRandomQuote(): Response<List<Quote>>

    @GET("today")
    suspend fun getQuoteOfTheDay(): Response<List<Quote>>
}
