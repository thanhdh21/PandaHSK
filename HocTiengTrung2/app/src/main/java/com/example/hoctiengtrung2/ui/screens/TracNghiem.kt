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

@Composable
fun TracNghiemScreen(
    idBaiHoc: String,
    tenBaiHoc: String,
    onQuayLai: () -> Unit,
    onVeTrangChu: () -> Unit,
    viewModel: TracNghiemViewModel = viewModel()
) {
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idBaiHoc) {
        viewModel.taiDuLieu(idBaiHoc)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.dangTai -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TimTrung)
            }
            uiState.loI != null -> {
                Text(uiState.loI!!, modifier = Modifier.align(Alignment.Center), color = HongDam)
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
                    onChonDapAn = { dapAn -> viewModel.chonDapAn(dapAn, idNguoiDung) },
                    onTiepTheo = { viewModel.cauTiepTheo(idNguoiDung, idBaiHoc) }
                )
            }
        }
    }
}

@Composable
fun TracNghiemContent(
    uiState: TracNghiemUiState,
    tenBaiHoc: String,
    onBack: () -> Unit,
    onChonDapAn: (String) -> Unit,
    onTiepTheo: () -> Unit
) {
    val cauHoi = uiState.danhSachCauHoi[uiState.viTriHienTai]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TimTrung)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Thử thách ", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TimDam)
                Text(tenBaiHoc, fontSize = 12.sp, color = TimNhat)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(TimPastel)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${uiState.viTriHienTai + 1} / ${uiState.danhSachCauHoi.size}",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TimTrung
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thanh tiến độ
        LinearProgressIndicator(
            progress = { (uiState.viTriHienTai + 1).toFloat() / uiState.danhSachCauHoi.size },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
            color = TimTrung, trackColor = TimPastel
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Thẻ câu hỏi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TimTrung)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Từ này có nghĩa là gì?", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(cauHoi.hanTu, fontSize = 64.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(cauHoi.pinyin, fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
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
                    .background(if (dungRoi) XanhNhat else HongNhat)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (dungRoi) "✅ Chính xác!"
                    else "❌ Sai rồi! Đáp án đúng: ${cauHoi.dapAnDung}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dungRoi) XanhDam else HongDam,
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
                colors = ButtonDefaults.buttonColors(containerColor = TimTrung)
            ) {
                Text(
                    text = if (uiState.viTriHienTai < uiState.danhSachCauHoi.size - 1) "Tiếp theo ➡" else "Xem kết quả 🏆",
                    fontSize = 15.sp, fontWeight = FontWeight.Medium
                )
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
        TrangThaiDapAn.CHUA_CHON -> Triple(NenTrang, TimMo, TimDam)
        TrangThaiDapAn.DUNG -> Triple(XanhNhat, XanhDam, XanhDam)
        TrangThaiDapAn.SAI -> Triple(HongNhat, HongDam, HongDam)
        TrangThaiDapAn.MO -> Triple(NenTrang, TimMo, TimMo)
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
        Text(tieuDe, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = TimDam)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bạn đã hoàn thành bài trắc nghiệm", fontSize = 14.sp, color = TimNhat, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThongKeTN(Modifier.weight(1f), "$soDung", "✅ Đúng", XanhNhat, XanhDam)
            ThongKeTN(Modifier.weight(1f), "$soSai", "❌ Sai", HongNhat, HongDam)
            ThongKeTN(Modifier.weight(1f), "$phanTram%", "🏆 Điểm", TimPastel, TimTrung)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVeTrangChu,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TimTrung)
        ) { Text("Về trang chủ", fontSize = 15.sp, fontWeight = FontWeight.Medium) }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLamLai,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, TimTrung)
        ) { Text("Làm lại", fontSize = 15.sp, color = TimTrung, fontWeight = FontWeight.Medium) }
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
