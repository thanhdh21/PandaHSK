package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.NguoiDung
import com.example.hoctiengtrung2.data.model.TaiKhoan
import com.example.hoctiengtrung2.data.remote.XacThucRemoteDataSource

class XacThucRepository(private val remoteDataSource: XacThucRemoteDataSource) {
    suspend fun checkUsername(username: String) = remoteDataSource.checkUsernameExists(username)
    suspend fun registerUser(taiKhoan: TaiKhoan) = remoteDataSource.register(taiKhoan)
    suspend fun loginUser(username: String, mk: String) = remoteDataSource.login(username, mk)
    suspend fun getProfile(idTK: String) = remoteDataSource.getNguoiDungByTaiKhoan(idTK)
    suspend fun capNhatThongTin(idNguoiDung: String, ten: String, tuoi: Int, target: Int) =
        remoteDataSource.capNhatThongTin(idNguoiDung, ten, tuoi, target)
}