package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.NguoiDung
import com.example.hoctiengtrung2.data.model.TaiKhoan
import com.example.hoctiengtrung2.data.remote.XacThucRemoteDataSource
import com.example.hoctiengtrung2.data.repository.XacThucRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TrangThaiDangNhap {
    object Cho : TrangThaiDangNhap()
    object Load : TrangThaiDangNhap()
    data class ThanhCong(val nguoiDung: NguoiDung) : TrangThaiDangNhap()
    data class Loi(val message: String) : TrangThaiDangNhap()
}

class XacThucViewModel(private val repository: XacThucRepository) : ViewModel() {
    private val _trangThai = MutableStateFlow<TrangThaiDangNhap>(TrangThaiDangNhap.Cho)
    val trangThai: StateFlow<TrangThaiDangNhap> = _trangThai

    fun dangKy(username: String, mk: String) {
        viewModelScope.launch {
            _trangThai.value = TrangThaiDangNhap.Load
            if (repository.checkUsername(username)) {
                _trangThai.value = TrangThaiDangNhap.Loi("Tên đăng nhập đã tồn tại!")
                return@launch
            }
            val taiKhoan = TaiKhoan(tenDangNhap = username, matKhau = mk, quyen = "HocVien")
            val nguoiDung = repository.registerUser(taiKhoan)
            
            if (nguoiDung != null) {
                _trangThai.value = TrangThaiDangNhap.ThanhCong(nguoiDung)
            } else {
                _trangThai.value = TrangThaiDangNhap.Loi("Đăng ký thất bại!")
            }
        }
    }

    fun dangNhap(username: String, mk: String) {
        viewModelScope.launch {
            _trangThai.value = TrangThaiDangNhap.Load
            val taiKhoan = repository.loginUser(username, mk)
            if (taiKhoan != null) {
                val nguoiDung = repository.getProfile(taiKhoan.idTaiKhoan)
                if (nguoiDung != null) {
                    _trangThai.value = TrangThaiDangNhap.ThanhCong(nguoiDung)
                } else {
                    _trangThai.value = TrangThaiDangNhap.Loi("Không tìm thấy thông tin người dùng!")
                }
            } else {
                _trangThai.value = TrangThaiDangNhap.Loi("Sai tài khoản hoặc mật khẩu!")
            }
        }
    }

    fun themThongTin(idNguoiDung: String, ten: String, target: Int) {
        viewModelScope.launch {
            _trangThai.value = TrangThaiDangNhap.Load
            if (ten.isBlank()) {
                _trangThai.value = TrangThaiDangNhap.Loi("Vui lòng nhập tên!")
                return@launch
            }
            val success = repository.capNhatThongTin(idNguoiDung, ten, target)
            if (success) {
                // Lấy lại profile đầy đủ hoặc tạo object mới để đảm bảo có thông tin người dùng
                val updatedNguoiDung = repository.getProfile(idNguoiDung) ?: NguoiDung(
                    idNguoiDung = idNguoiDung,
                    tenNguoiDung = ten,
                    target = target
                )
                _trangThai.value = TrangThaiDangNhap.ThanhCong(updatedNguoiDung)
            } else {
                _trangThai.value = TrangThaiDangNhap.Loi("Cập nhật thất bại!")
            }
        }
    }

    fun doiMatKhau(idTK: String, matKhauMoi: String) {
        viewModelScope.launch {
            _trangThai.value = TrangThaiDangNhap.Load
            val success = repository.doiMatKhau(idTK, matKhauMoi)
            if (success) {
                _trangThai.value = TrangThaiDangNhap.Cho // Hoặc tạo trạng thái thành công riêng
            } else {
                _trangThai.value = TrangThaiDangNhap.Loi("Đổi mật khẩu thất bại!")
            }
        }
    }

    fun resetState() {
        _trangThai.value = TrangThaiDangNhap.Cho
    }
}
