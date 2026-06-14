package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.CapDo
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CapDoUiState {
    object DangTai : CapDoUiState()
    data class ThanhCong(val danhSach: List<CapDo>) : CapDoUiState()
    data class LoI(val thongBao: String) : CapDoUiState()
}

class CapDoViewModel : ViewModel() {
    private val repository = HomeRepository(HomeRemoteDataSource())

    private val _uiState = MutableStateFlow<CapDoUiState>(CapDoUiState.DangTai)
    val uiState: StateFlow<CapDoUiState> = _uiState

    init {
        layTatCaCapDo()
    }

    fun layTatCaCapDo() {
        viewModelScope.launch {
            _uiState.value = CapDoUiState.DangTai
            try {
                val list = repository.getTatCaCapDo()
                if (list.isEmpty()) {
                    _uiState.value = CapDoUiState.LoI("Không tìm thấy trình độ nào")
                } else {
                    _uiState.value = CapDoUiState.ThanhCong(list.sortedBy { it.tenCapDo })
                }
            } catch (e: Exception) {
                _uiState.value = CapDoUiState.LoI(e.message ?: "Lỗi khi tải dữ liệu")
            }
        }
    }
}
