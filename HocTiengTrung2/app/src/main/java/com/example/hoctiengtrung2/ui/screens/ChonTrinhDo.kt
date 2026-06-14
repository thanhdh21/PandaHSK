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
import com.example.hoctiengtrung2.ui.viewmodel.CapDoUiState
import com.example.hoctiengtrung2.ui.viewmodel.CapDoViewModel

fun mauTheoHSK(tenCapDo: String): Triple<Color, Color, String> = when {
    tenCapDo.contains("1") -> Triple(TimPastel, TimDam, "✨")
    tenCapDo.contains("2") -> Triple(TimPastel, TimTrung, "💫")
    tenCapDo.contains("3") -> Triple(TimMo, TimDam, "💎")
    tenCapDo.contains("4") -> Triple(TimPastel, TimTrung, "⭐")
    tenCapDo.contains("5") -> Triple(HongNhat, HongDam, "🔥")
    else -> Triple(HongNhat, HongDam, "👑")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChonTrinhDoScreen(
    onChonCapDo: (String) -> Unit,
    onQuayLai: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CapDoViewModel = viewModel()
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
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text("Bạn đang học ở cấp độ nào?", fontSize = 14.sp, color = TimNhat)
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is CapDoUiState.DangTai -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TimTrung)
                    }
                    is CapDoUiState.LoI -> {
                        Text(
                            (uiState as CapDoUiState.LoI).thongBao,
                            modifier = Modifier.align(Alignment.Center),
                            color = HongDam
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

@Composable
fun CapDoCard(capDo: CapDo, onClick: () -> Unit) {
    val (mauNen, mauChu, icon) = mauTheoHSK(capDo.tenCapDo)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NenTrang),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, TimMo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(mauNen),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(capDo.tenCapDo, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TimDam)
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
