package com.example.hoctiengtrung2.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.hoctiengtrung2.ui.theme.TimDam
import com.example.hoctiengtrung2.ui.theme.TimNhat
import com.example.hoctiengtrung2.ui.theme.TimTrung
import java.io.File
import java.io.FileOutputStream

@Composable
fun ManHinhChaoMung(
    onDangNhap: () -> Unit,
    onVaoLuon: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo
        Text("🐼", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Tiêu đề
        Text(
            text = "Học Tiếng Trung",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = TimDam
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mô tả
        Text(
            text = "Học từ vựng hiệu quả\nvới phương pháp Spaced Repetition",
            fontSize = 14.sp,
            color = TimNhat,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(56.dp))

        // Nút Đăng nhập
        Button(
            onClick = onDangNhap,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TimTrung)
        ) {
            Text(
                text = "Đăng nhập",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nút Vào luôn (Xem chế độ khách hoặc chuyển đến trang chủ)
        OutlinedButton(
            onClick = onVaoLuon,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, TimTrung)
        ) {
            Text(
                text = "Vào luôn",
                fontSize = 16.sp,
                color = TimTrung,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dòng hướng dẫn sử dụng liên kết tới file PDF cục bộ
        Text(
            text = "Hướng dẫn sử dụng",
            fontSize = 14.sp,
            color = TimTrung,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable {
                    openLocalPdf(context, "HDSD_PandaHSK.pdf")
                }
                .padding(8.dp)
        )
    }
}

private fun openLocalPdf(context: Context, assetName: String) {
    try {
        val cacheFile = File(context.cacheDir, assetName)
        if (!cacheFile.exists()) {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}