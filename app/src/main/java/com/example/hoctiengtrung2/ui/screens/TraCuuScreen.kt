package com.example.hoctiengtrung2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.mauTheoHSK
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.ui.viewmodel.TaoFlashcardUiState
import com.example.hoctiengtrung2.ui.viewmodel.TaoFlashcardViewModel
import com.example.hoctiengtrung2.ui.viewmodel.TraCuuUiState
import com.example.hoctiengtrung2.ui.viewmodel.TraCuuViewModel
import com.example.hoctiengtrung2.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraCuuScreen(
    viewModel: TraCuuViewModel = viewModel(factory = AppViewModelProvider.Factory),
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    flashcardViewModel: TaoFlashcardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val homeUiState by homeViewModel.uiState.collectAsState()
    val flashcardUiState by flashcardViewModel.uiState.collectAsState()

    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotBlank()) {
            homeViewModel.layDuLieu(idNguoiDung)
        }
    }

    // Xử lý thông báo khi tạo Flashcard thành công
    LaunchedEffect(flashcardUiState) {
        if (flashcardUiState is TaoFlashcardUiState.ThanhCong) {
            Toast.makeText(context, "Đã thêm vào Flashcard cá nhân!", Toast.LENGTH_SHORT).show()
            flashcardViewModel.resetState()
        } else if (flashcardUiState is TaoFlashcardUiState.Loi) {
            Toast.makeText(context, (flashcardUiState as TaoFlashcardUiState.Loi).thongBao, Toast.LENGTH_SHORT).show()
            flashcardViewModel.resetState()
        }
    }

    val currentLevel = (homeUiState as? HomeUiState.ThanhCong)?.data?.capDo?.tenCapDo ?: "HSK1"
    val (_, levelColor, _) = mauTheoHSK(currentLevel)

    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tra cứu từ điển",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập từ hoặc câu cần dịch") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.traCuu(query) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is TraCuuUiState.DangTai -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is TraCuuUiState.ThanhCong -> {
                        val result = state.ketQua
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            // Header hiển thị từ đang tra và nút thêm Flashcard
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.query,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!result.hanViet.isNullOrEmpty()) {
                                        Text(
                                            text = result.hanViet.uppercase(),
                                            fontSize = 16.sp,
                                            color = levelColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        if (idNguoiDung.isNotBlank()) {
                                            flashcardViewModel.taoFlashcard(
                                                idNguoiDung = idNguoiDung,
                                                hanTu = result.query,
                                                pinyin = result.hanViet ?: "",
                                                nghia = result.nghia
                                            )
                                        } else {
                                            Toast.makeText(context, "Vui lòng đăng nhập để lưu thẻ!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        .size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Thêm vào Flashcard",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            KetQuaCard(
                                label = "Bản dịch",
                                content = result.nghia,
                                icon = Icons.Default.Translate,
                                accentColor = MaterialTheme.colorScheme.primary
                            )

                            if (!result.loaiTu.isNullOrEmpty()) {
                                KetQuaCard(
                                    label = "Loại từ",
                                    content = result.loaiTu,
                                    icon = Icons.Default.MenuBook,
                                    accentColor = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (!result.hanViet.isNullOrEmpty()) {
                                KetQuaCard(
                                    label = "Phiên âm Hán Việt",
                                    content = result.hanViet.uppercase(),
                                    icon = Icons.Default.MenuBook,
                                    accentColor = levelColor
                                )
                            }

                            if (result.cauLienQuan.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Ví dụ thực tế",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                result.cauLienQuan.forEach { pair ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = pair.original,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = pair.translation,
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                    is TraCuuUiState.Loi -> {
                        Text(
                            state.thongBao,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is TraCuuUiState.Cho -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Nhập nội dung để bắt đầu dịch",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KetQuaCard(
    label: String,
    content: String,
    icon: ImageVector,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            label,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
