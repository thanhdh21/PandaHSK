package com.example.hoctiengtrung2.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

data class LichSuUiState(
    val idNguoiDung: String? = null,        // null nếu là khách (chưa đăng nhập)
    val chuoiNgay: Int = 0,
    val cacNgayDaHoc: List<String> = emptyList(),
    val dangHienLich: Boolean = false       // Trạng thái ẩn/hiện hộp thoại Dialog trên giao diện
)

class HomeViewModel : ViewModel() {
    private val repository = HomeRepository(HomeRemoteDataSource())

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.DangTai)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun layDuLieu(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = HomeUiState.DangTai
            val data = repository.getHomeData(idNguoiDung)
            if (data != null) {
                _uiState.value = HomeUiState.ThanhCong(data)
            } else {
                _uiState.value = HomeUiState.LoI("Không thể tải thông tin người dùng")
            }
        }
    }

    fun lamMoiDuLieu(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) return
        viewModelScope.launch {
            val data = repository.getHomeData(idNguoiDung)
            if (data != null) {
                _uiState.value = HomeUiState.ThanhCong(data)
            }
        }
    }

    // === lich su hoc ===
    var lichSuUiState by mutableStateOf(LichSuUiState())
        private set

    fun layLichSuHocTap(idNguoiDung: String) {
        if (idNguoiDung.isBlank()) {

            lichSuUiState = LichSuUiState(idNguoiDung = null)
            return
        }

        viewModelScope.launch {
            val snapshot = repository.layLichSuHoc(idNguoiDung)
            if (snapshot != null) {

                val ngayHoc = snapshot.get("ngayDaHoc") as? List<String> ?: emptyList()
                val chuoi = snapshot.getLong("chuoiNgay")?.toInt() ?: 0

                lichSuUiState = lichSuUiState.copy(
                    idNguoiDung = idNguoiDung,
                    chuoiNgay = chuoi,
                    cacNgayDaHoc = ngayHoc
                )
            } else {

                lichSuUiState = LichSuUiState(idNguoiDung = null)
            }
        }
    }

    fun thayDoiHienThiLich(hienThi: Boolean) {
        lichSuUiState = lichSuUiState.copy(dangHienLich = hienThi)
    }
}
