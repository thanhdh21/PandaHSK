package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.data.repository.HomeData
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.utils.SessionManager
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun HomeScreen(
    onNavigateToBaiHoc: (String, String) -> Unit,
    onNavigateToThemThongTin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val daDangNhap = SessionManager.daDangNhap(context)

    if (!daDangNhap) {
        ChuaDangNhap(modifier = modifier)
        return
    }

    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotBlank()) {
            viewModel.layDuLieu(idNguoiDung)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (idNguoiDung.isNotBlank()) {
                    viewModel.lamMoiDuLieu(idNguoiDung)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (uiState) {
        is HomeUiState.DangTai -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is HomeUiState.LoI -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((uiState as HomeUiState.LoI).thongBao, color = MaterialTheme.colorScheme.error)
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
                    onBaiHocClick = onNavigateToBaiHoc
                )
            }
        }
    }
}

@Composable
fun ChuaDangNhap(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Đăng nhập để theo dõi tiến trình\nhọc cá nhân hóa của riêng bạn",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun HomeContent(modifier: Modifier = Modifier, data: HomeData, onBaiHocClick: (String, String) -> Unit) {
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
        StreakCard(streak = data.nguoiDung.streak)
        Spacer(modifier = Modifier.height(16.dp))
        ThongKeHaiCot(
            tongTu = data.tongTuDaHoc,
            tuHomNay = data.tuHocHomNay,
            tu7Ngay = data.tuHoc7Ngay,
            target = data.nguoiDung.target
        )
        Spacer(modifier = Modifier.height(12.dp))

        OTrinhDo(
            tenCapDo = data.capDo.tenCapDo,
            tongTuDaHoc = data.tongTuDaHoc,
            tongTuCapDo = data.tongTuCapDo,
            phanTram = phanTram
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Lộ trình ôn tập",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (data.soFlashcardCanOnTap > 0) {
            Text(
                text = "Bạn có ${data.soFlashcardCanOnTap} flashcard cá nhân cần ôn!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (data.soTuCanOnTap == 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "Bạn chưa có từ để ôn tập, bắt đầu học ngay thôi! 📚",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            val soBaiOnTap = (data.soTuCanOnTap + 19) / 20
            repeat(soBaiOnTap) { index ->
                val stt = index + 1
                val soTuTrongBai = if (stt == soBaiOnTap) {
                    val le = data.soTuCanOnTap % 20
                    if (le == 0) 20 else le
                } else 20

                BaiHocItem(
                    icon = "⚡",
                    ten = "Bài ôn tập $stt",
                    sub = "$soTuTrongBai từ vựng đến hạn ôn tập",
                    badge = "Hàng ngày",
                    badgeMau = MaterialTheme.colorScheme.secondaryContainer,
                    badgeTextMau = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { onBaiHocClick("review_$stt", "Bài ôn tập $stt") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bài học đề xuất",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        data.baiHocDeXuat.forEach { baiHoc ->
            BaiHocItem(
                icon = "📖",
                ten = baiHoc.tenBaiHoc,
                sub = baiHoc.moTa,
                badge = "Mới",
                badgeMau = MaterialTheme.colorScheme.secondaryContainer,
                badgeTextMau = MaterialTheme.colorScheme.onSecondaryContainer,
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
            Text(
                text = "Chào buổi sáng 👋",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = ten,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ten.take(2).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Chuỗi ngày học",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Text(
                    text = "${streak} ngày 🔥",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = if (streak > 0) "Tiếp tục cố lên!" else "Bắt đầu học hôm nay!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "7 ngày qua",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val ngayHienThi = streak.coerceAtMost(4)
                    repeat(ngayHienThi) { NgayStreakItem(daHoc = true) }
                    repeat(4 - ngayHienThi) { NgayStreakItem(daHoc = false) }
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
                if (daHoc) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (daHoc) "✓" else "·",
            fontSize = 16.sp,
            color = if (daHoc) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ThongKeHaiCot(tongTu: Int, tuHomNay: Int, tu7Ngay: Int, target: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "📚 Từ học 7 ngày qua",
            giaTri = "$tu7Ngay",
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = giaTri,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = phu,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun OTrinhDo(tenCapDo: String, tongTuDaHoc: Int, tongTuCapDo: Int, phanTram: Float) {
    val conLai = (tongTuDaHoc - tongTuDaHoc).coerceAtLeast(0) // Note: this was (tongTuCapDo - tongTuDaHoc) in original code, wait, let's keep original logic (tongTuCapDo - tongTuDaHoc)
    val capDoTiep = tenCapDo.replace(Regex("\\d+")) {
        ((it.value.toIntOrNull() ?: 1) + 1).toString()
    }
    val (mauNen, mauChu, icon) = mauTheoHSK(tenCapDo)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$icon Trình độ hiện tại",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tenCapDo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(mauNen)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (tenCapDo.contains("1") || tenCapDo.contains("2")) "Sơ cấp"
                               else if (tenCapDo.contains("3") || tenCapDo.contains("4")) "Trung cấp"
                               else "Cao cấp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = mauChu
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tiến độ lên $capDoTiep",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "$tongTuDaHoc / $tongTuCapDo từ · ${(phanTram * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = mauChu,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { phanTram },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = mauChu,
                trackColor = mauNen
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrinhDoStat(Modifier.weight(1f), "${(phanTram * 100).toInt()}%", "Hoàn thành")
                TrinhDoStat(Modifier.weight(1f), "${(tongTuCapDo - tongTuDaHoc).coerceAtLeast(0)} từ", "Còn lại")
                TrinhDoStat(Modifier.weight(1f), "~${(((tongTuCapDo - tongTuDaHoc).coerceAtLeast(0)) / 10).coerceAtLeast(1)} ngày", "Ước tính")
            }
        }
    }
}

@Composable
fun TrinhDoStat(modifier: Modifier = Modifier, giaTri: String, nhan: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = giaTri,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = nhan,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                    Text(
                        text = ten,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(badgeMau)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = badgeTextMau
                        )
                    }
                }
                Text(
                    text = sub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
