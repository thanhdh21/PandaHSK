package com.example.hoctiengtrung2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
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
    object TraCuu : Screen("tra_cuu", "Tra cứu", Icons.Default.Search)
    object TaoThe : Screen("tao_the", "Tạo thẻ", Icons.Default.AddCircle)
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

    object CaiDat : Screen("cai_dat")
    object DoiMatKhau : Screen("doi_mat_khau")
}

val BottomNavItems = listOf(
    Screen.TrangChu,
    Screen.LoTrinh,
    Screen.TraCuu,
    Screen.TaoThe,
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
        composable(route = Screen.ChaoMung.route) { _ ->
            ChaoMungScreen(
                onDangNhap = { navController.navigate(Screen.DangNhap.route) },
                onVaoLuon = { 
                    navController.navigate(Screen.TrangChu.route) {
                        popUpTo(Screen.ChaoMung.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.DangNhap.route) { _ ->
            DangNhapScreen(
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
        composable(route = Screen.DangKy.route) { _ ->
            DangKyScreen(
                onRegisterSuccess = { idNguoiDung ->
                    navController.navigate(Screen.ThemThongTin.buildRoute(idNguoiDung)) {
                        popUpTo(Screen.DangKy.route) { inclusive = true }
                    }
                },
                onNavigateToDangNhap = { navController.popBackStack() },
                onQuayLai = {
                    navController.navigate(Screen.ChaoMung.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ThemThongTin.route) { backStackEntry: NavBackStackEntry ->
            val idNguoiDung = backStackEntry.arguments?.getString("idNguoiDung") ?: ""
            ThemThongTinScreen(
                idNguoiDung = idNguoiDung,
                onHoanTat = {
                    navController.navigate(Screen.TrangChu.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.TrangChu.route) { _ ->
            HomeScreen(
                onNavigateToBaiHoc = { id, ten ->
                    navController.navigate(Screen.TuVung.buildRoute(id, ten))
                },
                onNavigateToThemThongTin = { idNguoiDung ->
                    navController.navigate(Screen.ThemThongTin.buildRoute(idNguoiDung)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.LoTrinh.route) { _ ->
            ChonTrinhDoScreen(
                onChonCapDo = { id -> navController.navigate(Screen.ChonBaiHoc.buildRoute(id)) },
                onQuayLai = { navController.popBackStack() }
            )
        }

        composable(route = Screen.TraCuu.route) { _ ->
            TraCuuScreen()
        }

        composable(route = Screen.TaoThe.route) { _ ->
            TaoFlashcardScreen(
                onHoanTat = {
                    navController.navigate(Screen.TrangChu.route) {
                        popUpTo(Screen.TrangChu.route) { inclusive = false }
                    }
                }
            )
        }

        composable(route = Screen.CaNhan.route) { _ ->
            CaNhanScreen(
                onNavigateToCaiDat = { navController.navigate(Screen.CaiDat.route) },
                onNavigateToDoiMatKhau = { navController.navigate(Screen.DoiMatKhau.route) },
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

        composable(route = Screen.CaiDat.route) { _ ->
            CaiDatScreen(
                onQuayLai = { navController.popBackStack() },
                onHoanTat = { navController.popBackStack() },
                onNavigateToDoiMatKhau = { navController.navigate(Screen.DoiMatKhau.route) },
                onDangXuat = {
                    navController.navigate(Screen.ChaoMung.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.DoiMatKhau.route) { _ ->
            DoiMatKhauScreen(
                onQuayLai = { navController.popBackStack() },
                onHoanTat = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ChonBaiHoc.route) { backStackEntry: NavBackStackEntry ->
            val idCapDo = backStackEntry.arguments?.getString("idCapDo") ?: "1"
            ChonBaiHocScreen(
                idCapDo = idCapDo,
                onChonBaiHoc = { id, ten -> navController.navigate(Screen.TuVung.buildRoute(id, ten)) },
                onQuayLai = { navController.popBackStack() }
            )
        }

        composable(route = Screen.TuVung.route) { backStackEntry: NavBackStackEntry ->
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

        composable(route = Screen.TracNghiem.route) { backStackEntry: NavBackStackEntry ->
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
