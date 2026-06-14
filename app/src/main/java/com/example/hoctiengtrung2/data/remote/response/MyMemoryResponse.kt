package com.example.hoctiengtrung2.data.remote.response

import com.google.gson.annotations.SerializedName

data class MyMemoryResponse(
    @SerializedName("responseData") val responseData: ResponseData,
    @SerializedName("matches") val matches: List<Match>? = null
)

data class ResponseData(
    @SerializedName("translatedText") val translatedText: String
)

data class Match(
    @SerializedName("id") val id: String,
    @SerializedName("translation") val translation: String,
    @SerializedName("quality") val quality: String? = null
)
