package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.TrangThaiDangNhap
import com.example.hoctiengtrung2.ui.viewmodel.XacThucViewModel
import com.example.hoctiengtrung2.data.model.NguoiDung
import com.example.hoctiengtrung2.utils.SessionManager

@Composable
fun DangNhap(
    onLoginSuccess: (NguoiDung) -> Unit,
    onNavigateToRegister: () -> Unit,
    onQuayLai: () -> Unit = {},
    viewModel: XacThucViewModel = viewModel()
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val trangThai by viewModel.trangThai.collectAsState()

    // Xử lý chuyển màn hình khi đăng nhập thành công
    LaunchedEffect(trangThai) {
        if (trangThai is TrangThaiDangNhap.ThanhCong) {
            val nguoiDung = (trangThai as TrangThaiDangNhap.ThanhCong).nguoiDung
            SessionManager.luuDangNhap(context, nguoiDung.idNguoiDung)
            onLoginSuccess(nguoiDung)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onQuayLai) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TimTrung)
            }
        }

        Text("🐼", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Học Tiếng Trung", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TimDam)
        Text("Đăng nhập để tiếp tục", fontSize = 13.sp, color = TimNhat, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; viewModel.resetState() },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.resetState() },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        if (trangThai is TrangThaiDangNhap.Loi) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                (trangThai as TrangThaiDangNhap.Loi).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.dangNhap(username, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TimTrung),
            enabled = trangThai !is TrangThaiDangNhap.Load
        ) {
            if (trangThai is TrangThaiDangNhap.Load) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay", color = TimTrung, fontSize = 14.sp)
        }
    }
}