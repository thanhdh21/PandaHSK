package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.repository.HomeData
import com.example.hoctiengtrung2.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object DangTai : HomeUiState()
    data class ThanhCong(val data: HomeData) : HomeUiState()
    data class LoI(val thongBao: String) : HomeUiState()
}

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.DangTai)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun layDuLieu(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = HomeUiState.DangTai
            try {
                val data = repository.getHomeData(idNguoiDung)
                if (data != null) {
                    _uiState.value = HomeUiState.ThanhCong(data)
                    launch {
                        try {
                            repository.getTatCaCapDo()
                            repository.getTatCaBaiHoc()
                            repository.getTatCaTuVung()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    _uiState.value = HomeUiState.LoI("Không thể tải thông tin người dùng")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = HomeUiState.LoI("Lỗi kết nối hoặc chưa có dữ liệu ngoại tuyến. Vui lòng kết nối mạng và thử lại.")
            }
        }
    }

    fun lamMoiDuLieu(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) return
        viewModelScope.launch {
            try {
                val data = repository.getHomeData(idNguoiDung)
                if (data != null) {
                    _uiState.value = HomeUiState.ThanhCong(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun taoDuLieuMau(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) return
        viewModelScope.launch {
            try {
                repository.taoDuLieuMau(idNguoiDung)
                layDuLieu(idNguoiDung)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
