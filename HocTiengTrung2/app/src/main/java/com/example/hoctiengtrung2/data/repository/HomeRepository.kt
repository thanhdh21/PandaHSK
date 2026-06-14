package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.*
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.remote.TuVungApiService
import com.example.hoctiengtrung2.data.remote.TuVungInternetDto
import com.google.firebase.firestore.DocumentSnapshot

data class HomeData(
    val nguoiDung: NguoiDung,
    val capDo: CapDo,
    val tongTuCapDo: Int,
    val tongTuDaHoc: Int,
    val tuHocHomNay: Int,
    val tuHocTrongTuan: Int,
    val baiHocDeXuat: List<BaiHoc>,
)

class HomeRepository(private val remoteDataSource: HomeRemoteDataSource) {
    
    private val apiService = TuVungApiService.create()
    suspend fun layNghinTuTuInternet(tuKhoa: String): List<TuVungInternetDto> {
        val tuKhoaChuan = tuKhoa.trim()
        if (tuKhoaChuan.isEmpty()) return emptyList()
        return apiService.traTuTrenInternet(tuKhoaChuan)
    }

    suspend fun getHomeData(idND: String): HomeData? {
        val nguoiDung = remoteDataSource.getNguoiDung(idND) ?: return null
        val capDo = remoteDataSource.getCapDo(nguoiDung.idCapDo)?: CapDo(tenCapDo = "HSK1", soLuongTu = 150)
        val baiHocDeXuat = remoteDataSource.getBaiHocDeXuat(idND)
        val thongKe = remoteDataSource.getThongKeTuVung(idND)

        return HomeData(
            nguoiDung = nguoiDung,
            capDo = capDo,
            tongTuCapDo = capDo.soLuongTu,
            tongTuDaHoc = thongKe["tongTu"] ?: 0,
            tuHocHomNay = thongKe["tuHomNay"] ?: 0,
            tuHocTrongTuan = thongKe["tuTrongTuan"] ?: 0,
            baiHocDeXuat = baiHocDeXuat,
        )
    }

    suspend fun getTatCaCapDo(): List<CapDo> {
        return remoteDataSource.getTatCaCapDo()
    }

    suspend fun getBaiHocDeXuat(idND: String): List<BaiHoc> {
        return remoteDataSource.getBaiHocDeXuat(idND)
    }
    suspend fun getTatCaBaiHoc(): List<BaiHoc> {
        return remoteDataSource.getTatCaBaiHoc()
    }

    suspend fun layLichSuHoc(idNguoiDung: String): DocumentSnapshot? {
        return remoteDataSource.getLichSuHoc(idNguoiDung)
    }}