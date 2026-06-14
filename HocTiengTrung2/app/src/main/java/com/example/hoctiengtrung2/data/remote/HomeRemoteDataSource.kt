package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class HomeRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getLichSuHoc(idNguoiDung: String): DocumentSnapshot? {
        return try {
            db.collection("NguoiDung").document(idNguoiDung).get().await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getNguoiDung(idND: String): NguoiDung? {
        return db.collection("NguoiDung").document(idND).get().await().toObject(NguoiDung::class.java)
    }

    suspend fun getCapDo(idCapDo: String): CapDo? {
        return db.collection("CapDo").document(idCapDo).get().await().toObject(CapDo::class.java)
    }

    suspend fun getTatCaCapDo(): List<CapDo> {
        return db.collection("CapDo").get().await().toObjects(CapDo::class.java)
    }

    suspend fun getTatCaBaiHoc(): List<BaiHoc> {
        return db.collection("BaiHoc").get().await().toObjects(BaiHoc::class.java)
    }

    suspend fun getBaiHocHoanThanh(idND: String): List<String> {
        val query = db.collection("NguoiDungBaiHoc")
            .whereEqualTo("idNguoiDung", idND)
            .whereEqualTo("trangThai", "Hoàn thành")
            .get().await()
        return query.documents.mapNotNull { it.getString("idBaiHoc") }
    }

    suspend fun getThongKeTuVung(idND: String): Map<String, Int> {
        // Chỉ lọc theo idNguoiDung để tránh phải tạo Index phức tạp trên Firestore
        val querySnapshot = db.collection("NguoiDungTuVung")
            .whereEqualTo("idNguoiDung", idND)
            .get().await()

        // Lọc những từ đã học (daHoc == true) bằng code Kotlin
        val dsTuDaHoc = querySnapshot.documents.filter { it.getBoolean("daHoc") == true }
        
        val tongTu = dsTuDaHoc.size

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.time

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.time

        var tuHomNay = 0
        var tuTrongTuan = 0

        dsTuDaHoc.forEach { doc ->
            val timestamp = doc.getTimestamp("ngayHocLanDau")?.toDate()
            if (timestamp != null) {
                if (timestamp.after(startOfToday)) tuHomNay++
                if (timestamp.after(startOfWeek)) tuTrongTuan++
            }
        }

        return mapOf(
            "tongTu" to tongTu,
            "tuHomNay" to tuHomNay,
            "tuTrongTuan" to tuTrongTuan
        )
    }

    suspend fun capNhatStreak(idND: String) {
        val userRef = db.collection("NguoiDung").document(idND)
        val snapshot = userRef.get().await()
        if (snapshot.exists()) {
            val currentStreak = snapshot.getLong("streak") ?: 0
            userRef.update("streak", currentStreak + 1).await()
        }
    }
    
    suspend fun capNhatCapDoNguoiDung(idND: String, idCapDo: String) {
        db.collection("NguoiDung").document(idND).update("idCapDo", idCapDo).await()
    }
    suspend fun getBaiHocDeXuat(idND: String): List<BaiHoc> {
        val bhHoanThanh = getBaiHocHoanThanh(idND)
        val tatCaBaiHoc = db.collection("BaiHoc").get().await().toObjects(BaiHoc::class.java)

        return tatCaBaiHoc
            .filter { it.idBaiHoc !in bhHoanThanh }
            .take(3)
    }
}
