package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.remote.TuVungRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.hoctiengtrung2.data.remote.TuVungInternetDto
class TuVungRepository(private val remoteDataSource: TuVungRemoteDataSource = TuVungRemoteDataSource()) {


    private val db = FirebaseFirestore.getInstance()

    suspend fun layDanhSachTuVung(idBaiHoc: String): List<TuVung> {
        return remoteDataSource.layDanhSachTuVungTheoBaiHoc(idBaiHoc)
    }

    suspend fun capNhatTienDoTuVung(idNguoiDung: String, idTuVung: String, laDung: Boolean) {
        remoteDataSource.capNhatTienDoTuVung(idNguoiDung, idTuVung, laDung)
    }

    suspend fun hoanThanhTuVung(idNguoiDung: String, idTuVung: String) {
        remoteDataSource.capNhatTienDoTuVung(idNguoiDung, idTuVung, true)
    }

    suspend fun hoanThanhBaiHoc(idNguoiDung: String, idBaiHoc: String) {
        remoteDataSource.capNhatTienDoBaiHoc(idNguoiDung, idBaiHoc, "Hoàn thành")
    }

    suspend fun capNhatStreak(idNguoiDung: String) {
        remoteDataSource.capNhatStreak(idNguoiDung)
    }

    // =========================================================
    // BỔ SUNG: Hàm lưu từ vựng từ Internet thành Flashcard cá nhân
    // =========================================================
    suspend fun luuFlashcardCaNhan(idNguoiDung: String, chuHan: String, pinyin: String, nghiaViet: String): Boolean {
        return try {
            // Tạo một gói dữ liệu định dạng Map để đẩy lên Firestore
            val flashcard = hashMapOf(
                "idNguoiDung" to idNguoiDung,
                "chuHan" to chuHan,
                "pinyin" to pinyin,
                "nghiaViet" to nghiaViet,
                "ngayTao" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            )

            // Đẩy dữ liệu vào một Collection mới tên là "FlashcardCaNhan"
            db.collection("FlashcardCaNhan").add(flashcard).await()
            true // Trả về true nếu lưu thành công
        } catch (e: Exception) {
            false // Trả về false nếu lỗi (mất mạng, nghẽn mạng...)
        }
    }

    suspend fun layDanhSachFlashcardCaNhan(idNguoiDung: String): List<TuVungInternetDto> {
        return try {
            db.collection("FlashcardCaNhan")
                .whereEqualTo("idNguoiDung", idNguoiDung)
                .get()
                .await()
                .documents.map { doc ->
                    TuVungInternetDto(
                        hanzi = doc.getString("chuHan") ?: "",
                        pinyin = doc.getString("pinyin") ?: "",
                        meaning = doc.getString("nghiaViet") ?: ""
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

