package com.example.hoctiengtrung2.data.model

data class NguoiDung(
    val idNguoiDung: String = "",
    val idTaiKhoan: String = "",
    val tenNguoiDung: String = "",
    val tuoi: Int = 0,
    val idCapDo: String = "",
    val streak: Int = 0,
    val target: Int = 0,

    // BẮT BUỘC SỬA THÀNH CÓ GIÁ TRỊ MẶC ĐỊNH NHƯ NÀY:
    val chuoiNgay: Int = 0,
    val ngayDaHoc: List<String> = emptyList()
)