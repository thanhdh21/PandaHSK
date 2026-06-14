package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.TraCuuResult
import com.example.hoctiengtrung2.data.repository.TraCuuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TraCuuUiState {
    object Cho : TraCuuUiState()
    object DangTai : TraCuuUiState()
    data class ThanhCong(val ketQua: TraCuuResult) : TraCuuUiState()
    data class Loi(val thongBao: String) : TraCuuUiState()
}

class TraCuuViewModel(private val repository: TraCuuRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TraCuuUiState>(TraCuuUiState.Cho)
    val uiState: StateFlow<TraCuuUiState> = _uiState

    fun traCuu(tu: String) {
        if (tu.isBlank()) return

        viewModelScope.launch {
            _uiState.value = TraCuuUiState.DangTai
            try {
                val result = repository.traCuuTu(tu)
                if (result != null) {
                    _uiState.value = TraCuuUiState.ThanhCong(result)
                } else {
                    _uiState.value = TraCuuUiState.Loi("Không tìm thấy kết quả")
                }
            } catch (e: Exception) {
                _uiState.value = TraCuuUiState.Loi("Lỗi kết nối, vui lòng thử lại")
            }
        }
    }
}

