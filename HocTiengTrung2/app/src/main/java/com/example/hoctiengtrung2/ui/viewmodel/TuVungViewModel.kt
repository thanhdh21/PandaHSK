package com.example.hoctiengtrung2.ui.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.repository.TuVungRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
// Thêm vào trong TuVungViewModel.kt của bạn
import androidx.lifecycle.viewModelScope


sealed class TuVungUiState {
    object DangTai : TuVungUiState()
    data class ThanhCong(val danhSach: List<TuVung>) : TuVungUiState()
    data class LoI(val thongBao: String) : TuVungUiState()
}

class TuVungViewModel(private val repository: TuVungRepository = TuVungRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<TuVungUiState>(TuVungUiState.DangTai)
    val uiState: StateFlow<TuVungUiState> = _uiState

    // Hàm nhận thông tin từ giao diện để ra lệnh cho Repository đẩy lên Firebase
    fun tuDongTaoFlashcard(idNguoiDung: String, chuHan: String, pinyin: String, nghiaViet: String, onKetQua: (Boolean) -> Unit) {
        if (idNguoiDung.isBlank()) {
            onKetQua(false)
            return
        }

        viewModelScope.launch {
            val thanhCong = repository.luuFlashcardCaNhan(idNguoiDung, chuHan, pinyin, nghiaViet)
            onKetQua(thanhCong)
        }
    }

    fun layDanhSachTuVung(idBaiHoc: String) {
        viewModelScope.launch {
            _uiState.value = TuVungUiState.DangTai
            try {
                val list = repository.layDanhSachTuVung(idBaiHoc)
                if (list.isEmpty()) {
                    _uiState.value = TuVungUiState.LoI("Không có từ vựng nào trong bài học này")
                } else {
                    _uiState.value = TuVungUiState.ThanhCong(list)
                }
            } catch (e: Exception) {
                _uiState.value = TuVungUiState.LoI(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun hoanThanhTuVung(idNguoiDung: String, idTuVung: String) {
        viewModelScope.launch {
            repository.hoanThanhTuVung(idNguoiDung, idTuVung)
        }
    }
}
