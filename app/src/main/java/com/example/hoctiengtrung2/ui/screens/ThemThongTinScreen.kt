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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.TrangThaiDangNhap
import com.example.hoctiengtrung2.ui.viewmodel.XacThucViewModel
import com.example.hoctiengtrung2.utils.SessionManager

@Composable
fun ThemThongTinScreen(
    idNguoiDung: String,
    onHoanTat: () -> Unit,
    viewModel: XacThucViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var ten by remember { mutableStateOf("") }
    var target by remember { mutableFloatStateOf(10f) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val trangThai by viewModel.trangThai.collectAsState()

    LaunchedEffect(trangThai) {
        if (trangThai is TrangThaiDangNhap.ThanhCong) {
            val nguoiDung = (trangThai as TrangThaiDangNhap.ThanhCong).nguoiDung
            SessionManager.luuDangNhap(context, nguoiDung.idNguoiDung)
            onHoanTat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐼", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thêm thông tin",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Giúp chúng tôi cá nhân hóa\ntrải nghiệm học của bạn",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = ten,
            onValueChange = { ten = it; viewModel.resetState() },
            label = { Text("Tên hiển thị") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                keyboardController?.hide()
                focusManager.clearFocus()
            })
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mục tiêu từ/ngày", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Text(
                    text = "${target.toInt()} từ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = target,
                onValueChange = { target = it },
                valueRange = 1f..30f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Text("30", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                target.toInt() <= 5 -> "😊 Nhẹ nhàng — phù hợp người mới"
                target.toInt() <= 10 -> "📚 Vừa phải — duy trì đều đặn"
                target.toInt() <= 20 -> "💪 Chăm chỉ — tiến bộ nhanh"
                else -> "🔥 Cường độ cao — dành cho người quyết tâm"
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (trangThai is TrangThaiDangNhap.Loi) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = (trangThai as TrangThaiDangNhap.Loi).message,
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
                    target = target.toInt()
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = trangThai !is TrangThaiDangNhap.Load
        ) {
            if (trangThai is TrangThaiDangNhap.Load) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Hoàn tất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
