package com.example.hoctiengtrung2.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hoctiengtrung2.data.model.FlashcardCaNhan
import com.example.hoctiengtrung2.ui.theme.*
import com.example.hoctiengtrung2.ui.viewmodel.AppViewModelProvider
import com.example.hoctiengtrung2.ui.viewmodel.TaoFlashcardUiState
import com.example.hoctiengtrung2.ui.viewmodel.TaoFlashcardViewModel
import com.example.hoctiengtrung2.ui.viewmodel.HomeViewModel
import com.example.hoctiengtrung2.ui.viewmodel.HomeUiState
import com.example.hoctiengtrung2.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaoFlashcardScreen(
    onHoanTat: () -> Unit,
    viewModel: TaoFlashcardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val idNguoiDung = SessionManager.layIdNguoiDung(context)
    val uiState by viewModel.uiState.collectAsState()
    val danhSachFlashcard by viewModel.danhSachFlashcard.collectAsState()
    val dangTaiDanhSach by viewModel.dangTaiDanhSach.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val isSelectionMode = selectedIds.isNotEmpty()
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            if (selectedIds.isNotEmpty()) {
                viewModel.exportToCsv(context, uri, selectedIds.toList())
                selectedIds = emptySet()
            } else {
                viewModel.exportToCsv(context, uri)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromCsv(context, idNguoiDung, uri)
        }
    }

    LaunchedEffect(idNguoiDung) {
        if (idNguoiDung.isNotEmpty()) {
            viewModel.layDanhSachFlashcard(idNguoiDung)
            homeViewModel.layDuLieu(idNguoiDung)
        }
    }

    val currentLevel = (homeUiState as? HomeUiState.ThanhCong)?.data?.capDo?.tenCapDo ?: "HSK1"
    val (_, levelColor, _) = mauTheoHSK(currentLevel)

    LaunchedEffect(uiState) {
        if (uiState is TaoFlashcardUiState.ThanhCong) {
            Toast.makeText(context, "Đã cập nhật bộ sưu tập!", Toast.LENGTH_SHORT).show()
            showDialog = false
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("Đã chọn ${selectedIds.size} thẻ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Bộ sưu tập thẻ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy chọn")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { showBatchDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa đã chọn", tint = Color.Red)
                        }
                        IconButton(onClick = { exportLauncher.launch("selected_flashcards.csv") }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Tải xuống đã chọn", tint = levelColor)
                        }
                    } else {
                        IconButton(
                            onClick = { 
                                importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/octet-stream"))
                            }
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = "Nhập CSV", tint = levelColor)
                        }
                        IconButton(
                            onClick = { 
                                exportLauncher.launch("flashcards_ca_nhan.csv") 
                            }
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Xuất CSV", tint = levelColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thẻ mới")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            if (dangTaiDanhSach && danhSachFlashcard.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = levelColor
                )
            } else if (danhSachFlashcard.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Chưa có thẻ nào trong bộ sưu tập.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Nhấn dấu + để bắt đầu tạo!",
                        color = levelColor,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(danhSachFlashcard) { item ->
                        val isSelected = selectedIds.contains(item.idFlashcard)
                        FlashcardItem(
                            flashcard = item,
                            levelColor = levelColor,
                            idNguoiDung = idNguoiDung,
                            uiState = uiState,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onSelectToggle = {
                                selectedIds = if (isSelected) selectedIds - item.idFlashcard else selectedIds + item.idFlashcard
                            },
                            onDelete = { viewModel.xoaFlashcard(idNguoiDung, item.idFlashcard) },
                            onEdit = { hanTu, pinyin, nghia ->
                                viewModel.suaFlashcard(idNguoiDung, item.idFlashcard, hanTu, pinyin, nghia)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        TaoFlashcardDialog(
            idNguoiDung = idNguoiDung,
            uiState = uiState,
            levelColor = levelColor,
            onDismiss = { 
                showDialog = false
                viewModel.resetState()
            },
            onSave = { hanTu, pinyin, nghia ->
                viewModel.taoFlashcard(idNguoiDung, hanTu, pinyin, nghia)
            }
        )
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Xóa các thẻ đã chọn", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa ${selectedIds.size} thẻ đã chọn?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.xoaDanhSachFlashcard(idNguoiDung, selectedIds.toList())
                        selectedIds = emptySet()
                        showBatchDeleteConfirm = false
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlashcardItem(
    flashcard: FlashcardCaNhan,
    levelColor: Color,
    idNguoiDung: String,
    uiState: TaoFlashcardUiState,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onSelectToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (String, String, String) -> Unit
) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "Rotation"
    )

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isDarkMode = SessionManager.isDarkModeState.value
    val hanTuColor = if (isDarkMode) Color.White else levelColor

    LaunchedEffect(uiState) {
        if (uiState is TaoFlashcardUiState.ThanhCong) {
            showEditDialog = false
        }
    }

    Box(modifier = Modifier.aspectRatio(1f)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .combinedClickable(
                    onClick = { 
                        if (isSelectionMode) onSelectToggle() else rotated = !rotated 
                    },
                    onLongClick = { 
                        if (!isSelectionMode) showMenu = true 
                    }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    if (isDarkMode) Color(0xFF2C285C) else TimPastel
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) levelColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front: Hán tự
                    Text(
                        text = flashcard.hanTu,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = hanTuColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    // Back: Pinyin & Nghĩa
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (flashcard.pinyin.isNotBlank()) {
                            Text(
                                text = flashcard.pinyin,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = flashcard.nghia,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }
                }
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(levelColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Chọn thẻ") },
                onClick = {
                    showMenu = false
                    onSelectToggle()
                }
            )
            DropdownMenuItem(
                text = { Text("Sửa thẻ") },
                onClick = {
                    showMenu = false
                    showEditDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Xóa thẻ") },
                onClick = {
                    showMenu = false
                    showDeleteConfirm = true
                }
            )
        }
    }

    if (showEditDialog) {
        TaoFlashcardDialog(
            idNguoiDung = idNguoiDung,
            uiState = uiState,
            levelColor = levelColor,
            title = "Sửa thẻ",
            initialHanTu = flashcard.hanTu,
            initialPinyin = flashcard.pinyin,
            initialNghia = flashcard.nghia,
            onDismiss = { showEditDialog = false },
            onSave = { hanTu, pinyin, nghia ->
                onEdit(hanTu, pinyin, nghia)
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirm = false 
            },
            title = { Text("Xóa thẻ", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa thẻ '${flashcard.hanTu}' này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirm = false 
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaoFlashcardDialog(
    idNguoiDung: String,
    uiState: TaoFlashcardUiState,
    levelColor: Color,
    title: String = "Tạo thẻ mới",
    initialHanTu: String = "",
    initialPinyin: String = "",
    initialNghia: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var hanTu by remember { mutableStateOf(initialHanTu) }
    var pinyin by remember { mutableStateOf(initialPinyin) }
    var nghia by remember { mutableStateOf(initialNghia) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = hanTu,
                    onValueChange = { 
                        hanTu = it
                        if (it.isNotBlank()) errorText = null
                    },
                    label = { Text("Hán tự (*)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = errorText != null && hanTu.isBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = levelColor,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = levelColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinyin,
                    onValueChange = { pinyin = it },
                    label = { Text("Pinyin (Không bắt buộc)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = levelColor,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = levelColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nghia,
                    onValueChange = { 
                        nghia = it
                        if (it.isNotBlank()) errorText = null
                    },
                    label = { Text("Nghĩa tiếng Việt (*)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    isError = errorText != null && nghia.isBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = levelColor,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = levelColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                val effectiveError = errorText ?: if (uiState is TaoFlashcardUiState.Loi) uiState.thongBao else null
                if (effectiveError != null) {
                    Text(
                        effectiveError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        if (hanTu.isBlank() || nghia.isBlank()) {
                            errorText = "Hán tự và Nghĩa không được để trống"
                        } else {
                            onSave(hanTu, pinyin, nghia)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = levelColor),
                    enabled = uiState !is TaoFlashcardUiState.DangTai
                ) {
                    if (uiState is TaoFlashcardUiState.DangTai) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Lưu thẻ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
