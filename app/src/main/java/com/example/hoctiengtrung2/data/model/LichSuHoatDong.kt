package com.example.hoctiengtrung2.data.model

data class LichSuHoatDong(
    val idHoatDong: String = "",
    val idNguoiDung: String = "",
    val ngay: String = "", // Định dạng yyyy-MM-dd
    val soTuMoi: Int = 0,
    val soTuOnTap: Int = 0,
    val tongtudahoc: Int = 0
)
