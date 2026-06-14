package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.repository.TuVungRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TuVungUiState {
    object DangTai : TuVungUiState()
    data class ThanhCong(val danhSach: List<TuVung>) : TuVungUiState()
    data class LoI(val thongBao: String) : TuVungUiState()
}

class TuVungViewModel(private val repository: TuVungRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TuVungUiState>(TuVungUiState.DangTai)
    val uiState: StateFlow<TuVungUiState> = _uiState

    fun layDanhSachTuVung(idBaiHoc: String, idNguoiDung: String = "") {
        viewModelScope.launch {
            _uiState.value = TuVungUiState.DangTai
            try {
                val list = if (idBaiHoc.startsWith("review")) {
                    val allReviewWords = repository.layDanhSachTuVungCanOnTap(idNguoiDung)
                    if (idBaiHoc.contains("_")) {
                        val parts = idBaiHoc.split("_")
                        val index = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val start = (index - 1) * 20
                        val end = (start + 20).coerceAtMost(allReviewWords.size)
                        if (start < allReviewWords.size) {
                            allReviewWords.subList(start, end)
                        } else {
                            emptyList()
                        }
                    } else {
                        allReviewWords
                    }
                } else {
                    repository.layDanhSachTuVung(idBaiHoc)
                }
                if (list.isEmpty()) {
                    _uiState.value = TuVungUiState.LoI(
                        if (idBaiHoc == "review") "Hôm nay bạn không có từ nào cần ôn tập!"
                        else "Không có từ vựng nào trong bài học này"
                    )
                } else {
                    _uiState.value = TuVungUiState.ThanhCong(list)
                }
            } catch (e: Exception) {
                _uiState.value = TuVungUiState.LoI(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun hoanThanhTuVung(idNguoiDung: String, idTuVung: String, laTuMoi: Boolean) {
        viewModelScope.launch {
            repository.hoanThanhTuVung(idNguoiDung, idTuVung, laTuMoi)
        }
    }
}
