package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class HomeRemoteDataSource {
    private val db = FirebaseFirestore.getInstance()

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

    suspend fun getTatCaTuVung(): List<TuVung> {
        return db.collection("TuVung").get().await().toObjects(TuVung::class.java)
    }

    suspend fun getTienDoTuVungNguoiDung(idND: String): List<NguoiDungTuVung> {
        return db.collection("NguoiDungTuVung")
            .whereEqualTo("idNguoiDung", idND)
            .get()
            .await()
            .toObjects(NguoiDungTuVung::class.java)
    }

    suspend fun getBaiHocHoanThanh(idND: String): List<String> {
        val query = db.collection("NguoiDungBaiHoc")
            .whereEqualTo("idNguoiDung", idND)
            .whereEqualTo("trangThai", "Hoàn thành")
            .get().await()
        return query.documents.mapNotNull { it.getString("idBaiHoc") }
    }

    suspend fun getThongKeTuVung(idND: String): Map<String, Int> {
        val user = getNguoiDung(idND)
        val querySnapshot = db.collection("NguoiDungTuVung")
            .whereEqualTo("idNguoiDung", idND)
            .get().await()

        val lichSuToanBo = getThongKeHoatDong(idND, days = 7)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hoatDongHomNay = lichSuToanBo.find { it.ngay == todayStr }
        
        val tuHomNay = (hoatDongHomNay?.soTuMoi ?: 0)
        val tuTrongTuan = lichSuToanBo.sumOf { it.soTuMoi }
        
        var tongTu = user?.tongTuDaHoc ?: 0
        if (tongTu == 0) {
            val querySnapshot = db.collection("LichSuHoatDong")
                .whereEqualTo("idNguoiDung", idND)
                .get().await()
            if (!querySnapshot.isEmpty) {
                tongTu = querySnapshot.toObjects(LichSuHoatDong::class.java)
                    .maxByOrNull { it.ngay }?.tongtudahoc ?: 0
            }
        }

        val dsDaHoc = querySnapshot.documents.filter { it.getBoolean("daHoc") == true }
        val bayGio = Date()
        var soTuCanOnTap = 0
        var soFlashcardCanOnTap = 0
        
        dsDaHoc.forEach { doc ->
            val ngayOnTap = doc.getTimestamp("ngayOnTapTiepTheo")?.toDate()
            if (ngayOnTap == null || ngayOnTap.before(bayGio)) {
                if (doc.getBoolean("isFlashcardCaNhan") == true) {
                    soFlashcardCanOnTap++
                } else {
                    soTuCanOnTap++
                }
            }
        }

        return mapOf(
            "tongTu" to tongTu,
            "tuHomNay" to tuHomNay,
            "tuTrongTuan" to tuTrongTuan,
            "soTuCanOnTap" to (soTuCanOnTap + soFlashcardCanOnTap),
            "soFlashcardCanOnTap" to soFlashcardCanOnTap
        )
    }

    suspend fun capNhatLichSuHoatDong(
        idND: String,
        tuMoi: Int = 0,
        tuOnTap: Int = 0
    ) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val docId = "${idND}_${today}"
        val userRef = db.collection("NguoiDung").document(idND)
        val historyRef = db.collection("LichSuHoatDong").document(docId)

        val pastTotal = try {
            val querySnapshot = db.collection("LichSuHoatDong")
                .whereEqualTo("idNguoiDung", idND)
                .get()
                .await()
            val pastHistories = querySnapshot.toObjects(LichSuHoatDong::class.java)
            val nearestPast = pastHistories
                .filter { it.ngay < today }
                .maxByOrNull { it.ngay }
            nearestPast?.tongtudahoc ?: 0
        } catch (e: Exception) {
            0
        }

        try {
            val userSnapshot = userRef.get().await()
            val historySnapshot = historyRef.get().await()
            
            val currentTotal = userSnapshot.getLong("tongTuDaHoc") ?: 0L
            val newTotal = currentTotal + tuMoi

            userRef.update("tongTuDaHoc", newTotal).await()

            if (!historySnapshot.exists()) {
                val data = mutableMapOf(
                    "idHoatDong" to docId,
                    "idNguoiDung" to idND,
                    "ngay" to today,
                    "soTuMoi" to tuMoi.toLong(),
                    "soTuOnTap" to tuOnTap.toLong(),
                    "tongtudahoc" to (pastTotal + tuMoi).toLong()
                )
                historyRef.set(data).await()
            } else {
                val currentTodayNewWords = historySnapshot.getLong("soTuMoi") ?: 0L
                val newSoTuMoi = currentTodayNewWords + tuMoi
                
                historyRef.update(mapOf(
                    "soTuMoi" to newSoTuMoi,
                    "soTuOnTap" to com.google.firebase.firestore.FieldValue.increment(tuOnTap.toLong()),
                    "tongtudahoc" to (pastTotal + newSoTuMoi).toLong()
                )).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getThongKeHoatDong(idND: String, days: Int = 7): List<LichSuHoatDong> {
        return try {
            val querySnapshot = db.collection("LichSuHoatDong")
                .whereEqualTo("idNguoiDung", idND)
                .get().await()
            
            val list = querySnapshot.toObjects(LichSuHoatDong::class.java)
            val map = list.associateBy { it.ngay }
            
            val result = mutableListOf<LichSuHoatDong>()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            for (i in (days - 1) downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = sdf.format(cal.time)
                
                val item = map[dateStr] ?: LichSuHoatDong(
                    idHoatDong = "${idND}_${dateStr}",
                    idNguoiDung = idND,
                    ngay = dateStr,
                    soTuMoi = 0,
                    soTuOnTap = 0,
                    tongtudahoc = 0
                )
                result.add(item)
            }
            
            var lastKnownTotal = 0
            val allSorted = list.sortedBy { it.ngay }
            
            for (i in result.indices) {
                if (result[i].tongtudahoc == 0) {
                    val nearestPast = allSorted.filter { it.ngay < result[i].ngay }.maxByOrNull { it.ngay }
                    result[i] = result[i].copy(tongtudahoc = nearestPast?.tongtudahoc ?: lastKnownTotal)
                }
                lastKnownTotal = result[i].tongtudahoc
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBaiHocDeXuat(idND: String): List<BaiHoc> {
        val bhHoanThanh = getBaiHocHoanThanh(idND)
        val tatCaBaiHoc = db.collection("BaiHoc").get().await().toObjects(BaiHoc::class.java)

        return tatCaBaiHoc
            .filter { it.idBaiHoc !in bhHoanThanh }
            .take(3)
    }

    suspend fun getLichSuHoc(idND: String): Map<String, Int> {
        return try {
            val querySnapshot = db.collection("NguoiDungTuVung")
                .whereEqualTo("idNguoiDung", idND)
                .get().await()

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lichSu = mutableMapOf<String, Int>()

            querySnapshot.documents.forEach { doc ->
                val timestamp = doc.getTimestamp("ngayHocLanDau")?.toDate()
                if (timestamp != null) {
                    val dateKey = sdf.format(timestamp)
                    lichSu[dateKey] = (lichSu[dateKey] ?: 0) + 1
                }
            }
            lichSu
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getThongKeHSK(idND: String): Map<String, Map<String, Int>> {
        return try {
            val userWords = db.collection("NguoiDungTuVung")
                .whereEqualTo("idNguoiDung", idND)
                .get().await().toObjects(NguoiDungTuVung::class.java)
                .filter { it.ngayHocLanDau != null }

            if (userWords.isEmpty()) return emptyMap()

            val allTuVung = db.collection("TuVung").get().await().toObjects(TuVung::class.java)
            val allBaiHoc = db.collection("BaiHoc").get().await().toObjects(BaiHoc::class.java)
            val allCapDo = db.collection("CapDo").get().await().toObjects(CapDo::class.java)

            val tuVungToBaiHoc = allTuVung.associate { it.idTuVung to it.idBaiHoc }
            val baiHocToCapDo = allBaiHoc.associate { it.idBaiHoc to it.idCapDo }
            val capDoMap = allCapDo.associate { it.idCapDo to it.tenCapDo }

            val calendar = Calendar.getInstance()
            val startOf7Days = Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_YEAR, -6)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            val startOfMonth = Calendar.getInstance().apply { 
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time

            val result = mutableMapOf(
                "7 ngày qua" to mutableMapOf<String, Int>(),
                "Tháng này" to mutableMapOf<String, Int>(),
                "Tất cả" to mutableMapOf<String, Int>()
            )

            userWords.forEach { userWord ->
                val idBaiHoc = tuVungToBaiHoc[userWord.idTuVung] ?: return@forEach
                val idCapDo = baiHocToCapDo[idBaiHoc] ?: return@forEach
                val tenCapDo = capDoMap[idCapDo] ?: "Khác"
                val date = userWord.ngayHocLanDau?.toDate() ?: return@forEach

                result["Tất cả"]!![tenCapDo] = (result["Tất cả"]!![tenCapDo] ?: 0) + 1
                
                if (!date.before(startOf7Days)) {
                    result["7 ngày qua"]!![tenCapDo] = (result["7 ngày qua"]!![tenCapDo] ?: 0) + 1
                }

                if (!date.before(startOfMonth)) {
                    result["Tháng này"]!![tenCapDo] = (result["Tháng này"]!![tenCapDo] ?: 0) + 1
                }
            }

            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun taoDuLieuMau(idND: String) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val random = Random()
        val calendar = Calendar.getInstance()
        
        val user = getNguoiDung(idND)
        var currentTotal = user?.tongTuDaHoc ?: 150
        
        for (i in 1..6) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val ngayStr = sdf.format(calendar.time)
            val docId = "${idND}_${ngayStr}"
            
            val docRef = db.collection("LichSuHoatDong").document(docId)
            val snapshot = docRef.get().await()
            
            if (!snapshot.exists()) {
                val soTuMoi = random.nextInt(10) + 5
                val soTuOnTap = random.nextInt(20) + 10
                
                currentTotal -= soTuMoi
                if (currentTotal < 0) currentTotal = 0
                
                val data = hashMapOf(
                    "idHoatDong" to docId,
                    "idNguoiDung" to idND,
                    "ngay" to ngayStr,
                    "soTuMoi" to soTuMoi,
                    "soTuOnTap" to soTuOnTap,
                    "tongtudahoc" to currentTotal
                )
                docRef.set(data).await()
            }
        }
    }
}
