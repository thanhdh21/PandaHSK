package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.BaiHoc
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BaiHocUiState {
    object DangTai : BaiHocUiState()
    data class ThanhCong(val danhSach: List<BaiHoc>) : BaiHocUiState()
    data class LoI(val thongBao: String) : BaiHocUiState()
}

class BaiHocViewModel : ViewModel() {
    private val repository = HomeRepository(HomeRemoteDataSource())

    private val _uiState = MutableStateFlow<BaiHocUiState>(BaiHocUiState.DangTai)
    val uiState: StateFlow<BaiHocUiState> = _uiState.asStateFlow()

    fun layDanhSachBaiHoc(idCapDo: String) {
        viewModelScope.launch {
            _uiState.value = BaiHocUiState.DangTai
            try {
                val tatCaBaiHoc = repository.getTatCaBaiHoc()
                val list = tatCaBaiHoc.filter { it.idCapDo == idCapDo }
                if (list.isEmpty()) {
                    _uiState.value = BaiHocUiState.LoI("Không tìm thấy bài học nào cho trình độ này")
                } else {
                    _uiState.value = BaiHocUiState.ThanhCong(list)
                }
            } catch (e: Exception) {
                _uiState.value = BaiHocUiState.LoI(e.message ?: "Lỗi khi tải dữ liệu")
            }
        }
    }
}
