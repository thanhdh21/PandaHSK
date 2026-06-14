package com.example.hoctiengtrung2.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hoctiengtrung2.ui.screens.*
import com.example.hoctiengtrung2.data.model.NguoiDung

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object ChaoMung : Screen("chao_mung")
    object DangNhap : Screen("dang_nhap")
    object DangKy : Screen("dang_ky")
    object ThemThongTin : Screen("them_thong_tin/{idNguoiDung}") {
        fun buildRoute(id: String) = "them_thong_tin/$id"
    }

    object TrangChu : Screen("trang_chu", "Trang chủ", Icons.Default.Home)
    object LoTrinh : Screen("lo_trinh", "Lộ trình", Icons.AutoMirrored.Filled.LibraryBooks)
    object CaNhan : Screen("ca_nhan", "Cá nhân", Icons.Default.Person)
    
    object ChonBaiHoc : Screen("chon_bai_hoc/{idCapDo}") {
        fun buildRoute(id: String) = "chon_bai_hoc/$id"
    }
    
    object TuVung : Screen("tu_vung/{idBaiHoc}/{tenBaiHoc}") {
        fun buildRoute(id: String, ten: String) = "tu_vung/$id/$ten"
    }
    
    object TracNghiem : Screen("trac_nghiem/{idBaiHoc}/{tenBaiHoc}") {
        fun buildRoute(id: String, ten: String) = "trac_nghiem/$id/$ten"
    }
}

val BottomNavItems = listOf(
    Screen.TrangChu,
    Screen.LoTrinh,
    Screen.CaNhan
)

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.ChaoMung.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.ChaoMung.route) {
            ManHinhChaoMung(
                onDangNhap = { navController.navigate(Screen.DangNhap.route) },
                onVaoLuon = { 
                    navController.navigate(Screen.TrangChu.route) {
                        popUpTo(Screen.ChaoMung.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.DangNhap.route) {
            DangNhap(
                onLoginSuccess = { nguoiDung ->
                    if (nguoiDung.tenNguoiDung.isBlank()) {
                        navController.navigate(Screen.ThemThongTin.buildRoute(nguoiDung.idNguoiDung)) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.TrangChu.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.DangKy.route) },
                onQuayLai = { navController.popBackStack() }
            )
        }
        composable(route = Screen.DangKy.route) {
            DangKy(
                onRegisterSuccess = { idNguoiDung ->
                    navController.navigate(Screen.ThemThongTin.buildRoute(idNguoiDung)) {
                        popUpTo(Screen.DangKy.route) { inclusive = true }
                    }
                },
                onNavigateToDangNhap = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ThemThongTin.route) { backStackEntry ->
            val idNguoiDung = backStackEntry.arguments?.getString("idNguoiDung") ?: ""
            ThemThongTin(
                idNguoiDung = idNguoiDung,
                onHoanTat = {
                    navController.navigate(Screen.TrangChu.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 1. Sửa khối Trang chủ: Truyền lệnh điều hướng cho onNavigateToDangNhap
        composable(route = Screen.TrangChu.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val idNguoiDung = com.example.hoctiengtrung2.utils.SessionManager.layIdNguoiDung(context)

            // Quản lý trạng thái đóng/mở màn hình tra từ
            var hienManHinhTraTu by remember { mutableStateOf(false) }

            if (hienManHinhTraTu) {
                // Gọi màn hình TraTuScreen và truyền đủ tham số nút quay lại
                TraTuScreen(
                    idNguoiDung = idNguoiDung,
                    onQuayLai = { hienManHinhTraTu = false }
                )
            } else {
                // Giữ nguyên hàm Home gốc của bạn
                Home(
                    onNavigateToBaiHoc = { id, ten ->
                        navController.navigate(Screen.TuVung.buildRoute(id, ten))
                    },
                    onNavigateToDangNhap = {
                        navController.navigate(Screen.DangNhap.route)
                    },
                    onNavigateToThemThongTin = { idND ->
                        navController.navigate(Screen.ThemThongTin.buildRoute(idND)) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onTraTuClick = { hienManHinhTraTu = true }
                )
            }
        }

        composable(route = Screen.LoTrinh.route) {
            ChonTrinhDoScreen(
                onChonCapDo = { id -> navController.navigate(Screen.ChonBaiHoc.buildRoute(id)) },
                onQuayLai = { navController.popBackStack() }
            )
        }

        // 2. Sửa khối Cá nhân: Đảm bảo onNavigateToDangNhap được truyền đầy đủ
        composable(route = Screen.CaNhan.route) {
            CaNhanScreen(
                onDangXuat = {
                    navController.navigate(Screen.ChaoMung.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToDangNhap = {
                    navController.navigate(Screen.DangNhap.route)
                }
            )
        }

        composable(route = Screen.ChonBaiHoc.route) { backStackEntry ->
            val idCapDo = backStackEntry.arguments?.getString("idCapDo") ?: "1"
            ChonBaiHocScreen(
                idCapDo = idCapDo,
                onChonBaiHoc = { id, ten -> navController.navigate(Screen.TuVung.buildRoute(id, ten)) },
                onQuayLai = { navController.popBackStack() }
            )
        }

        composable(route = Screen.TuVung.route) { backStackEntry ->
            val idBaiHoc = backStackEntry.arguments?.getString("idBaiHoc") ?: ""
            val tenBaiHoc = backStackEntry.arguments?.getString("tenBaiHoc") ?: "Bài học"
            TuVungScreen(
                idBaiHoc = idBaiHoc,
                tenBaiHoc = tenBaiHoc,
                onQuayLai = { navController.popBackStack() },
                onBatDauOnTap = {
                    navController.navigate(Screen.TracNghiem.buildRoute(idBaiHoc, tenBaiHoc))
                }
            )
        }

        composable(route = Screen.TracNghiem.route) { backStackEntry ->
            val idBaiHoc = backStackEntry.arguments?.getString("idBaiHoc") ?: ""
            val tenBaiHoc = backStackEntry.arguments?.getString("tenBaiHoc") ?: "Bài học"
            TracNghiemScreen(
                idBaiHoc = idBaiHoc,
                tenBaiHoc = tenBaiHoc,
                onQuayLai = { navController.popBackStack() },
                onVeTrangChu = { navController.popBackStack(Screen.TrangChu.route, inclusive = false) }
            )
        }
    }
}
