package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.BaiHoc
import com.example.hoctiengtrung2.data.model.NguoiDungTuVung
import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BaiHocUiState {
    object DangTai : BaiHocUiState()
    data class ThanhCong(val danhSach: List<BaiHocVoiTienDo>) : BaiHocUiState()
    data class LoI(val thongBao: String) : BaiHocUiState()
}

data class BaiHocVoiTienDo(
    val baiHoc: BaiHoc,
    val soTuDaHoc: Int,
    val phanTram: Float
)

class BaiHocViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<BaiHocUiState>(BaiHocUiState.DangTai)
    val uiState: StateFlow<BaiHocUiState> = _uiState.asStateFlow()

    fun layDanhSachBaiHoc(idCapDo: String, idNguoiDung: String) {
        viewModelScope.launch {
            _uiState.value = BaiHocUiState.DangTai
            try {
                val tatCaBaiHoc = repository.getTatCaBaiHoc()
                val dsBaiHoc = tatCaBaiHoc.filter { it.idCapDo == idCapDo }
                
                if (dsBaiHoc.isEmpty()) {
                    _uiState.value = BaiHocUiState.LoI("Không tìm thấy bài học nào cho trình độ này")
                    return@launch
                }

                val tatCaTuVung = repository.getTatCaTuVung()
                val tienDoNguoiDung = repository.getTienDoTuVungNguoiDung(idNguoiDung)

                val setTuDaHoc = tienDoNguoiDung
                    .filter { it.daHoc }
                    .map { it.idTuVung }
                    .toSet()

                val listVoiTienDo = dsBaiHoc.map { baiHoc ->
                    val tuVungCuaBai = tatCaTuVung.filter { it.idBaiHoc == baiHoc.idBaiHoc }
                    val soTuTrongBai = tuVungCuaBai.size.coerceAtLeast(baiHoc.soTu)
                    
                    val soTuDaThuoc = tuVungCuaBai.count { it.idTuVung in setTuDaHoc }
                    
                    val phanTram = if (soTuTrongBai > 0) soTuDaThuoc.toFloat() / soTuTrongBai else 0f
                    
                    BaiHocVoiTienDo(
                        baiHoc = baiHoc,
                        soTuDaHoc = soTuDaThuoc,
                        phanTram = phanTram
                    )
                }

                _uiState.value = BaiHocUiState.ThanhCong(listVoiTienDo)
            } catch (e: Exception) {
                _uiState.value = BaiHocUiState.LoI(e.message ?: "Lỗi khi tải dữ liệu")
            }
        }
    }
}
