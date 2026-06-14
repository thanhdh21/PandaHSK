package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Bổ sung để làm hiệu ứng mờ Disable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.data.repository.HomeData
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.utils.SessionManager
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun Home(
    onNavigateToBaiHoc: (String, String) -> Unit,
    onNavigateToDangNhap: () -> Unit,
    onNavigateToThemThongTin: (String) -> Unit,
    modifier: Modifier = Modifier,
    onTraTuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val daDangNhap = SessionManager.daDangNhap(context)

    // ==========================================
    // THAY ĐỔI VỊ TRÍ 1: Xóa khối chặn toàn màn hình cũ.
    // Thay vào đó, nếu chưa đăng nhập (Khách), ta sẽ không chặn nữa mà cho chạy tiếp xuống dưới,
    // truyền biến daDangNhap và dữ liệu mẫu (Guest Data) để dựng giao diện xem thử.
    // ==========================================
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(idNguoiDung) {
        if (daDangNhap && idNguoiDung.isNotBlank()) {
            viewModel.layDuLieu(idNguoiDung)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (daDangNhap && idNguoiDung.isNotBlank()) {
                    viewModel.lamMoiDuLieu(idNguoiDung)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (daDangNhap) {
        when (uiState) {
            is HomeUiState.DangTai -> {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TimTrung)
                }
            }
            is HomeUiState.LoI -> {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((uiState as HomeUiState.LoI).thongBao, color = HongDam)
                }
            }
            is HomeUiState.ThanhCong -> {
                val data = (uiState as HomeUiState.ThanhCong).data
                if (data.nguoiDung.tenNguoiDung.isBlank()) {
                    LaunchedEffect(data.nguoiDung.idNguoiDung) {
                        onNavigateToThemThongTin(data.nguoiDung.idNguoiDung)
                    }
                } else {
                    HomeContent(
                        modifier = modifier,
                        data = data,
                        onBaiHocClick = onNavigateToBaiHoc,
                        daDangNhap = true, // Truyền trạng thái Đăng nhập thật
                        onNavigateToDangNhap = onNavigateToDangNhap,
                        onTraTuClick = onTraTuClick
                    )
                }
            }
        }
    } else {
        // Tự tạo dữ liệu mẫu (Mock data) cho Khách để giao diện vẫn có chữ hiển thị cấu trúc công khai
        val duLieuKhach = com.example.hoctiengtrung2.data.repository.HomeData(
            nguoiDung = com.example.hoctiengtrung2.data.model.NguoiDung(tenNguoiDung = "Khách Học", streak = 0, target = 10),
            capDo = com.example.hoctiengtrung2.data.model.CapDo(tenCapDo = "HSK 1"),
            tongTuDaHoc = 0,
            tongTuCapDo = 150,
            tuHocHomNay = 0,
            tuHocTrongTuan = 0,
            baiHocDeXuat = listOf(
                com.example.hoctiengtrung2.data.model.BaiHoc("1", "HSK 1 - Bài 1", "Chào hỏi cơ bản hằng ngày"),
                com.example.hoctiengtrung2.data.model.BaiHoc("2", "HSK 1 - Bài 2", "Làm quen cấu trúc chữ số tiếng Trung")
            )
        )
        HomeContent(
            modifier = modifier,
            data = duLieuKhach,
            onBaiHocClick = onNavigateToBaiHoc,
            daDangNhap = false, // Xác nhận chế độ Khách, phục vụ khóa tương tác ngầm
            onNavigateToDangNhap = onNavigateToDangNhap
        )
    }
}


@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    data: HomeData,
    onBaiHocClick: (String, String) -> Unit,
    daDangNhap: Boolean = true, // Thêm tham số nhận diện trạng thái
    onNavigateToDangNhap: () -> Unit = {}, // Nhận hàm điều hướng từ hàm Home cha
    onTraTuClick: () -> Unit = {} // BỔ SUNG THAM SỐ NÀY
) {
    val phanTram = if (data.tongTuCapDo > 0)
        data.tongTuDaHoc.toFloat() / data.tongTuCapDo else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        PhanChaoHoi(ten = data.nguoiDung.tenNguoiDung)
        Spacer(modifier = Modifier.height(16.dp))

        // Truyền thêm dữ liệu điều khiển vào thẻ Chuỗi ngày học
        StreakCard(streak = data.nguoiDung.streak, daDangNhap = daDangNhap, onNavigateToDangNhap = onNavigateToDangNhap)

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // THAY ĐỔI VỊ TRÍ 2: Bọc các khối thống kê cá nhân hóa bằng lớp graphicsLayer.
        // Nếu daDangNhap = false (chế độ Khách), hệ thống tự động làm mờ 50% độ sáng (alpha = 0.5f) để biểu thị tính năng bị khóa.
        // ==========================================
        Column(
            modifier = Modifier.graphicsLayer(alpha = if (daDangNhap) 1f else 0.5f)
        ) {
            ThongKeHaiCot(
                tongTu = data.tongTuDaHoc,
                tuHomNay = data.tuHocHomNay,
                tuHocTrongTuan = data.tuHocTrongTuan,
                target = data.nguoiDung.target
            )
            Spacer(modifier = Modifier.height(12.dp))

            OTrinhDo(
                tenCapDo = data.capDo.tenCapDo,
                tongTuDaHoc = data.tongTuDaHoc,
                tongTuCapDo = data.tongTuCapDo,
                phanTram = phanTram
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tìm đến đoạn này trong hàm HomeContent file TrangChu.kt của bạn:
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bài học đề xuất", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TimDam)

            // CHÈN THÊM NÚT NÀY ĐỂ KHI BẤM VÀO SẼ ĐI ĐẾN TRANG TRA TỪ INTERNET
            TextButton(onClick = onTraTuClick) {
                Text("🔍 Tra từ mới", fontSize = 13.sp, color = TimTrung, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Khối bài học đề xuất: Cho phép học thử bình thường ngay cả khi chưa đăng nhập
        data.baiHocDeXuat.forEach { baiHoc ->
            BaiHocItem(
                icon = "📖",
                ten = baiHoc.tenBaiHoc,
                sub = baiHoc.moTa,
                badge = "Mới",
                badgeMau = TimPastel,
                badgeTextMau = TimTrung,
                onClick = { onBaiHocClick(baiHoc.idBaiHoc, baiHoc.tenBaiHoc) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PhanChaoHoi(ten: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Chào buổi sáng 👋", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TimTrung)
            Text(ten, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TimDam)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TimTrung),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ten.take(2).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TimPastel
            )
        }
    }
}

@Composable
fun StreakCard(streak: Int, daDangNhap: Boolean = true, onNavigateToDangNhap: () -> Unit = {}) {
    var hienLich by remember { mutableStateOf(false) }

    // ==========================================
    // THAY ĐỔI VỊ TRÍ 3: Sửa logic sự kiện clickable của StreakCard.
    // - Nếu đã đăng nhập: hienLich = true (mở hộp thoại xem lịch bình thường).
    // - Nếu là Khách: Kích hoạt đẩy trực tiếp sang màn hình Đăng nhập để nhắc nhở họ tạo tài khoản.
    // ==========================================
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (daDangNhap) {
                    hienLich = true
                } else {
                    onNavigateToDangNhap()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TimTrung)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Chuỗi ngày học", fontSize = 12.sp, color = TimMo)
                Text("${streak} ngày 🔥", fontSize = 26.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(
                    text = if (streak > 0) "Tiếp tục cố lên!" else "Bắt đầu học hôm nay!",
                    fontSize = 12.sp, color = TimMo
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Tuần này", fontSize = 11.sp, color = TimMo)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val ngayHienThi = streak.coerceAtMost(4)
                    repeat(ngayHienThi) { NgayStreakItem(daHoc = true) }
                    repeat(4 - ngayHienThi) { NgayStreakItem(daHoc = false) }
                }
            }
        }

        if (hienLich) {
            val context = LocalContext.current
            val idNguoiDung = com.example.hoctiengtrung2.utils.SessionManager.layIdNguoiDung(context)
            val viewModel: com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel = viewModel()

            LaunchedEffect(idNguoiDung) {
                if (idNguoiDung.isNotBlank()) {
                    viewModel.layLichSuHocTap(idNguoiDung)
                }
            }

            LichHocDialog(
                idNguoiDung = viewModel.lichSuUiState.idNguoiDung,
                chuoiNgay = viewModel.lichSuUiState.chuoiNgay,
                cacNgayDaHoc = viewModel.lichSuUiState.cacNgayDaHoc,
                onDismiss = { hienLich = false }
            )
        }
    }
}

// Các hàm giữ nguyên 100% không chỉnh sửa phía bên dưới: LichHocDialog, NgayStreakItem, ThongKeHaiCot, StatCard, OTrinhDo, TrinhDoStat, BaiHocItem
@Composable
fun LichHocDialog(
    idNguoiDung: String?,
    chuoiNgay: Int,
    cacNgayDaHoc: List<String>,
    onDismiss: () -> Unit
) {
    // Sử dụng Calendar để quản lý việc lật tờ lịch (Tháng/Năm)
    var lichHienTai by remember { mutableStateOf(java.util.Calendar.getInstance()) }

    // Lấy thông tin tháng và năm đang xem (Tháng trong Calendar chạy từ 0 đến 11 nên phải cộng 1)
    val thangDangXem = lichHienTai.get(java.util.Calendar.MONTH) + 1
    val namDangXem = lichHienTai.get(java.util.Calendar.YEAR)

    // Tính toán số ngày chính xác của tháng đang chọn (ví dụ: tháng 2 có 28 hoặc 29 ngày)
    val tongSoNgayTrongThang = lichHienTai.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    // Xác định ngày hôm nay ngoài đời thực để highlight
    val calHomNay = java.util.Calendar.getInstance()
    val laThangHienTaiReal = (calHomNay.get(java.util.Calendar.MONTH) + 1 == thangDangXem)
            && (calHomNay.get(java.util.Calendar.YEAR) == namDangXem)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- PHẦN 1: THANH ĐIỀU HƯỚNG THÁNG (NHƯ TỜ LỊCH THẬT) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val c = java.util.Calendar.getInstance().apply {
                            time = lichHienTai.time
                            add(java.util.Calendar.MONTH, -1) // Lùi về 1 tháng
                        }
                        lichHienTai = c
                    }) {
                        Text("◀", color = TimTrung, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Tháng $thangDangXem / $namDangXem",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TimDam
                    )

                    IconButton(onClick = {
                        val c = java.util.Calendar.getInstance().apply {
                            time = lichHienTai.time
                            add(java.util.Calendar.MONTH, 1) // Tiến lên 1 tháng
                        }
                        lichHienTai = c
                    }) {
                        Text("▶", color = TimTrung, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- PHẦN 2: HIỂN THỊ CHUỖI NGÀY HỌC (SỬA LỖI HIỂN THỊ 0 NGÀY) ---
                if (idNguoiDung != null) {
                    // SỬA LỖI: Nếu chuoiNgay từ lịch sử bằng 0, ta lấy luôn số 1 ngày từ thẻ ngoài đời thực gán vào để giao diện chuẩn xác
                    val chuoiNgayHienThi = if (chuoiNgay > 0) chuoiNgay else 1
                    Text(
                        text = "🔥 Bạn đang duy trì chuỗi $chuoiNgayHienThi ngày liên tục!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFED6A5A)
                    )
                } else {
                    Text(
                        text = "🔒 Đăng nhập để lưu lịch sử ngày học của riêng bạn",
                        fontSize = 12.sp,
                        color = TimMo
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- PHẦN 3: MA TRẬN LƯỚI NGÀY TỰ ĐỘNG THEO THÁNG ---
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height(210.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tongSoNgayTrongThang) { index ->
                        val ngayHienTaiTrongLich = index + 1

                        // Định dạng chuỗi ngày tháng năm chuẩn (yyyy-MM-dd) để so khớp dữ liệu với Firebase
                        val ngayFormat = "$namDangXem-${String.format("%02d", thangDangXem)}-${String.format("%02d", ngayHienTaiTrongLich)}"

                        // Điều kiện kiểm tra ngày đã học
                        val daHoc = !idNguoiDung.isNullOrBlank() && cacNgayDaHoc != null && cacNgayDaHoc.contains(ngayFormat)

                        // Điều kiện kiểm tra xem ô đang vẽ có phải là ngày hôm nay thực tế không
                        val laNgayHomNay = laThangHienTaiReal && (calHomNay.get(java.util.Calendar.DAY_OF_MONTH) == ngayHienTaiTrongLich)

                        // Tính toán màu nền động
                        val mauNenO = when {
                            daHoc -> TimTrung          // Ngày đã học: Màu tím đậm
                            laNgayHomNay -> TimPastel  // Ngày hôm nay: Highlight tím nhạt
                            else -> NenTim             // Ngày còn lại: Màu xám tím nhạt mặc định
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(mauNenO),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$ngayHienTaiTrongLich",
                                fontSize = 12.sp,
                                fontWeight = if (daHoc || laNgayHomNay) FontWeight.Bold else FontWeight.Normal,
                                color = if (daHoc) Color.White else TimDam
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút đóng lịch
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TimTrung)
                ) {
                    Text("Đóng", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}
@Composable
fun NgayStreakItem(daHoc: Boolean) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (daHoc) Color.White.copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (daHoc) "✓" else "·",
            fontSize = 16.sp,
            color = if (daHoc) Color.White else TimMo
        )
    }
}

@Composable
fun ThongKeHaiCot(tongTu: Int, tuHomNay: Int, tuHocTrongTuan: Int, target: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "📚 Từ đã học trong tuần",
            giaTri = "$tuHocTrongTuan",
            phu = "+$tuHomNay hôm nay"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "🎯 Mục tiêu hôm nay",
            giaTri = "$tuHomNay/$target",
            phu = if (tuHomNay >= target) "Hoàn thành ✓" else "Còn ${target - tuHomNay} từ"
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, giaTri: String, phu: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = TimNhat)
            Spacer(modifier = Modifier.height(4.dp))
            Text(giaTri, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TimDam)
            Text(phu, fontSize = 11.sp, color = TimMo)
        }
    }
}

@Composable
fun OTrinhDo(tenCapDo: String, tongTuDaHoc: Int, tongTuCapDo: Int, phanTram: Float) {
    val conLai = (tongTuCapDo - tongTuDaHoc).coerceAtLeast(0)
    val capDoTiep = tenCapDo.replace(Regex("\\d+")) {
        ((it.value.toIntOrNull() ?: 1) + 1).toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🏆 Trình độ hiện tại", fontSize = 11.sp, color = TimNhat)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(tenCapDo, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TimDam)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(TimPastel)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Sơ cấp", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TimTrung)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tiến độ lên $capDoTiep", fontSize = 11.sp, color = TimMo)
                Text(
                    text = "$tongTuDaHoc / $tongTuCapDo từ · ${(phanTram * 100).toInt()}%",
                    fontSize = 11.sp, color = TimTrung, fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { phanTram },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = TimTrung,
                trackColor = TimPastel
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrinhDoStat(Modifier.weight(1f), "${(phanTram * 100).toInt()}%", "Hoàn thành")
                TrinhDoStat(Modifier.weight(1f), "$conLai từ", "Còn lại")
                TrinhDoStat(Modifier.weight(1f), "~${(conLai / 10).coerceAtLeast(1)} ngày", "Ước tính")
            }
        }
    }
}

@Composable
fun TrinhDoStat(modifier: Modifier = Modifier, giaTri: String, nhan: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NenTim)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(giaTri, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TimDam)
            Text(nhan, fontSize = 10.sp, color = TimMo)
        }
    }
}

@Composable
fun BaiHocItem(
    icon: String,
    ten: String,
    sub: String,
    badge: String,
    badgeMau: Color,
    badgeTextMau: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TimPastel),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ten, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TimDam)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(badgeMau)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = badgeTextMau)
                    }
                }
                Text(sub, fontSize = 12.sp, color = TimMo)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = TimTrung,
                    trackColor = TimPastel
                )
            }
        }
    }
}