package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.remote.response.ThivienResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ThivienApiService {
    @FormUrlEncoded
    @POST("transcript-query.json.php")
    suspend fun convertHanViet(
        @Field("mode") mode: String = "trans",
        @Field("lang") lang: String = "1",
        @Field("input") input: String
    ): ThivienResponse
}
