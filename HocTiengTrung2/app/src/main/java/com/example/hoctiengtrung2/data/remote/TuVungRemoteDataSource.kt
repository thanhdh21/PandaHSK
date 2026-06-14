package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.model.TuVung
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class TuVungRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun layDanhSachTuVungTheoBaiHoc(idBaiHoc: String): List<TuVung> {
        return try {
            val snapshot = db.collection("TuVung")
                .whereEqualTo("idBaiHoc", idBaiHoc)
                .get()
                .await()
            snapshot.toObjects(TuVung::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun capNhatTienDoTuVung(idNguoiDung: String, idTuVung: String, laDung: Boolean) {
        val documentId = "${idNguoiDung}_${idTuVung}"
        val docRef = db.collection("NguoiDungTuVung").document(documentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val bayGio = com.google.firebase.Timestamp.now()

            if (!snapshot.exists()) {
                // Lần đầu tiên học từ này
                val data = hashMapOf(
                    "idNguoiDung" to idNguoiDung,
                    "idTuVung" to idTuVung,
                    "ngayHocLanDau" to bayGio,
                    "soLanDung" to if (laDung) 1 else 0,
                    "soLanSai" to if (laDung) 0 else 1,
                    "daHoc" to laDung
                )
                transaction.set(docRef, data)
            } else {
                // Cập nhật thông số cơ bản
                val soLanDung = snapshot.getLong("soLanDung") ?: 0
                val soLanSai = snapshot.getLong("soLanSai") ?: 0

                val updates = mutableMapOf<String, Any>(
                    "soLanDung" to if (laDung) soLanDung + 1 else soLanDung,
                    "soLanSai" to if (!laDung) soLanSai + 1 else soLanSai
                )
                
                if (laDung) {
                    updates["daHoc"] = true
                }
                
                transaction.update(docRef, updates)
            }
        }.await()
    }

    suspend fun capNhatTienDoBaiHoc(idNguoiDung: String, idBaiHoc: String, trangThai: String) {
        val documentId = "${idNguoiDung}_${idBaiHoc}"
        val data = hashMapOf(
            "idNguoiDung" to idNguoiDung,
            "idBaiHoc" to idBaiHoc,
            "trangThai" to trangThai,
            "ngayHoanThanh" to com.google.firebase.Timestamp.now()
        )
        db.collection("NguoiDungBaiHoc").document(documentId).set(data).await()
    }

    suspend fun capNhatStreak(idNguoiDung: String) {
        val userRef = db.collection("NguoiDung").document(idNguoiDung)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentStreak = snapshot.getLong("streak") ?: 0
            transaction.update(userRef, "streak", currentStreak + 1)
        }.await()
    }
}
