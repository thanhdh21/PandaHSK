package com.example.hoctiengtrung2.data.model

import com.google.firebase.Timestamp

data class NguoiDungTuVung(
    val idNguoiDung: String = "",
    val idTuVung: String = "",
    val ngayHocLanDau: Timestamp? = null,
    val daHoc: Boolean = false,
    val soLanDung: Int = 0,
    val soLanSai: Int = 0
)
