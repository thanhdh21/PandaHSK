package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.data.model.CapDo
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.CapDoUiState
import com.example.hoctiengtrung2.ui.viewmodel.CapDoViewModel



/**
 * Màn hình cho phép người dùng chọn trình độ học tiếng Trung (HSK).
 *
 * Màn hình này hiển thị danh sách các cấp độ dưới dạng lưới thẻ (grid),
 * mỗi thẻ đại diện cho một cấp HSK. Người dùng bấm vào thẻ để chọn trình độ
 * và chuyển sang màn hình học tương ứng.
 *
 * @param onChonCapDo Callback được gọi khi người dùng chọn một cấp độ;
 *                    truyền vào [idCapDo] của cấp độ đã chọn để điều hướng tiếp theo.
 * @param onQuayLai   Callback được gọi khi người dùng nhấn nút "Quay lại" trên thanh AppBar.
 * @param modifier    [Modifier] tùy chỉnh bố cục bên ngoài, mặc định là [Modifier].
 * @param viewModel   [CapDoViewModel] cung cấp trạng thái UI; mặc định được tạo tự động
 *                    qua [AppViewModelProvider.Factory].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChonTrinhDoScreen(
    onChonCapDo: (String) -> Unit,
    onQuayLai: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CapDoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn trình độ", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onQuayLai) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text("Bạn đang học ở cấp độ nào?", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is CapDoUiState.DangTai -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                    }
                    is CapDoUiState.LoI -> {
                        Text(
                            (uiState as CapDoUiState.LoI).thongBao,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is CapDoUiState.ThanhCong -> {
                        val levels = (uiState as CapDoUiState.ThanhCong).danhSach
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(levels) { capDo ->
                                CapDoCard(
                                    capDo = capDo,
                                    onClick = { onChonCapDo(capDo.idCapDo) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Thẻ hiển thị thông tin của một cấp độ HSK.
 *
 * Thẻ này trình bày tên cấp độ, mô tả, icon emoji và màu sắc đặc trưng
 * tương ứng với từng mức HSK. Toàn bộ thẻ có thể bấm được để chọn cấp độ.
 *
 * Lý do tách thành composable riêng: để tái sử dụng trong [LazyVerticalGrid]
 * ở [ChonTrinhDoScreen] và dễ dàng kiểm thử độc lập.
 *
 * @param capDo  Đối tượng [CapDo] chứa thông tin cấp độ cần hiển thị
 *               (tên, mô tả, idCapDo, v.v.).
 * @param onClick Callback được gọi khi người dùng bấm vào thẻ.
 */
@Composable
fun CapDoCard(capDo: CapDo, onClick: () -> Unit) {
    val (mauNen, mauChu, icon) = mauTheoHSK(capDo.tenCapDo)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(mauNen),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = capDo.tenCapDo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(mauNen)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(capDo.moTa, fontSize = 11.sp, color = mauChu, fontWeight = FontWeight.Medium)
            }
        }
    }
}
