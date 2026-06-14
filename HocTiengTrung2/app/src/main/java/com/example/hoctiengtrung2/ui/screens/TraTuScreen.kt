package com.example.hoctiengtrung2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoctiengtrung2.data.remote.TuVungInternetDto
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel // Hoặc ViewModel bạn quản lý mạng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraTuScreen(
    idNguoiDung: String,
    onQuayLai: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tuKhoa by remember { mutableStateOf("") }
    var tuKhoaDangTim by remember { mutableStateOf("") }
    var danhSachKetQua by remember { mutableStateOf<List<TuVungInternetDto>>(emptyList()) }
    var dangTaiDuLieu by remember { mutableStateOf(false) }

    val homeRepository = remember {
        com.example.hoctiengtrung2.data.repository.HomeRepository(
            com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource()
        )
    }
    val tuVungRepository = remember {
        com.example.hoctiengtrung2.data.repository.TuVungRepository(
            com.example.hoctiengtrung2.data.remote.TuVungRemoteDataSource()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tra từ Internet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TimDam
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onQuayLai) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TimDam)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NenTrang)
            )
        },
        containerColor = NenTrang
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. THANH Ô TÌM KIẾM
            OutlinedTextField(
                value = tuKhoa,
                onValueChange = { tuKhoa = it },
                placeholder = { Text("Nhập từ cần tra (Hán, Pinyin, Việt)...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TimTrung) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TimTrung,
                    unfocusedBorderColor = TimMo,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = TimDam
                ),
                singleLine = true,
                trailingIcon = {
                    if (tuKhoa.isNotBlank()) {
                        Button(
                            onClick = {
                                if (tuKhoa.isNotBlank()) {
                                    dangTaiDuLieu = true
                                    tuKhoaDangTim = tuKhoa
                                    scope.launch {
                                        danhSachKetQua = homeRepository.layNghinTuTuInternet(tuKhoa)
                                        dangTaiDuLieu = false
                                        if (danhSachKetQua.isEmpty()) {
                                            Toast.makeText(context, "Không tìm thấy kết quả!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TimDam),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Text("Tìm", color = Color.White)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dangTaiDuLieu) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TimDam)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(danhSachKetQua) { tuVung ->
                        WordItem(
                            tuVung = tuVung,
                            tuKhoa = tuKhoaDangTim,
                            idNguoiDung = idNguoiDung,
                            onSave = {
                                val thanhCong = tuVungRepository.luuFlashcardCaNhan(
                                    idNguoiDung = idNguoiDung,
                                    chuHan = tuVung.hanzi,
                                    pinyin = tuVung.pinyin,
                                    nghiaViet = tuVung.meaning
                                )
                                if (thanhCong) {
                                    Toast.makeText(context, "Đã lưu vào Flashcard! ❤️", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Lỗi khi lưu từ!", Toast.LENGTH_SHORT).show()
                                }
                                thanhCong
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordItem(
    tuVung: TuVungInternetDto,
    tuKhoa: String,
    idNguoiDung: String,
    onSave: suspend () -> Boolean
) {
    var daLuuCard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Highlight Hán tự
                Text(
                    text = highlightText(tuVung.hanzi, tuKhoa),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimDam
                )
                
                // Highlight Pinyin
                Text(
                    text = highlightText(tuVung.pinyin, tuKhoa),
                    fontSize = 16.sp,
                    color = TimTrung,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Highlight Nghĩa
                Text(
                    text = highlightText(tuVung.meaning, tuKhoa),
                    fontSize = 15.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            if (idNguoiDung.isNotBlank() && idNguoiDung != "khach") {
                IconButton(
                    onClick = {
                        if (!daLuuCard) {
                            scope.launch {
                                if (onSave()) {
                                    daLuuCard = true
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (daLuuCard) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Lưu",
                        tint = if (daLuuCard) Color.Red else TimTrung,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun highlightText(fullText: String, query: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        if (query.isEmpty()) {
            append(fullText)
        } else {
            var startIndex = 0
            while (startIndex < fullText.length) {
                val index = fullText.indexOf(query, startIndex, ignoreCase = true)
                if (index == -1) {
                    append(fullText.substring(startIndex))
                    break
                } else {
                    append(fullText.substring(startIndex, index))
                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append(fullText.substring(index, index + query.length))
                    }
                    startIndex = index + query.length
                }
            }
        }
    }
}
