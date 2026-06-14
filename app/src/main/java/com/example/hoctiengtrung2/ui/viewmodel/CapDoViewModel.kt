package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.CapDo
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Biểu diễn các trạng thái có thể có của màn hình chọn cấp độ.
 *
 * Được thiết kế dưới dạng sealed class để UI có thể xử lý đầy đủ
 * tất cả các trướng hợp khi vẽ màn hình (when-expression đầy đủ).
 */
sealed class CapDoUiState {
    /** Trạng thái đang tải dữ liệu từ server; UI hiển thị vong quay chờ (loading spinner). */
    object DangTai : CapDoUiState()

    /**
     * Trạng thái tải thành công; mang danh sách cấp độ để hiển thị lên lưới thẻ.
     * @param danhSach Danh sách [CapDo] lấy được từ Firestore.
     */
    data class ThanhCong(val danhSach: List<CapDo>) : CapDoUiState()

    /**
     * Trạng thái lỗi; mang thông báo lỗi cần hiển thị cho người dùng.
     * @param thongBao Nội dung của lỗi (từ Exception.message hoặc thông báo mặc định).
     */
    data class LoI(val thongBao: String) : CapDoUiState()
}

/**
 * ViewModel quản lý dữ liệu và trạng thái UI cho màn hình chọn cấp độ.
 *
 * Lồng ghép [HomeRepository] thông qua constructor injection, đảm bảo tách
 * biệt giữa lớp dữ liệu và lớp trình bày. Dữ liệu được gửi đi
 * dưới dạng [StateFlow] để UI có thể observe và tự động cập nhật.
 *
 * @param repository [HomeRepository] cung cấp các hàm truy cập dữ liệu
 *                   (Firestore và các nguồn remote khác).
 */
class CapDoViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CapDoUiState>(CapDoUiState.DangTai)
    val uiState: StateFlow<CapDoUiState> = _uiState

    init {
        layTatCaCapDo()
    }

    /**
     * Gọi API để lấy toàn bộ danh sách cấp độ và cập nhật [uiState].
     *
     * Được gọi tự động trong [init] khi ViewModel được khởi tạo;
     * cũng có thể gọi lại từ UI khi người dùng muốn tải lại (refresh).
     *
     * Luồng xử lý:
     *  1. Đặt [uiState] = [CapDoUiState.DangTai] để UI hiển thị loading.
     *  2. Gọi [HomeRepository.getTatCaCapDo] trong coroutine scope của ViewModel.
     *  3. Nếu thành công và danh sách không rỗng → [CapDoUiState.ThanhCong] (sắp xếp theo tên).
     *  4. Nếu danh sách rỗng → [CapDoUiState.LoI] với thông báo phù hợp.
     *  5. Nếu có ngoại lệ → [CapDoUiState.LoI] với nội dung lỗi từ exception.
     */
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
