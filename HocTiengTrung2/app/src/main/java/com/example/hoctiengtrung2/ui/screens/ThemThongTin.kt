package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.TrangThaiDangNhap
import com.example.hoctiengtrung2.ui.viewmodel.XacThucViewModel
import com.example.hoctiengtrung2.utils.SessionManager

@Composable
fun ThemThongTin(
    idNguoiDung: String,
    onHoanTat: () -> Unit,
    viewModel: XacThucViewModel = viewModel()
) {
    var ten by remember { mutableStateOf("") }
    var target by remember { mutableFloatStateOf(10f) }

    val context = LocalContext.current
    val trangThai by viewModel.trangThai.collectAsState()

    LaunchedEffect(trangThai) {
        if (trangThai is TrangThaiDangNhap.ThanhCong) {
            val nguoiDung = (trangThai as TrangThaiDangNhap.ThanhCong).nguoiDung
            // Lưu thông tin đăng nhập vào Session để Trang chủ có thể lấy được idNguoiDung
            SessionManager.luuDangNhap(context, nguoiDung.idNguoiDung)
            onHoanTat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐼", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Thêm thông tin",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = TimDam
        )
        Text(
            "Giúp chúng tôi cá nhân hóa\ntrải nghiệm học của bạn",
            fontSize = 13.sp,
            color = TimNhat,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tên
        OutlinedTextField(
            value = ten,
            onValueChange = { ten = it; viewModel.resetState() },
            label = { Text("Tên hiển thị") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Target - thanh kéo
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mục tiêu từ/ngày", fontSize = 14.sp, color = TimDam, fontWeight = FontWeight.Medium)
                Text(
                    "${target.toInt()} từ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimTrung
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = target,
                onValueChange = { target = it },
                valueRange = 1f..30f,
                steps = 28,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = TimTrung,
                    activeTrackColor = TimTrung,
                    inactiveTrackColor = TimNhat.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1", fontSize = 12.sp, color = TimNhat)
                Text("30", fontSize = 12.sp, color = TimNhat)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gợi ý mức target
        Text(
            text = when {
                target.toInt() <= 5 -> "😊 Nhẹ nhàng — phù hợp người mới"
                target.toInt() <= 10 -> "📚 Vừa phải — duy trì đều đặn"
                target.toInt() <= 20 -> "💪 Chăm chỉ — tiến bộ nhanh"
                else -> "🔥 Cường độ cao — dành cho người quyết tâm"
            },
            fontSize = 13.sp,
            color = TimNhat,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (trangThai is TrangThaiDangNhap.Loi) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                (trangThai as TrangThaiDangNhap.Loi).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.themThongTin(
                    idNguoiDung = idNguoiDung,
                    ten = ten,
                    tuoi = 0,
                    target = target.toInt()
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TimTrung),
            enabled = trangThai !is TrangThaiDangNhap.Load
        ) {
            if (trangThai is TrangThaiDangNhap.Load) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Hoàn tất", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}
