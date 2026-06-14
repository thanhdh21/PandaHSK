package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.utils.SessionManager

@Composable
fun CaNhanScreen(
    onDangXuat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToDangNhap: () -> Unit, // BỔ SUNG: Tham số dẫn lối về trang Đăng nhập khi bấm nút

) {
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val uiState by viewModel.uiState.collectAsState()

    // BỔ SUNG: Kiểm tra xem thực tế trạng thái thiết bị đã đăng nhập chưa
    val daDangNhap = SessionManager.daDangNhap(context)

    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotBlank() && daDangNhap) { // Thêm điều kiện chỉ load dữ liệu nếu đã đăng nhập
            viewModel.layDuLieu(idNguoiDung)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hồ sơ cá nhân",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimDam
                )
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // SỬA ĐỔI: Nhảy vào rẽ nhánh kiểm tra tài khoản Khách tại đây
            if (!daDangNhap) {
                // NẾU LÀ KHÁCH: Hiện Form thiết kế gợi ý đăng nhập nịnh mắt chuẩn màu Pastel
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(NenTim, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👤", fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Bạn chưa đăng nhập",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TimDam
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Đăng nhập tài khoản để đồng bộ chuỗi ngày học, từ vựng đã thuộc và theo dõi tiến trình luyện thi HSK của riêng bạn.",
                        fontSize = 13.sp,
                        color = TimMo,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onNavigateToDangNhap,
                        colors = ButtonDefaults.buttonColors(containerColor = TimTrung),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Đăng nhập ngay", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            } else {
                // NẾU ĐÃ ĐĂNG NHẬP: Giữ nguyên vẹn 100% logic load dữ liệu Firebase cũ từ trước của bạn
                when (uiState) {
                    is HomeUiState.DangTai -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TimTrung)
                    }
                    is HomeUiState.LoI -> {
                        Text(
                            (uiState as HomeUiState.LoI).thongBao,
                            modifier = Modifier.align(Alignment.Center),
                            color = HongDam
                        )
                    }
                    is HomeUiState.ThanhCong -> {
                        val data = (uiState as HomeUiState.ThanhCong).data
                        CaNhanContent(
                            idNguoiDung = idNguoiDung,
                            data = data,
                            onDangXuat = {
                                SessionManager.dangXuat(context)
                                onDangXuat()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaNhanContent(
    idNguoiDung: String,
    data: com.example.hoctiengtrung2.data.repository.HomeData,
    onDangXuat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar & Name
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(TimPastel),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.nguoiDung.tenNguoiDung.take(1).uppercase(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TimTrung
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = data.nguoiDung.tenNguoiDung,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TimDam
        )
        Text(
            text = "Trình độ: ${data.capDo.tenCapDo}",
            fontSize = 14.sp,
            color = TimNhat
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem(Modifier.weight(1f), "🔥", "${data.nguoiDung.streak}", "Ngày chuỗi")
            StatItem(Modifier.weight(1f), "📚", "${data.tongTuDaHoc}", "Từ đã học")
            StatItem(Modifier.weight(1f), "🎯", "${data.tuHocHomNay}/${data.nguoiDung.target}", "Mục tiêu")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NenTrang)
                .padding(vertical = 8.dp)
        ) {
            MenuItem(icon = Icons.Default.Settings, title = "Cài đặt tài khoản")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = TimMo)
            MenuItem(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Đăng xuất", color = Color.Red, onClick = onDangXuat)
        }
        
        KhuKhoFlashcard(idNguoiDung = idNguoiDung)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Phiên bản 1.0.0",
            fontSize = 12.sp,
            color = TimMo
        )
    }
}

@Composable
fun StatItem(modifier: Modifier = Modifier, icon: String, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TimDam)
            Text(label, fontSize = 11.sp, color = TimMo)
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    color: Color = TimDam,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(text = title, fontSize = 16.sp, color = color, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TimMo)
        }
    }
}

@Composable
fun KhuKhoFlashcard(idNguoiDung: String, modifier: Modifier = Modifier) {
    // Khởi tạo Repository để lấy dữ liệu ngầm từ Firebase
    val tuVungRepository = remember {
        com.example.hoctiengtrung2.data.repository.TuVungRepository(
            com.example.hoctiengtrung2.data.remote.TuVungRemoteDataSource()
        )
    }

    var danhSachFlashcard by remember { mutableStateOf<List<com.example.hoctiengtrung2.data.remote.TuVungInternetDto>>(emptyList()) }
    var dangTai by remember { mutableStateOf(true) }

    // Tự động tải danh sách từ Firebase về khi mở màn hình hồ sơ
    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotBlank()) {
            danhSachFlashcard = tuVungRepository.layDanhSachFlashcardCaNhan(idNguoiDung)
            dangTai = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "📚 Sổ tay từ vựng cá nhân (${danhSachFlashcard.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TimDam,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (dangTai) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TimTrung, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
        } else if (danhSachFlashcard.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NenTrang),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
            ) {
                Text(
                    text = "Bạn chưa lưu từ vựng nào từ Internet.\nHãy dùng tính năng tra từ ngoài Trang chủ nhé! ❤️",
                    fontSize = 12.sp,
                    color = TimMo,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        } else {
            // Hiển thị danh sách từ đã lưu, tái sử dụng chính hàm cấu trúc WordItem gọn gàng
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                danhSachFlashcard.forEach { tuVung ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NenTrang),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tuVung.hanzi, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TimDam)
                                Text(text = tuVung.pinyin, fontSize = 14.sp, color = TimTrung, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = tuVung.meaning, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.7f))
                            }
                            Text(text = "❤️", fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                }
            }
        }
    }
}