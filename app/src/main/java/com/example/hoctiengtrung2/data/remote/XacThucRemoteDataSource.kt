package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.model.NguoiDung
import com.example.hoctiengtrung2.data.model.TaiKhoan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class XacThucRemoteDataSource {
    //Kết nối đến Firestore
    private val firestore = FirebaseFirestore.getInstance()
    //Tạo tham chiếu đến các collection(table) trong Firestore
    private val taiKhoanCol = firestore.collection("TaiKhoan")
    private val nguoiDungCol = firestore.collection("NguoiDung")

    suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val query = taiKhoanCol.whereEqualTo("tenDangNhap", username).get().await()
            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(taiKhoan: TaiKhoan): NguoiDung? {
        return try {
            val batch = firestore.batch()

            // Tạo DocumentReference để lấy ID trước
            val tkRef = taiKhoanCol.document()
            val ndRef = nguoiDungCol.document()

            val idTK = tkRef.id
            val idND = ndRef.id

            // Cập nhật model với ID thực tế từ Firestore
            val newTaiKhoan = taiKhoan.copy(idTaiKhoan = idTK)
            val newNguoiDung = NguoiDung(
                idNguoiDung = idND,
                idTaiKhoan = idTK,
                tenNguoiDung = "", // Sẽ cập nhật sau ở màn hình thêm thông tin
                idCapDo = "1", // Mặc định là cấp độ 1
                streak = 0,
                target = 10 // Mặc định mục tiêu là 10 từ
            )

            batch.set(tkRef, newTaiKhoan)
            batch.set(ndRef, newNguoiDung)
            batch.commit().await()
            newNguoiDung
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(username: String, mk: String): TaiKhoan? {
        return try {
            val query = taiKhoanCol
                .whereEqualTo("tenDangNhap", username)
                .whereEqualTo("matKhau", mk)
                .get()
                .await()

            query.toObjects(TaiKhoan::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
    suspend fun capNhatThongTin(idNguoiDung: String, ten: String, target: Int): Boolean {
        return try {
            nguoiDungCol.document(idNguoiDung)
                .update(
                    mapOf(
                        "tenNguoiDung" to ten,
                        "target" to target
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun getNguoiDungByTaiKhoan(idTK: String): NguoiDung? {
        return try {
            val query = nguoiDungCol.whereEqualTo("idTaiKhoan", idTK).get().await()
            query.toObjects(NguoiDung::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun doiMatKhau(idTK: String, matKhauMoi: String): Boolean {
        return try {
            taiKhoanCol.document(idTK).update("matKhau", matKhauMoi).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
