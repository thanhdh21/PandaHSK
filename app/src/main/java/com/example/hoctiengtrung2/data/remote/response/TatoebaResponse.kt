package com.example.hoctiengtrung2.data.remote.response

import com.google.gson.annotations.SerializedName

data class TatoebaResponse(
    @SerializedName("data") val data: List<TatoebaSentence>? = null
)

data class TatoebaSentence(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("translations") val translations: List<TatoebaTranslation>? = null
)

data class TatoebaTranslation(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("lang") val lang: String? = null
)
