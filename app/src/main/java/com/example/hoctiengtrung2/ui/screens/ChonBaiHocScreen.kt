package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.BaiHocUiState
import com.example.hoctiengtrung2.ui.viewmodel.BaiHocViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.hoctiengtrung2.ui.viewmodel.BaiHocVoiTienDo
import com.example.hoctiengtrung2.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChonBaiHocScreen(
    idCapDo: String,
    onChonBaiHoc: (String, String) -> Unit,
    onQuayLai: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BaiHocViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)

    LaunchedEffect(idCapDo, idNguoiDung) {
        viewModel.layDanhSachBaiHoc(idCapDo, idNguoiDung)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val levelNumber = idCapDo.replace("hsk", "", ignoreCase = true).trim()
                    Text("Bài học HSK $levelNumber", fontSize = 18.sp, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onQuayLai) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues).fillMaxSize()) {
            when (uiState) {
                is BaiHocUiState.DangTai -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is BaiHocUiState.LoI -> {
                    Text(
                        (uiState as BaiHocUiState.LoI).thongBao,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is BaiHocUiState.ThanhCong -> {
                    val list = (uiState as BaiHocUiState.ThanhCong).danhSach
                    
                    if (list.isEmpty()) {
                        Text(
                            text = "Không tìm thấy bài học nào cho trình độ này",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { item ->
                                BaiHocCard(
                                    item = item,
                                    onClick = {
                                        onChonBaiHoc(item.baiHoc.idBaiHoc, item.baiHoc.tenBaiHoc)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BaiHocCard(item: BaiHocVoiTienDo, onClick: () -> Unit) {
    val baiHoc = item.baiHoc
    val (mauNen, mauChu, icon) = mauTheoHSK(baiHoc.idCapDo)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(mauNen),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = baiHoc.tenBaiHoc,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.soTuDaHoc}/${baiHoc.soTu} từ",
                        fontSize = 11.sp,
                        color = mauChu,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = baiHoc.moTa,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.phanTram },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = mauChu,
                    trackColor = mauNen
                )
            }
        }
    }
}
