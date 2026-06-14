package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.data.repository.TuVungRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CauHoi(
    val idTuVung: String,
    val hanTu: String,
    val pinyin: String,
    val dapAnDung: String,
    val cacDapAn: List<String>
)

data class TracNghiemUiState(
    val dangTai: Boolean = false,
    val loI: String? = null,
    val danhSachCauHoi: List<CauHoi> = emptyList(),
    val viTriHienTai: Int = 0,
    val dapAnDaChon: String? = null,
    val soCauDung: Int = 0,
    val soCauSai: Int = 0,
    val daHoanThanh: Boolean = false
)

enum class TrangThaiDapAn { CHUA_CHON, DUNG, SAI, MO }

class TracNghiemViewModel(private val repository: TuVungRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TracNghiemUiState())
    val uiState: StateFlow<TracNghiemUiState> = _uiState.asStateFlow()
    private var _danhSachTuGoc: List<TuVung> = emptyList()
    private var _khoTuDistractors: List<TuVung> = emptyList()

    fun taiDuLieu(idBaiHoc: String, idNguoiDung: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(dangTai = true, loI = null)
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
                _danhSachTuGoc = list
                if (list.isEmpty()) {
                    _uiState.value = _uiState.value.copy(dangTai = false, loI = "Không có từ vựng nào")
                } else {
                    // Nếu danh sách câu hỏi ít hơn 4 từ, tải thêm tất cả từ vựng để làm kho đáp án nhiễu
                    _khoTuDistractors = if (list.size < 4) {
                        repository.layTatCaTuVung()
                    } else {
                        list
                    }
                    val questions = taoCauHoi(list, _khoTuDistractors)
                    _uiState.value = _uiState.value.copy(dangTai = false, danhSachCauHoi = questions)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(dangTai = false, loI = e.message ?: "Lỗi không xác định")
            }
        }
    }

    private fun taoCauHoi(danhSachTu: List<TuVung>, khoTuDistractors: List<TuVung>): List<CauHoi> {
        return danhSachTu.map { tuHienTai ->
            val dapAnSai = khoTuDistractors
                .filter { it.idTuVung != tuHienTai.idTuVung }
                .shuffled()
                .take(3)
                .map { it.nghia }
            val cacDapAn = (dapAnSai + tuHienTai.nghia).shuffled()
            CauHoi(
                idTuVung = tuHienTai.idTuVung,
                hanTu = tuHienTai.hanTu,
                pinyin = tuHienTai.pinyin,
                dapAnDung = tuHienTai.nghia,
                cacDapAn = cacDapAn
            )
        }.shuffled()
    }

    fun chonDapAn(dapAn: String, idNguoiDung: String, isReview: Boolean) {
        val current = _uiState.value
        if (current.dapAnDaChon != null) return

        val cauHoi = current.danhSachCauHoi[current.viTriHienTai]
        val dungHay = dapAn == cauHoi.dapAnDung

        viewModelScope.launch {
            repository.capNhatTienDoTuVung(
                idNguoiDung = idNguoiDung,
                idTuVung = cauHoi.idTuVung,
                laDung = dungHay,
                tuTracNghiem = true,
                isReview = isReview
            )
        }

        _uiState.value = current.copy(
            dapAnDaChon = dapAn,
            soCauDung = if (dungHay) current.soCauDung + 1 else current.soCauDung,
            soCauSai = if (!dungHay) current.soCauSai + 1 else current.soCauSai
        )
    }

    fun cauTiepTheo(idNguoiDung: String, idBaiHoc: String) {
        val current = _uiState.value
        if (current.viTriHienTai < current.danhSachCauHoi.size - 1) {
            _uiState.value = current.copy(
                viTriHienTai = current.viTriHienTai + 1,
                dapAnDaChon = null
            )
        } else {
            // Khi hoàn thành câu cuối, cập nhật trạng thái bài học và streak
            viewModelScope.launch {
                if (!idBaiHoc.startsWith("review")) {
                    repository.hoanThanhBaiHoc(idNguoiDung, idBaiHoc)
                }
                repository.capNhatStreak(idNguoiDung)
            }
            _uiState.value = current.copy(daHoanThanh = true)
        }
    }

    fun lamLai() {
        val questions = taoCauHoi(_danhSachTuGoc, _khoTuDistractors)
        _uiState.value = _uiState.value.copy(
            danhSachCauHoi = questions,
            viTriHienTai = 0,
            dapAnDaChon = null,
            soCauDung = 0,
            soCauSai = 0,
            daHoanThanh = false
        )
    }
}
