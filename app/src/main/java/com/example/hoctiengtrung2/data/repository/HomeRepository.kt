package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.*
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource

data class HomeData(
    val nguoiDung: NguoiDung,
    val capDo: CapDo,
    val tongTuCapDo: Int,
    val tongTuDaHoc: Int,
    val tuHocHomNay: Int,
    val tuHoc7Ngay: Int,
    val baiHocDeXuat: List<BaiHoc>,
    val soTuCanOnTap: Int,
    val soFlashcardCanOnTap: Int = 0,
    val lichSuHoc: Map<String, Int>,
    val thongKeHSK: Map<String, Map<String, Int>>,
    val lichSuHoatDong: List<LichSuHoatDong>
)

class HomeRepository(private val remoteDataSource: HomeRemoteDataSource) {

    suspend fun getHomeData(idND: String): HomeData? {
        val nguoiDung = remoteDataSource.getNguoiDung(idND) ?: return null
        val capDo = remoteDataSource.getCapDo(nguoiDung.idCapDo)?: CapDo(tenCapDo = "HSK1", soLuongTu = 150)
        val baiHocDeXuat = remoteDataSource.getBaiHocDeXuat(idND)
        val thongKe = remoteDataSource.getThongKeTuVung(idND)
        val lichSu = remoteDataSource.getLichSuHoc(idND)
        val thongKeHSK = remoteDataSource.getThongKeHSK(idND)
        val lichSuMoi = remoteDataSource.getThongKeHoatDong(idND, days = 365)

        return HomeData(
            nguoiDung = nguoiDung,
            capDo = capDo,
            tongTuCapDo = capDo.soLuongTu,
            tongTuDaHoc = thongKe["tongTu"] ?: 0,
            tuHocHomNay = thongKe["tuHomNay"] ?: 0,
            tuHoc7Ngay = thongKe["tuTrongTuan"] ?: 0,
            baiHocDeXuat = baiHocDeXuat,
            soTuCanOnTap = thongKe["soTuCanOnTap"] ?: 0,
            soFlashcardCanOnTap = thongKe["soFlashcardCanOnTap"] ?: 0,
            lichSuHoc = lichSu,
            thongKeHSK = thongKeHSK,
            lichSuHoatDong = lichSuMoi
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

    suspend fun getTatCaTuVung(): List<TuVung> {
        return remoteDataSource.getTatCaTuVung()
    }

    suspend fun getTienDoTuVungNguoiDung(idND: String): List<NguoiDungTuVung> {
        return remoteDataSource.getTienDoTuVungNguoiDung(idND)
    }
    suspend fun taoDuLieuMau(idND: String) {
        remoteDataSource.taoDuLieuMau(idND)
    }
}
