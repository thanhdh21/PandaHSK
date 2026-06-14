package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaNhanScreen(
    onDangXuat: () -> Unit,
    onNavigateToCaiDat: () -> Unit,
    onNavigateToDoiMatKhau: () -> Unit,
    onNavigateToDangNhap: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val daDangNhap = SessionManager.daDangNhap(context)
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotBlank()) {
            viewModel.layDuLieu(idNguoiDung)
        }
    }

    if (!daDangNhap) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hồ sơ cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🐼", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chưa đăng nhập",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Đăng nhập để theo dõi tiến trình học\nvà xem thống kê chi tiết của bạn.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToDangNhap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Đăng nhập ngay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                actions = {
                    IconButton(onClick = onNavigateToCaiDat) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is HomeUiState.DangTai -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is HomeUiState.LoI -> {
                    Text(
                        (uiState as HomeUiState.LoI).thongBao,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is HomeUiState.ThanhCong -> {
                    val data = (uiState as HomeUiState.ThanhCong).data
                    CaNhanContent(data = data)
                }
            }
        }
    }
}

@Composable
fun CaNhanContent(
    data: com.example.hoctiengtrung2.data.repository.HomeData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.nguoiDung.tenNguoiDung.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = data.nguoiDung.tenNguoiDung,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Trình độ hiện tại: ${data.capDo.tenCapDo}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Level Progress
        LevelProgressBar(
            currentWords = data.tongTuDaHoc,
            totalWords = data.tongTuCapDo,
            levelName = data.capDo.tenCapDo
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Learning Calendar
        CalendarView(lichSuHoc = data.lichSuHoc)

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Chart 7 Days
        LearningChart(lichSu = data.lichSuHoatDong, target = data.nguoiDung.target)

        Spacer(modifier = Modifier.height(24.dp))

        // HSK Progress Line Chart (New)
        HSKProgressLineChart(
            lichSu = data.lichSuHoatDong,
            tongTuHienTai = data.tongTuDaHoc
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Phiên bản 1.1.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun LevelProgressBar(currentWords: Int, totalWords: Int, levelName: String) {
    val progress = (currentWords.toFloat() / totalWords.toFloat()).coerceIn(0f, 1f)
    val (mauNen, mauChu, icon) = mauTheoHSK(levelName)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$icon Tiến độ $levelName", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("$currentWords/$totalWords từ", fontSize = 12.sp, color = mauChu, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = mauChu,
                trackColor = mauNen,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (progress >= 1f) "Chúc mừng! Bạn đã hoàn thành mục tiêu $levelName" 
                       else "Cố lên! Còn ${(totalWords - currentWords)} từ nữa để chạm mốc tiếp theo",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CalendarView(lichSuHoc: Map<String, Int>) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    
    val tempCal = calendar.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
    val firstDayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = calendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    calendar = newCal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = monthName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                IconButton(onClick = {
                    val newCal = calendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    calendar = newCal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var dayCounter = 1
            for (i in 0..5) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (j in 0..6) {
                        val cellIndex = i * 7 + j
                        if (cellIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val loopCal = calendar.clone() as Calendar
                            loopCal.set(Calendar.DAY_OF_MONTH, dayCounter)
                            val dateKey = sdf.format(loopCal.time)
                            val hasStudied = lichSuHoc.containsKey(dateKey)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(if (hasStudied) MaterialTheme.colorScheme.primary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    fontSize = 12.sp,
                                    color = if (hasStudied) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            dayCounter++
                        }
                    }
                }
                if (dayCounter > daysInMonth) break
            }
        }
    }
}

@Composable
fun LearningChart(lichSu: List<com.example.hoctiengtrung2.data.model.LichSuHoatDong>, target: Int = 10) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displaySdf = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    val last7Days = (0..6).reversed().map { i ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
        val dateStr = sdf.format(cal.time)
        val dayData = lichSu.find { it.ngay == dateStr }
        displaySdf.format(cal.time) to (dayData?.soTuMoi ?: 0)
    }
    
    val maxCount = (last7Days.maxByOrNull { it.second }?.second ?: target).coerceAtLeast(target + 5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Từ mới 7 ngày qua",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(156.dp)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = maxCount.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                    
                    val targetOffset = 156.dp * (1f - target.toFloat() / maxCount.toFloat())
                    Text(
                        text = target.toString(),
                        fontSize = 10.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = targetOffset - 6.dp)
                    )
                    
                    Text(
                        text = "0",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp)) {
                        val targetY = size.height - (size.height * (target.toFloat() / maxCount.toFloat()))
                        drawLine(
                            color = Color.Red.copy(alpha = 0.2f),
                            start = Offset(0f, targetY),
                            end = Offset(size.width, targetY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last7Days.forEach { data ->
                            val barHeightFactor = data.second.toFloat() / maxCount.toFloat()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight(barHeightFactor.coerceAtLeast(0.01f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (data.second >= target) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last7Days.forEach { data ->
                            Text(
                                text = data.first,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HSKProgressLineChart(
    lichSu: List<com.example.hoctiengtrung2.data.model.LichSuHoatDong>,
    tongTuHienTai: Int
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displaySdf = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    var selectedTimeframe by remember { mutableStateOf("7 ngày") }
    var currentPeriodCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Reset period calendar when timeframe changes
    LaunchedEffect(selectedTimeframe) {
        currentPeriodCalendar = Calendar.getInstance()
    }
    
    val todayStr = remember { sdf.format(Calendar.getInstance().time) }
    
    val canNavigateNext = remember(selectedTimeframe, currentPeriodCalendar) {
        val today = Calendar.getInstance()
        if (selectedTimeframe == "Tháng") {
            val currentYear = currentPeriodCalendar.get(Calendar.YEAR)
            val currentMonth = currentPeriodCalendar.get(Calendar.MONTH)
            val todayYear = today.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH)
            
            if (currentYear < todayYear) {
                true
            } else if (currentYear == todayYear) {
                currentMonth < todayMonth
            } else {
                false
            }
        } else {
            // For 7-day rolling, check if the end of current period is before today
            val endOfPeriod = (currentPeriodCalendar.clone() as Calendar)
            endOfPeriod.set(Calendar.HOUR_OF_DAY, 0); endOfPeriod.set(Calendar.MINUTE, 0); endOfPeriod.set(Calendar.SECOND, 0); endOfPeriod.set(Calendar.MILLISECOND, 0)
            val todayStart = (today.clone() as Calendar)
            todayStart.set(Calendar.HOUR_OF_DAY, 0); todayStart.set(Calendar.MINUTE, 0); todayStart.set(Calendar.SECOND, 0); todayStart.set(Calendar.MILLISECOND, 0)
            endOfPeriod.before(todayStart)
        }
    }
    
    // 1. Generate target days based on timeframe
    val targetDaysCal = remember(selectedTimeframe, currentPeriodCalendar) {
        if (selectedTimeframe == "Tháng") {
            val daysInMonth = currentPeriodCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            (1..daysInMonth).map { day ->
                (currentPeriodCalendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, day)
                }
            }
        } else {
            // 7 days ending at currentPeriodCalendar
            (0 until 7).reversed().map { i ->
                (currentPeriodCalendar.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
            }
        }
    }
    
    val days = targetDaysCal.size
    
    // 2. Compute cumulative counts for each date
    val chronologicalCumulative = remember(targetDaysCal, lichSu, tongTuHienTai) {
        targetDaysCal.map { cal ->
            val dateStr = sdf.format(cal.time)
            val learnedAfter = lichSu.filter { it.ngay > dateStr }.sumOf { it.soTuMoi }
            (tongTuHienTai - learnedAfter).coerceAtLeast(0)
        }
    }
    
    // 4. HSK Thresholds: renamed to "H1", "H2", etc.
    val hskThresholds = listOf(
        150 to "H1",
        300 to "H2",
        600 to "H3",
        1200 to "H4",
        2500 to "H5",
        5000 to "H6"
    )
    
    val maxVal = chronologicalCumulative.maxOrNull() ?: 0
    
    val activeThresholds = remember(maxVal) {
        val active = mutableListOf<Pair<Int, String>>()
        for (i in hskThresholds.indices) {
            val thresh = hskThresholds[i]
            active.add(thresh)
            if (thresh.first > maxVal) {
                if (i + 1 < hskThresholds.size) {
                    active.add(hskThresholds[i + 1])
                }
                break
            }
        }
        active
    }
    
    val yMax = (activeThresholds.maxByOrNull { it.first }?.first ?: 150).toFloat() * 1.15f
    val chartHeight = 160.dp
    val labelWidth = 28.dp // Compacted to the left
    val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Timeframe Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ đạt chuẩn HSK",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("7 ngày", "Tháng").forEach { text ->
                        val isSelected = selectedTimeframe == text
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedTimeframe = text }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Sub-header: Period Navigation (like a calendar selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newCal = currentPeriodCalendar.clone() as Calendar
                        if (selectedTimeframe == "Tháng") {
                            newCal.add(Calendar.MONTH, -1)
                        } else {
                            newCal.add(Calendar.DAY_OF_YEAR, -7)
                        }
                        currentPeriodCalendar = newCal
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Trước",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                val labelText = if (selectedTimeframe == "Tháng") {
                    SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(currentPeriodCalendar.time)
                } else {
                    val endOfPeriod = currentPeriodCalendar
                    val startOfPeriod = (endOfPeriod.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
                    val df = SimpleDateFormat("dd/MM", Locale.getDefault())
                    "${df.format(startOfPeriod.time)} - ${df.format(endOfPeriod.time)}"
                }

                Text(
                    text = labelText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(min = 100.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        val newCal = currentPeriodCalendar.clone() as Calendar
                        if (selectedTimeframe == "Tháng") {
                            newCal.add(Calendar.MONTH, 1)
                        } else {
                            newCal.add(Calendar.DAY_OF_YEAR, 7)
                        }
                        currentPeriodCalendar = newCal
                    },
                    enabled = canNavigateNext,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Tiếp theo",
                        tint = if (!canNavigateNext) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                val totalWidth = maxWidth
                val chartWidth = totalWidth - labelWidth
                val stepX = chartWidth / (days - 1).toFloat()
                val primaryColor = MaterialTheme.colorScheme.primary
                val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                
                // 1. Dashed Lines Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .padding(start = labelWidth)
                ) {
                    activeThresholds.forEach { thresh ->
                        val yPos = size.height - (size.height * (thresh.first.toFloat() / yMax))
                        if (yPos >= 0 && yPos <= size.height) {
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, yPos),
                                end = Offset(size.width, yPos),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
                
                // 2. Y-Axis labels aligned with lines (Compact 28.dp width)
                Box(
                    modifier = Modifier
                        .width(labelWidth)
                        .height(chartHeight)
                ) {
                    activeThresholds.forEach { thresh ->
                        val yOffset = chartHeight * (1f - thresh.first.toFloat() / yMax)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(y = yOffset - 7.dp)
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = thresh.second,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // 3. Line Chart Drawing (No daily circles/dots)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .padding(start = labelWidth)
                ) {
                    val points = chronologicalCumulative.mapIndexedNotNull { index, count ->
                        val cal = targetDaysCal[index]
                        val dateStr = sdf.format(cal.time)
                        if (dateStr > todayStr) null
                        else {
                            val x = index * (size.width / (days - 1).toFloat())
                            val y = size.height - (size.height * (count.toFloat() / yMax))
                            Offset(x, y)
                        }
                    }
                    
                    val path = androidx.compose.ui.graphics.Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
                
                // 4. Overlays for count text and dates (Milestone days display)
                val todayIndex = targetDaysCal.indexOfFirst { sdf.format(it.time) == todayStr }
                val latestIndexToShow = if (todayIndex != -1) todayIndex else days - 1
                
                val indicesToShowDate = if (days == 7) (0..6).toList() else listOf(0, 9, 19, days - 1)
                val indicesToShowCount = if (days == 7) {
                    if (todayIndex != -1) {
                        (0..todayIndex).toList()
                    } else {
                        // Tuần hiện tại không chứa ngày hôm nay
                        // Nếu tuần đã qua hoàn toàn -> hiện tất cả, nếu là tuần tương lai -> không hiện
                        val lastDayStr = sdf.format(targetDaysCal.last().time)
                        if (lastDayStr < todayStr) (0..6).toList() else emptyList()
                    }
                } else {
                    if (todayIndex != -1) {
                        listOf(todayIndex)
                    } else {
                        // Tháng không chứa ngày hôm nay: kiểm tra là tháng đã qua hay tương lai
                        val firstDayStr = sdf.format(targetDaysCal.first().time)
                        if (firstDayStr > todayStr) emptyList() else listOf(days - 1)
                    }
                }
                
                chronologicalCumulative.forEachIndexed { index, count ->
                    val cal = targetDaysCal[index]
                    val dateStr = displaySdf.format(cal.time)
                    
                    val xPos = labelWidth + stepX * index
                    val yPos = chartHeight * (1f - count.toFloat() / yMax)
                    
                    if (index in indicesToShowCount) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = xPos - 20.dp,
                                    y = yPos - 22.dp
                                )
                                .width(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                    
                    if (index in indicesToShowDate) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = xPos - 25.dp,
                                    y = chartHeight + 4.dp
                                )
                                .width(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateStr,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
