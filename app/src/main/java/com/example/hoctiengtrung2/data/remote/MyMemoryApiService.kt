package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.remote.response.MyMemoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryApiService {
    @GET("get")
    suspend fun traCuu(
        @Query("q") query: String,
        @Query("langpair") langPair: String = "zh|vi"
    ): MyMemoryResponse
}
