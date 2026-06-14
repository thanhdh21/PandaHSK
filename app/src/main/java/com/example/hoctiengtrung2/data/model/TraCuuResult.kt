package com.example.hoctiengtrung2.data.model
data class TraCuuResult(
    val query: String,
    val nghia: String,
    val loaiTu: String?,
    val hanViet: String?,
    val cauLienQuan: List<TatoebaSentencePair>
)

data class TatoebaSentencePair(
    val original: String,
    val translation: String
)
