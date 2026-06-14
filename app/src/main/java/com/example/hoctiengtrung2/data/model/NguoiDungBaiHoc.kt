package com.example.hoctiengtrung2.data.model

data class NguoiDungBaiHoc(
    val idNguoiDung: String,
    val idBaiHoc: String,
    val trangThai: String,
    val tienDo: Int = 0
)