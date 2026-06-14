package com.example.hoctiengtrung2.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.data.model.TuVung
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.TuVungUiState
import com.example.hoctiengtrung2.ui.viewmodel.TuVungViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.hoctiengtrung2.utils.SessionManager
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuVungScreen(
    idBaiHoc: String,
    tenBaiHoc: String,
    onQuayLai: () -> Unit,
    onBatDauOnTap: () -> Unit,
    viewModel: TuVungViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val idNguoiDung = remember(context) { SessionManager.layIdNguoiDung(context) }

    LaunchedEffect(idBaiHoc, idNguoiDung) {
        viewModel.layDanhSachTuVung(idBaiHoc, idNguoiDung)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tenBaiHoc, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Flashcard học từ mới", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onQuayLai) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is TuVungUiState.DangTai -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TimTrung)
                }
                is TuVungUiState.LoI -> {
                    Text(
                        (uiState as TuVungUiState.LoI).thongBao,
                        modifier = Modifier.align(Alignment.Center),
                        color = HongDam
                    )
                }
                is TuVungUiState.ThanhCong -> {
                    val danhSach = (uiState as TuVungUiState.ThanhCong).danhSach
                    FlashcardContent(
                        danhSach = danhSach,
                        idNguoiDung = idNguoiDung,
                        isReview = idBaiHoc.startsWith("review"),
                        onWordFinished = { idTuVung, laTuMoi ->
                            viewModel.hoanThanhTuVung(idNguoiDung, idTuVung, laTuMoi)
                        },
                        onFinished = onBatDauOnTap
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardContent(
    danhSach: List<TuVung>,
    idNguoiDung: String,
    isReview: Boolean,
    onWordFinished: (String, Boolean) -> Unit,
    onFinished: () -> Unit
) {
    var viTriHienTai by remember { mutableIntStateOf(0) }
    var daLat by remember { mutableStateOf(false) }
    val finishedWords = remember { mutableSetOf<String>() }

    val tuHienTai = danhSach[viTriHienTai]

    fun markWordAsFinished() {
        if (tuHienTai.idTuVung !in finishedWords) {
            onWordFinished(tuHienTai.idTuVung, !isReview)
            finishedWords.add(tuHienTai.idTuVung)
        }
    }

    fun nextCard() {
        markWordAsFinished()
        if (viTriHienTai < danhSach.size - 1) {
            viTriHienTai++
            daLat = false
        } else {
            onFinished()
        }
    }

    fun prevCard() {
        if (viTriHienTai > 0) {
            viTriHienTai--
            daLat = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { (viTriHienTai + 1).toFloat() / danhSach.size },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = TimTrung,
                trackColor = TimPastel
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "${viTriHienTai + 1} / ${danhSach.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TimTrung
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Card Area
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MuiTenDieuHuong(
                icon = "‹",
                enabled = viTriHienTai > 0,
                onClick = { prevCard() }
            )

            TheFlashcardTuVung(
                modifier = Modifier.weight(1f),
                tuVung = tuHienTai,
                daLat = daLat,
                onFlip = { daLat = !daLat },
                onSwipeLeft = { nextCard() },
                onSwipeRight = { prevCard() }
            )

            MuiTenDieuHuong(
                icon = "›",
                enabled = true, // Luôn enabled để bấm "Xong" ở cuối
                onClick = { nextCard() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Chạm để lật thẻ • Vuốt để chuyển từ",
            fontSize = 13.sp,
            color = TimMo,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { nextCard() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viTriHienTai == danhSach.size - 1) TimDam else TimTrung
            )
        ) {
            Text(
                if (viTriHienTai == danhSach.size - 1) "Bắt đầu ôn tập" else "Tiếp theo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TheFlashcardTuVung(
    modifier: Modifier = Modifier,
    tuVung: TuVung,
    daLat: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (daLat) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    var dragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .height(350.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .pointerInput(tuVung.idTuVung) { // Reset drag on new word
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragX < -150f) onSwipeLeft()
                        else if (dragX > 150f) onSwipeRight()
                        dragX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragX += dragAmount
                    }
                )
            }
            .clickable { onFlip() }
    ) {
        if (rotation <= 90f) {
            MatTruocTuVung(tuVung)
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }.fillMaxSize()) {
                MatSauTuVung(tuVung)
            }
        }
    }
}

@Composable
fun MatTruocTuVung(tuVung: TuVung) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = TimTrung),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tuVung.hanTu,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chạm để xem nghĩa",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MatSauTuVung(tuVung: TuVung) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, TimPastel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tuVung.pinyin,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = TimDam
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tuVung.nghia,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TimDam,
                textAlign = TextAlign.Center
            )
            if (tuVung.hanViet.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hán Việt: ${tuVung.hanViet}",
                    fontSize = 16.sp,
                    color = TimNhat
                )
            }
        }
    }
}

@Composable
fun MuiTenDieuHuong(icon: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) TimPastel else Color.Transparent)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            color = if (enabled) TimTrung else Color.Transparent,
            fontWeight = FontWeight.Bold
        )
    }
}
