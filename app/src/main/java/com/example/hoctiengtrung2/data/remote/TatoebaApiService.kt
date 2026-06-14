package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.remote.response.TatoebaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TatoebaApiService {
    @GET("v1/sentences")
    suspend fun getRelatedSentences(
        @Query("lang") lang: String = "cmn",
        @Query("q") query: String,
        @Query("sort") sort: String = "relevance",
        @Query("trans:lang") transLang: String = "vie",
        @Query("limit") limit: Int = 5
    ): TatoebaResponse
}
