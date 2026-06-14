package com.example.hoctiengtrung2.data.model

data class NguoiDung(
    val idNguoiDung: String = "",
    val idTaiKhoan: String = "",
    val tenNguoiDung: String = "",
    val idCapDo: String = "",
    val streak: Int = 0,
    val target: Int = 0,
    val tongTuDaHoc: Int = 0,
    val ngayCapNhatStreakCuoi: java.util.Date? = null
)