package com.example.hoctiengtrung2.utils

import android.util.Log
import com.example.hoctiengtrung2.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object SeedingUtils {
    suspend fun taoTaiKhoanTestVaSeedDuLieu() {
        val db = FirebaseFirestore.getInstance()
        try {
            // Check if testaccount already exists
            val query = db.collection("TaiKhoan").whereEqualTo("tenDangNhap", "testaccount").get().await()
            var idND = ""
            if (!query.isEmpty) {
                val idTK = query.documents[0].id
                val ndQuery = db.collection("NguoiDung").whereEqualTo("idTaiKhoan", idTK).get().await()
                if (!ndQuery.isEmpty) {
                    idND = ndQuery.documents[0].id
                    Log.d("SEEDING", "Test account exists with idNguoiDung: $idND")
                }
            }

            if (idND.isEmpty()) {
                // Register new test account
                val tkRef = db.collection("TaiKhoan").document()
                val idTK = tkRef.id
                val taiKhoanData = TaiKhoan(idTaiKhoan = idTK, tenDangNhap = "testaccount", matKhau = "123456", quyen = "user")
                tkRef.set(taiKhoanData).await()

                val ndRef = db.collection("NguoiDung").document()
                idND = ndRef.id
                val nguoiDungData = NguoiDung(
                    idNguoiDung = idND,
                    idTaiKhoan = idTK,
                    tenNguoiDung = "Tài khoản Test",
                    idCapDo = "1",
                    streak = 0,
                    target = 10,
                    tongTuDaHoc = 0
                )
                ndRef.set(nguoiDungData).await()
                Log.d("SEEDING", "Created new test account: testaccount / 123456, idND: $idND")
            }

            // Seed HSK1 Lesson 1 learned yesterday for this user
            taoDuLieuTestOnTap(idND)
        } catch (e: Exception) {
            Log.e("SEEDING", "Failed to create/seed test account: ${e.message}", e)
        }
    }

    suspend fun taoDuLieuTestOnTap(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) {
            Log.d("SEEDING", "User ID is blank, cannot seed test data.")
            return
        }
        try {
            val db = FirebaseFirestore.getInstance()

            // 1. Tính toán mốc thời gian hôm qua
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val homQua = cal.time
            val homQuaStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(homQua)
            val timestampHomQua = com.google.firebase.Timestamp(homQua)

            Log.d("SEEDING", "Starting seed for user: $idNguoiDung, date: $homQuaStr")

            // 2. Lấy hoặc tạo bài học HSK1 Bài 1
            var lessonId = "lesson_hsk1_1"
            val lessonsSnapshot = db.collection("BaiHoc")
                .whereEqualTo("idCapDo", "1")
                .limit(1)
                .get()
                .await()

            if (!lessonsSnapshot.isEmpty) {
                lessonId = lessonsSnapshot.documents[0].id
                Log.d("SEEDING", "Found existing HSK1 Lesson 1 ID: $lessonId")
            } else {
                // Tạo bài học mẫu nếu chưa có
                val sampleLesson = hashMapOf(
                    "idBaiHoc" to lessonId,
                    "tenBaiHoc" to "Bài 1: Chào hỏi",
                    "moTa" to "Học các từ vựng chào hỏi cơ bản trong tiếng Trung",
                    "idCapDo" to "1"
                )
                db.collection("BaiHoc").document(lessonId).set(sampleLesson).await()
                Log.d("SEEDING", "Created sample HSK1 Lesson 1 in BaiHoc.")
            }

            // 3. Lấy hoặc tạo 5 từ vựng cho Bài 1 HSK1
            val wordsSnapshot = db.collection("TuVung")
                .whereEqualTo("idBaiHoc", lessonId)
                .limit(5)
                .get()
                .await()

            val listTuVung = mutableListOf<TuVung>()
            if (!wordsSnapshot.isEmpty && wordsSnapshot.size() >= 5) {
                listTuVung.addAll(wordsSnapshot.toObjects(TuVung::class.java).take(5))
                Log.d("SEEDING", "Using 5 existing vocabulary words from Lesson 1.")
            } else {
                // Tạo 5 từ vựng mẫu cho Bài 1
                val sampleWords = listOf(
                    TuVung(idTuVung = "tv_h1_1_1", hanTu = "我", pinyin = "wǒ", nghia = "Tôi", idBaiHoc = lessonId, idLoaiTu = "1"),
                    TuVung(idTuVung = "tv_h1_1_2", hanTu = "你", pinyin = "nǐ", nghia = "Bạn", idBaiHoc = lessonId, idLoaiTu = "1"),
                    TuVung(idTuVung = "tv_h1_1_3", hanTu = "好", pinyin = "hǎo", nghia = "Tốt, khỏe", idBaiHoc = lessonId, idLoaiTu = "2"),
                    TuVung(idTuVung = "tv_h1_1_4", hanTu = "谢谢", pinyin = "xièxie", nghia = "Cảm ơn", idBaiHoc = lessonId, idLoaiTu = "2"),
                    TuVung(idTuVung = "tv_h1_1_5", hanTu = "再见", pinyin = "zàijiàn", nghia = "Tạm biệt", idBaiHoc = lessonId, idLoaiTu = "2")
                )

                val wordBatch = db.batch()
                sampleWords.forEach { word ->
                    wordBatch.set(db.collection("TuVung").document(word.idTuVung), word)
                }
                wordBatch.commit().await()
                listTuVung.addAll(sampleWords)
                Log.d("SEEDING", "Seeded 5 sample vocabulary words for Lesson 1 into TuVung.")
            }

            // 4. Batch ghi nhận dữ liệu đã học hôm qua để đến hạn ôn tập vào hôm nay
            val batch = db.batch()

            // A. Đánh dấu 5 từ đã học (daHoc = true) vào hôm qua
            listTuVung.forEach { tuVung ->
                val progressId = "${idNguoiDung}_${tuVung.idTuVung}"
                val docRef = db.collection("NguoiDungTuVung").document(progressId)
                val progressData = hashMapOf(
                    "idNguoiDung" to idNguoiDung,
                    "idTuVung" to tuVung.idTuVung,
                    "daHoc" to true,
                    "ngayHocLanDau" to timestampHomQua,
                    "soLanDung" to 1L,
                    "soLanSai" to 0L,
                    "repetitions" to 1L,
                    "interval" to 1L,
                    "easinessFactor" to 2.5,
                    "ngayOnTapTiepTheo" to timestampHomQua // Ngày ôn tập tiếp theo là hôm qua (đã đến hạn ôn tập)
                )
                batch.set(docRef, progressData)
            }

            // B. Đánh dấu bài học đã hoàn thành hôm qua
            val userLessonId = "${idNguoiDung}_${lessonId}"
            val userLessonRef = db.collection("NguoiDungBaiHoc").document(userLessonId)
            val userLessonData = hashMapOf(
                "idNguoiDung" to idNguoiDung,
                "idBaiHoc" to lessonId,
                "trangThai" to "Hoàn thành",
                "ngayHoanThanh" to timestampHomQua
            )
            batch.set(userLessonRef, userLessonData)

            // C. Tạo lịch sử hoạt động ngày hôm qua (soTuMoi = 5)
            val historyId = "${idNguoiDung}_${homQuaStr}"
            val historyRef = db.collection("LichSuHoatDong").document(historyId)
            val historyData = hashMapOf(
                "idHoatDong" to historyId,
                "idNguoiDung" to idNguoiDung,
                "ngay" to homQuaStr,
                "soTuMoi" to 5L,
                "soTuOnTap" to 0L,
                "tongtudahoc" to 5L
            )
            batch.set(historyRef, historyData)

            // D. Cập nhật thông tin tổng số từ đã học trong bảng NguoiDung
            val userRef = db.collection("NguoiDung").document(idNguoiDung)
            batch.update(userRef, mapOf(
                "tongTuDaHoc" to 5L,
                "idCapDo" to "1" // Đảm bảo người dùng ở trình độ HSK1
            ))

            batch.commit().await()
            Log.d("SEEDING", "Seeding complete! 5 words marked as learned yesterday for user $idNguoiDung.")
        } catch (e: Exception) {
            Log.e("SEEDING", "Seeding failed: ${e.message}", e)
        }
    }
}
