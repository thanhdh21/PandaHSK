package com.example.hoctiengtrung2.data.remote

import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.model.LichSuHoatDong
import com.example.hoctiengtrung2.utils.SM2Algorithm
import com.example.hoctiengtrung2.utils.SM2State
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    suspend fun capNhatTienDoTuVung(
        idNguoiDung: String,
        idTuVung: String,
        laDung: Boolean,
        tuTracNghiem: Boolean = false,
        isReview: Boolean = false,
        laTuMoi: Boolean = false
    ) {
        val documentId = "${idNguoiDung}_${idTuVung}"
        val docRef = db.collection("NguoiDungTuVung").document(documentId)
        val userRef = db.collection("NguoiDung").document(idNguoiDung)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val historyId = "${idNguoiDung}_${today}"
        val historyRef = db.collection("LichSuHoatDong").document(historyId)

        val pastTotal = try {
            val querySnapshot = db.collection("LichSuHoatDong")
                .whereEqualTo("idNguoiDung", idNguoiDung)
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
            val snapshot = docRef.get().await()
            val userSnapshot = userRef.get().await()
            val historySnapshot = historyRef.get().await()
            val bayGio = com.google.firebase.Timestamp.now()

            var isNewLearned = false
            var isNewReviewed = false

            val currentTotal = userSnapshot.getLong("tongTuDaHoc") ?: 0L

            if (!snapshot.exists()) {
                val laTracNghiemDungChuaHoc = !isReview && laDung && tuTracNghiem
                val daHocMoi = if (tuTracNghiem) laDung else laTuMoi
                
                if (laTracNghiemDungChuaHoc) {
                    isNewLearned = true
                }

                val targetNgayOnTap = if (laTracNghiemDungChuaHoc) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    com.google.firebase.Timestamp(cal.time)
                } else {
                    null
                }

                val targetRepetitions = if (laTracNghiemDungChuaHoc) 1 else 0
                val targetInterval = if (laTracNghiemDungChuaHoc) 1 else 0

                val data = hashMapOf<String, Any>(
                    "idNguoiDung" to idNguoiDung,
                    "idTuVung" to idTuVung,
                    "ngayHocLanDau" to bayGio,
                    "soLanDung" to if (tuTracNghiem && laDung) 1L else 0L,
                    "soLanSai" to if (tuTracNghiem && !laDung) 1L else 0L,
                    "daHoc" to daHocMoi,
                    "repetitions" to targetRepetitions,
                    "interval" to targetInterval,
                    "easinessFactor" to 2.5
                )
                if (targetNgayOnTap != null) {
                    data["ngayOnTapTiepTheo"] = targetNgayOnTap
                }
                docRef.set(data).await()
            } else {
                val soLanDung = snapshot.getLong("soLanDung") ?: 0L
                val soLanSai = snapshot.getLong("soLanSai") ?: 0L
                val repetitions = snapshot.getLong("repetitions")?.toInt() ?: 0
                val interval = snapshot.getLong("interval")?.toInt() ?: 0
                val easinessFactor = snapshot.getDouble("easinessFactor") ?: 2.5
                val isFlashcard = snapshot.getBoolean("isFlashcardCaNhan") ?: false

                val daHocCu = snapshot.getBoolean("daHoc") ?: false
                val laTracNghiemDungChuaHoc = !isReview && laDung && tuTracNghiem && soLanDung == 0L
                val laHocMoiChuaHoc = !daHocCu && laTuMoi
                val daHocMoi = if (tuTracNghiem) {
                    laDung
                } else {
                    daHocCu || laTuMoi
                }

                if (laTracNghiemDungChuaHoc) {
                    isNewLearned = true
                }

                if (isReview && laDung && tuTracNghiem) {
                    isNewReviewed = true
                }

                var targetNgayOnTap: com.google.firebase.Timestamp? = null
                var targetRepetitions = 0
                var targetInterval = 0
                var targetEasinessFactor = easinessFactor

                if (tuTracNghiem) {
                    if (laDung) {
                        if (laTracNghiemDungChuaHoc) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            targetNgayOnTap = com.google.firebase.Timestamp(cal.time)
                            targetRepetitions = 1
                            targetInterval = 1
                            targetEasinessFactor = 2.5
                        } else {
                            val sm2State = SM2Algorithm.calculate(
                                laDung = true,
                                prevRepetitions = repetitions,
                                prevInterval = interval,
                                prevEasinessFactor = easinessFactor
                            )
                            targetNgayOnTap = sm2State.ngayOnTapTiepTheo
                            targetRepetitions = sm2State.repetitions
                            targetInterval = sm2State.interval
                            targetEasinessFactor = sm2State.easinessFactor
                        }
                    } else {
                        targetNgayOnTap = null
                        targetRepetitions = 0
                        targetInterval = 0
                        val sm2State = SM2Algorithm.calculate(
                            laDung = false,
                            prevRepetitions = repetitions,
                            prevInterval = interval,
                            prevEasinessFactor = easinessFactor
                        )
                        targetEasinessFactor = sm2State.easinessFactor
                    }
                } else {
                    targetNgayOnTap = if (daHocMoi) {
                        snapshot.getTimestamp("ngayOnTapTiepTheo")
                    } else {
                        null
                    }
                    targetRepetitions = repetitions
                    targetInterval = interval
                    targetEasinessFactor = easinessFactor
                }

                val updates = mutableMapOf<String, Any>(
                    "soLanDung" to if (tuTracNghiem && laDung) soLanDung + 1 else soLanDung,
                    "soLanSai" to if (tuTracNghiem && !laDung) soLanSai + 1 else soLanSai,
                    "repetitions" to targetRepetitions,
                    "interval" to targetInterval,
                    "easinessFactor" to targetEasinessFactor,
                    "daHoc" to daHocMoi
                )
                
                if (targetNgayOnTap != null) {
                    updates["ngayOnTapTiepTheo"] = targetNgayOnTap
                } else {
                    updates["ngayOnTapTiepTheo"] = com.google.firebase.firestore.FieldValue.delete()
                }
                
                docRef.update(updates).await()
            }

            if (isNewLearned || isNewReviewed) {
                val tuMoiTang = if (isNewLearned) 1L else 0L
                val tuOnTapTang = if (isNewReviewed) 1L else 0L
                val newTotal = currentTotal + tuMoiTang

                if (isNewLearned) {
                    userRef.update("tongTuDaHoc", newTotal).await()
                }

                if (!historySnapshot.exists()) {
                    val data = mutableMapOf(
                        "idHoatDong" to historyId,
                        "idNguoiDung" to idNguoiDung,
                        "ngay" to today,
                        "soTuMoi" to tuMoiTang,
                        "soTuOnTap" to tuOnTapTang,
                        "tongtudahoc" to (pastTotal + tuMoiTang)
                    )
                    historyRef.set(data).await()
                } else {
                    val currentTodayNewWords = historySnapshot.getLong("soTuMoi") ?: 0L
                    val newSoTuMoi = currentTodayNewWords + tuMoiTang
                    
                    val updates = mutableMapOf<String, Any>()
                    updates["soTuMoi"] = newSoTuMoi
                    if (isNewReviewed) {
                        updates["soTuOnTap"] = com.google.firebase.firestore.FieldValue.increment(1L)
                    }
                    updates["tongtudahoc"] = pastTotal + newSoTuMoi
                    historyRef.update(updates).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun layDanhSachTuVungCanOnTap(idNguoiDung: String): List<TuVung> {
        return try {
            val querySnapshot = db.collection("NguoiDungTuVung")
                .whereEqualTo("idNguoiDung", idNguoiDung)
                .get()
                .await()

            val bayGio = Date()
            val dsCanOnTap = querySnapshot.documents.filter { doc ->
                val daHoc = doc.getBoolean("daHoc") ?: false
                if (daHoc) {
                    val ngayOnTap = doc.getTimestamp("ngayOnTapTiepTheo")?.toDate()
                    ngayOnTap == null || ngayOnTap.before(bayGio) || ngayOnTap == bayGio
                } else {
                    false
                }
            }

            val idTuVungList = dsCanOnTap.map { 
                val id = it.getString("idTuVung") ?: ""
                val isFlashcard = it.getBoolean("isFlashcardCaNhan") ?: false
                id to isFlashcard
            }.filter { it.first.isNotEmpty() }
            
            if (idTuVungList.isEmpty()) return emptyList()

            coroutineScope {
                val deferreds = idTuVungList.map { (id, isFlashcard) ->
                    async {
                        try {
                            val collection = if (isFlashcard) "FlashcardCaNhan" else "TuVung"
                            val doc = db.collection(collection).document(id).get().await()
                            if (isFlashcard) {
                                val flash = doc.toObject(com.example.hoctiengtrung2.data.model.FlashcardCaNhan::class.java)
                                flash?.let {
                                    TuVung(
                                        idTuVung = it.idFlashcard,
                                        hanTu = it.hanTu,
                                        pinyin = it.pinyin,
                                        nghia = it.nghia,
                                        idBaiHoc = "personal_flashcard"
                                    )
                                }
                            } else {
                                doc.toObject(TuVung::class.java)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                deferreds.awaitAll().filterNotNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
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
        
        try {
            val snapshot = userRef.get().await()
            if (!snapshot.exists()) return

            val currentStreak = snapshot.getLong("streak") ?: 0
            val lastUpdateTimestamp = snapshot.getTimestamp("ngayCapNhatStreakCuoi")
            val lastUpdateDate = lastUpdateTimestamp?.toDate()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val today = calendar.time

            if (lastUpdateDate == null) {
                userRef.update(mapOf(
                    "streak" to 1,
                    "ngayCapNhatStreakCuoi" to com.google.firebase.Timestamp(today)
                )).await()
            } else {
                val diff = today.time - lastUpdateDate.time
                val oneDayMillis = 24 * 60 * 60 * 1000L

                when {
                    diff < oneDayMillis -> {
                    }
                    diff == oneDayMillis -> {
                        userRef.update(mapOf(
                            "streak" to currentStreak + 1,
                            "ngayCapNhatStreakCuoi" to com.google.firebase.Timestamp(today)
                        )).await()
                    }
                    else -> {
                        userRef.update(mapOf(
                            "streak" to 1,
                            "ngayCapNhatStreakCuoi" to com.google.firebase.Timestamp(today)
                        )).await()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun taoFlashcardCaNhan(flashcard: com.example.hoctiengtrung2.data.model.FlashcardCaNhan): Boolean {
        return try {
            val docRef = db.collection("FlashcardCaNhan").document()
            val bayGio = com.google.firebase.Timestamp.now()
            val newFlashcard = flashcard.copy(
                idFlashcard = docRef.id,
                ngayTao = bayGio
            )
            
            db.collection("FlashcardCaNhan").document(docRef.id).set(newFlashcard).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun layDanhSachFlashcardCaNhan(idNguoiDung: String): List<com.example.hoctiengtrung2.data.model.FlashcardCaNhan> {
        return try {
            val snapshot = db.collection("FlashcardCaNhan")
                .whereEqualTo("idNguoiDung", idNguoiDung)
                .orderBy("ngayTao", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(com.example.hoctiengtrung2.data.model.FlashcardCaNhan::class.java)
        } catch (e: Exception) {
            try {
                val snapshot = db.collection("FlashcardCaNhan")
                    .whereEqualTo("idNguoiDung", idNguoiDung)
                    .get()
                    .await()
                snapshot.toObjects(com.example.hoctiengtrung2.data.model.FlashcardCaNhan::class.java)
                    .sortedByDescending { it.ngayTao }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    suspend fun xoaFlashcardCaNhan(idFlashcard: String): Boolean {
        return try {
            db.collection("FlashcardCaNhan").document(idFlashcard).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun xoaDanhSachFlashcardCaNhan(idFlashcards: List<String>): Boolean {
        return try {
            val batch = db.batch()
            idFlashcards.forEach { id ->
                val docRef = db.collection("FlashcardCaNhan").document(id)
                batch.delete(docRef)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun suaFlashcardCaNhan(idFlashcard: String, hanTu: String, pinyin: String, nghia: String): Boolean {
        return try {
            db.collection("FlashcardCaNhan").document(idFlashcard)
                .update(
                    mapOf(
                        "hanTu" to hanTu,
                        "pinyin" to pinyin,
                        "nghia" to nghia
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
