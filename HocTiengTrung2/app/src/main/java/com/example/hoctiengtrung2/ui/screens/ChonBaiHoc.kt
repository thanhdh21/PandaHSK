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
import com.example.hoctiengtrung2.data.model.BaiHoc
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.BaiHocUiState
import com.example.hoctiengtrung2.ui.viewmodel.BaiHocViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChonBaiHocScreen(
    idCapDo: String,
    onChonBaiHoc: (String, String) -> Unit,
    onQuayLai: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BaiHocViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idCapDo) {
        viewModel.layDanhSachBaiHoc(idCapDo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bài học HSK $idCapDo", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TimTrung)
                }
                is BaiHocUiState.LoI -> {
                    Text(
                        (uiState as BaiHocUiState.LoI).thongBao,
                        modifier = Modifier.align(Alignment.Center),
                        color = HongDam
                    )
                }
                is BaiHocUiState.ThanhCong -> {
                    val list = (uiState as BaiHocUiState.ThanhCong).danhSach
                    
                    if (list.isEmpty()) {
                        Text(
                            "Không tìm thấy bài học nào cho trình độ này",
                            modifier = Modifier.align(Alignment.Center),
                            color = TimMo
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { baiHoc ->
                                BaiHocCard(
                                    baiHoc = baiHoc,
                                    onClick = {
                                        onChonBaiHoc(baiHoc.idBaiHoc, baiHoc.tenBaiHoc)
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
fun BaiHocCard(baiHoc: BaiHoc, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(TimPastel),
                contentAlignment = Alignment.Center
            ) {
                Text("📖", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(baiHoc.tenBaiHoc, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TimDam)
                Text(baiHoc.moTa, fontSize = 12.sp, color = TimMo)
            }
        }
    }
}
