package com.example.hoctiengtrung2.data.model

import com.google.firebase.Timestamp

data class FlashcardCaNhan(
    val idFlashcard: String = "",
    val idNguoiDung: String = "",
    val hanTu: String = "",
    val pinyin: String = "",
    val nghia: String = "",
    val ngayTao: Timestamp? = null
)
