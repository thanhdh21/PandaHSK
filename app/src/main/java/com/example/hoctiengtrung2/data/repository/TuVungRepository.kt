package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.remote.TuVungRemoteDataSource

class TuVungRepository(
    private val remoteDataSource: TuVungRemoteDataSource = TuVungRemoteDataSource(),
    private val homeRemoteDataSource: HomeRemoteDataSource = HomeRemoteDataSource()
) {

    suspend fun layDanhSachTuVung(idBaiHoc: String): List<TuVung> {
        return remoteDataSource.layDanhSachTuVungTheoBaiHoc(idBaiHoc)
    }

    suspend fun layDanhSachTuVungCanOnTap(idNguoiDung: String): List<TuVung> {
        return remoteDataSource.layDanhSachTuVungCanOnTap(idNguoiDung)
    }

    suspend fun capNhatTienDoTuVung(idNguoiDung: String, idTuVung: String, laDung: Boolean, tuTracNghiem: Boolean = false, isReview: Boolean = false) {
        remoteDataSource.capNhatTienDoTuVung(idNguoiDung, idTuVung, laDung, tuTracNghiem, isReview)
    }

    suspend fun hoanThanhTuVung(idNguoiDung: String, idTuVung: String, laTuMoi: Boolean = false) {
        remoteDataSource.capNhatTienDoTuVung(idNguoiDung, idTuVung, laDung = true, tuTracNghiem = false, laTuMoi = laTuMoi)
    }

    suspend fun hoanThanhBaiHoc(idNguoiDung: String, idBaiHoc: String) {
        remoteDataSource.capNhatTienDoBaiHoc(idNguoiDung, idBaiHoc, "Hoàn thành")
    }

    suspend fun capNhatStreak(idNguoiDung: String) {
        remoteDataSource.capNhatStreak(idNguoiDung)
    }

    suspend fun taoFlashcardCaNhan(flashcard: com.example.hoctiengtrung2.data.model.FlashcardCaNhan): Boolean {
        return remoteDataSource.taoFlashcardCaNhan(flashcard)
    }

    suspend fun layDanhSachFlashcardCaNhan(idNguoiDung: String): List<com.example.hoctiengtrung2.data.model.FlashcardCaNhan> {
        return remoteDataSource.layDanhSachFlashcardCaNhan(idNguoiDung)
    }

    suspend fun xoaFlashcardCaNhan(idFlashcard: String): Boolean {
        return remoteDataSource.xoaFlashcardCaNhan(idFlashcard)
    }

    suspend fun xoaDanhSachFlashcardCaNhan(idFlashcards: List<String>): Boolean {
        return remoteDataSource.xoaDanhSachFlashcardCaNhan(idFlashcards)
    }

    suspend fun suaFlashcardCaNhan(idFlashcard: String, hanTu: String, pinyin: String, nghia: String): Boolean {
        return remoteDataSource.suaFlashcardCaNhan(idFlashcard, hanTu, pinyin, nghia)
    }

    suspend fun layTatCaTuVung(): List<TuVung> {
        return homeRemoteDataSource.getTatCaTuVung()
    }

    suspend fun capNhatLichSuHoatDong(
        idND: String,
        tuMoi: Int = 0,
        tuOnTap: Int = 0
    ) {
        homeRemoteDataSource.capNhatLichSuHoatDong(idND, tuMoi, tuOnTap)
    }
}
