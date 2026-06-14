package com.example.hoctiengtrung2.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.TrangThaiDangNhap
import com.example.hoctiengtrung2.ui.viewmodel.XacThucViewModel

import androidx.compose.ui.platform.LocalContext
import com.example.hoctiengtrung2.utils.NetworkUtils

@Composable
fun DangKyScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToDangNhap: () -> Unit,
    onQuayLai: () -> Unit = {},
    viewModel: XacThucViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    BackHandler { onQuayLai() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
 
    val trangThai by viewModel.trangThai.collectAsState()
 
    LaunchedEffect(trangThai) {
        if (trangThai is TrangThaiDangNhap.ThanhCong) {
            onRegisterSuccess((trangThai as TrangThaiDangNhap.ThanhCong).nguoiDung.idNguoiDung)
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
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onQuayLai) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }
        }
 
        Text("🐼", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tạo tài khoản mới",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Chỉ cần tài khoản và mật khẩu\nđể bắt đầu!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
 
        Spacer(modifier = Modifier.height(32.dp))
 
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; viewModel.resetState(); errorText = "" },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
 
        Spacer(modifier = Modifier.height(12.dp))
 
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.resetState(); errorText = "" },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                keyboardController?.hide()
                focusManager.clearFocus()
            })
        )
 
        val displayedError = remember(trangThai, errorText) {
            if (errorText.isNotEmpty()) {
                errorText
            } else if (trangThai is TrangThaiDangNhap.Loi) {
                (trangThai as TrangThaiDangNhap.Loi).message
            } else {
                ""
            }
        }

        if (displayedError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = displayedError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
 
        Spacer(modifier = Modifier.height(24.dp))
 
        Button(
            onClick = { 
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    errorText = "Không có kết nối mạng. Vui lòng kết nối mạng để đăng ký."
                } else {
                    errorText = ""
                    viewModel.dangKy(username, password)
                }
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
                    text = "Đăng ký",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateToDangNhap) {
            Text("Đã có tài khoản? Đăng nhập ngay", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        }
    }
}
