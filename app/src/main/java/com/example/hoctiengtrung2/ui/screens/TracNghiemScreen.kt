package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.TracNghiemViewModel
import com.example.hoctiengtrung2.ui.viewmodel.TracNghiemUiState
import com.example.hoctiengtrung2.ui.viewmodel.TrangThaiDapAn
import com.example.hoctiengtrung2.utils.SessionManager
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider

@Composable
fun TracNghiemScreen(
    idBaiHoc: String,
    tenBaiHoc: String,
    onQuayLai: () -> Unit,
    onVeTrangChu: () -> Unit,
    viewModel: TracNghiemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idBaiHoc, idNguoiDung) {
        viewModel.taiDuLieu(idBaiHoc, idNguoiDung)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.dangTai -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            }
            uiState.loI != null -> {
                Text(uiState.loI!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            }
            uiState.daHoanThanh -> {
                KetQuaTracNghiem(
                    tong = uiState.danhSachCauHoi.size,
                    soDung = uiState.soCauDung,
                    soSai = uiState.soCauSai,
                    onVeTrangChu = onVeTrangChu,
                    onLamLai = { viewModel.lamLai() }
                )
            }
            uiState.danhSachCauHoi.isNotEmpty() -> {
                TracNghiemContent(
                    uiState = uiState,
                    tenBaiHoc = tenBaiHoc,
                    onBack = onQuayLai,
                    onChonDapAn = { dapAn -> viewModel.chonDapAn(dapAn, idNguoiDung, idBaiHoc.startsWith("review")) },
                    onTiepTheo = { viewModel.cauTiepTheo(idNguoiDung, idBaiHoc) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracNghiemContent(
    uiState: TracNghiemUiState,
    tenBaiHoc: String,
    onBack: () -> Unit,
    onChonDapAn: (String) -> Unit,
    onTiepTheo: () -> Unit
) {
    val cauHoi = uiState.danhSachCauHoi[uiState.viTriHienTai]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thử thách",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tenBaiHoc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.viTriHienTai + 1} / ${uiState.danhSachCauHoi.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Thanh tiến độ
            LinearProgressIndicator(
                progress = { (uiState.viTriHienTai + 1).toFloat() / uiState.danhSachCauHoi.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ câu hỏi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Từ này có nghĩa là gì?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(cauHoi.hanTu, fontSize = 64.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(cauHoi.pinyin, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 đáp án dạng 2x2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cauHoi.cacDapAn.chunked(2).forEach { hang ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        hang.forEach { dapAn ->
                            val trangThai = when {
                                uiState.dapAnDaChon == null -> TrangThaiDapAn.CHUA_CHON
                                dapAn == cauHoi.dapAnDung -> TrangThaiDapAn.DUNG
                                dapAn == uiState.dapAnDaChon -> TrangThaiDapAn.SAI
                                else -> TrangThaiDapAn.MO
                            }

                            DapAnItem(
                                modifier = Modifier.weight(1f),
                                dapAn = dapAn,
                                trangThai = trangThai,
                                onClick = { onChonDapAn(dapAn) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông báo đúng/sai
            if (uiState.dapAnDaChon != null) {
                val dungRoi = uiState.dapAnDaChon == cauHoi.dapAnDung
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dungRoi) TimPastel else HongNhat)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (dungRoi) "✅ Chính xác!"
                        else "❌ Sai rồi! Đáp án đúng: ${cauHoi.dapAnDung}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (dungRoi) TimDam else HongDam,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút Tiếp theo (chỉ hiện sau khi chọn)
            if (uiState.dapAnDaChon != null) {
                Button(
                    onClick = onTiepTheo,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (uiState.viTriHienTai < uiState.danhSachCauHoi.size - 1) "Tiếp theo ➡" else "Xem kết quả 🏆",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DapAnItem(
    modifier: Modifier = Modifier,
    dapAn: String,
    trangThai: TrangThaiDapAn,
    onClick: () -> Unit
) {
    val (mauNen, mauVien, mauChu) = when (trangThai) {
        TrangThaiDapAn.CHUA_CHON -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurface
        )
        TrangThaiDapAn.DUNG -> Triple(TimPastel, TimDam, TimDam)
        TrangThaiDapAn.SAI -> Triple(HongNhat, HongDam, HongDam)
        TrangThaiDapAn.MO -> Triple(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
    Card(
        modifier = modifier.clickable(enabled = trangThai == TrangThaiDapAn.CHUA_CHON) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = mauNen),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, mauVien)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(dapAn, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = mauChu, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun KetQuaTracNghiem(
    tong: Int,
    soDung: Int,
    soSai: Int,
    onVeTrangChu: () -> Unit,
    onLamLai: () -> Unit
) {
    val phanTram = if (tong > 0) (soDung.toFloat() / tong * 100).toInt() else 0
    val (emoji, tieuDe) = when {
        phanTram >= 80 -> "🎉" to "Xuất sắc!"
        phanTram >= 60 -> "👍" to "Tốt lắm!"
        else -> "💪" to "Cố lên!"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tieuDe,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn đã hoàn thành bài trắc nghiệm",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThongKeTN(Modifier.weight(1f), "$soDung", "✅ Đúng", TimPastel, TimDam)
            ThongKeTN(Modifier.weight(1f), "$soSai", "❌ Sai", HongNhat, HongDam)
            ThongKeTN(Modifier.weight(1f), "$phanTram%", "🏆 Điểm", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVeTrangChu,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Về trang chủ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLamLai,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Làm lại",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ThongKeTN(modifier: Modifier = Modifier, giaTri: String, nhan: String, mauNen: Color, mauChu: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = mauNen)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(giaTri, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = mauChu)
            Spacer(modifier = Modifier.height(4.dp))
            Text(nhan, fontSize = 11.sp, color = mauChu, textAlign = TextAlign.Center)
        }
    }
}
