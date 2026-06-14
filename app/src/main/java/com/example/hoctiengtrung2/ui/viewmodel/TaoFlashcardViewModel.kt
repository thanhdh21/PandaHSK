package com.example.hoctiengtrung2.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hoctiengtrung2.data.model.FlashcardCaNhan
import com.example.hoctiengtrung2.data.repository.TuVungRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class TaoFlashcardUiState {
    object Cho : TaoFlashcardUiState()
    object DangTai : TaoFlashcardUiState()
    object ThanhCong : TaoFlashcardUiState()
    data class Loi(val thongBao: String) : TaoFlashcardUiState()
}

class TaoFlashcardViewModel(private val repository: TuVungRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TaoFlashcardUiState>(TaoFlashcardUiState.Cho)
    val uiState: StateFlow<TaoFlashcardUiState> = _uiState

    private val _danhSachFlashcard = MutableStateFlow<List<FlashcardCaNhan>>(emptyList())
    val danhSachFlashcard: StateFlow<List<FlashcardCaNhan>> = _danhSachFlashcard

    private val _dangTaiDanhSach = MutableStateFlow(false)
    val dangTaiDanhSach: StateFlow<Boolean> = _dangTaiDanhSach

    fun layDanhSachFlashcard(idNguoiDung: String) {
        viewModelScope.launch {
            _dangTaiDanhSach.value = true
            val results = repository.layDanhSachFlashcardCaNhan(idNguoiDung)
            _danhSachFlashcard.value = results
            _dangTaiDanhSach.value = false
        }
    }

    fun taoFlashcard(idNguoiDung: String, hanTu: String, pinyin: String, nghia: String) {
        if (hanTu.isBlank() || nghia.isBlank()) {
            _uiState.value = TaoFlashcardUiState.Loi("Vui lòng nhập đầy đủ Hán tự và Nghĩa")
            return
        }

        viewModelScope.launch {
            _uiState.value = TaoFlashcardUiState.DangTai
            val flashcard = FlashcardCaNhan(
                idNguoiDung = idNguoiDung,
                hanTu = hanTu,
                pinyin = pinyin,
                nghia = nghia
            )
            val success = repository.taoFlashcardCaNhan(flashcard)
            if (success) {
                _uiState.value = TaoFlashcardUiState.ThanhCong
                layDanhSachFlashcard(idNguoiDung) // Làm mới danh sách
            } else {
                _uiState.value = TaoFlashcardUiState.Loi("Tạo thẻ thất bại, vui lòng thử lại")
            }
        }
    }

    fun xoaFlashcard(idNguoiDung: String, idFlashcard: String) {
        viewModelScope.launch {
            val success = repository.xoaFlashcardCaNhan(idFlashcard)
            if (success) {
                layDanhSachFlashcard(idNguoiDung)
            }
        }
    }

    fun xoaDanhSachFlashcard(idNguoiDung: String, idFlashcards: List<String>) {
        viewModelScope.launch {
            _dangTaiDanhSach.value = true
            val success = repository.xoaDanhSachFlashcardCaNhan(idFlashcards)
            if (success) {
                layDanhSachFlashcard(idNguoiDung)
            }
            _dangTaiDanhSach.value = false
        }
    }

    fun resetState() {
        _uiState.value = TaoFlashcardUiState.Cho
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val curVal = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    curVal.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(curVal.toString())
                curVal.setLength(0)
            } else {
                curVal.append(c)
            }
            i++
        }
        result.add(curVal.toString())
        return result
    }

    fun importFromCsv(context: Context, idNguoiDung: String, uri: Uri) {
        viewModelScope.launch {
            _dangTaiDanhSach.value = true
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val reader = inputStream?.bufferedReader()
                var lines = reader?.readLines() ?: emptyList()
                if (lines.isNotEmpty()) {
                    var firstLine = lines[0]
                    if (firstLine.startsWith("\uFEFF")) {
                        firstLine = firstLine.substring(1)
                        lines = listOf(firstLine) + lines.drop(1)
                    }
                    if (firstLine.startsWith("PK")) {
                        Toast.makeText(
                            context,
                            "Tệp Excel (.xlsx) không được hỗ trợ trực tiếp! Vui lòng chọn 'Save As' trong Excel và lưu dưới định dạng 'CSV UTF-8' (.csv) trước khi tải lên.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                    var startIdx = 0
                    val firstLineParts = parseCsvLine(lines[0])
                    if (firstLineParts.any { 
                            val lower = it.lowercase(Locale.getDefault())
                            lower.contains("hantu") || lower.contains("hán tự") || lower.contains("pinyin") || lower.contains("nghia") || lower.contains("nghĩa")
                        }) {
                        startIdx = 1
                    }
                    var importedCount = 0
                    for (i in startIdx until lines.size) {
                        val line = lines[i]
                        if (line.trim().isEmpty()) continue
                        val parts = parseCsvLine(line)
                        if (parts.isNotEmpty()) {
                            val hanTu = parts.getOrNull(0)?.trim() ?: ""
                            val pinyin = parts.getOrNull(1)?.trim() ?: ""
                            val nghia = parts.getOrNull(2)?.trim() ?: ""
                            if (hanTu.isNotEmpty() && nghia.isNotEmpty()) {
                                val flashcard = FlashcardCaNhan(
                                    idNguoiDung = idNguoiDung,
                                    hanTu = hanTu,
                                    pinyin = pinyin,
                                    nghia = nghia
                                )
                                repository.taoFlashcardCaNhan(flashcard)
                                importedCount++
                            }
                        }
                    }
                    layDanhSachFlashcard(idNguoiDung)
                    Toast.makeText(context, "Đã nhập thành công $importedCount thẻ từ CSV!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "File CSV trống!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi nhập CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                _dangTaiDanhSach.value = false
            }
        }
    }

    fun exportToCsv(context: Context, uri: Uri, idFlashcards: List<String>? = null) {
        viewModelScope.launch {
            _dangTaiDanhSach.value = true
            try {
                val contentResolver = context.contentResolver
                val outputStream = contentResolver.openOutputStream(uri)
                val writer = outputStream?.bufferedWriter()
                writer?.use { w ->
                    w.write("\uFEFF") // Write UTF-8 BOM for Excel/WPS compatibility
                    w.write("HanTu,Pinyin,Nghia\n")
                    val list = _danhSachFlashcard.value.filter { card ->
                        idFlashcards == null || idFlashcards.contains(card.idFlashcard)
                    }
                    list.forEach { card ->
                        val escapedHanTu = escapeCsv(card.hanTu)
                        val escapedPinyin = escapeCsv(card.pinyin)
                        val escapedNghia = escapeCsv(card.nghia)
                        w.write("$escapedHanTu,$escapedPinyin,$escapedNghia\n")
                    }
                }
                val count = idFlashcards?.size ?: _danhSachFlashcard.value.size
                Toast.makeText(context, "Đã xuất thành công $count thẻ ra CSV!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi xuất CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                _dangTaiDanhSach.value = false
            }
        }
    }

    fun suaFlashcard(idNguoiDung: String, idFlashcard: String, hanTu: String, pinyin: String, nghia: String) {
        if (hanTu.isBlank() || nghia.isBlank()) {
            _uiState.value = TaoFlashcardUiState.Loi("Vui lòng nhập đầy đủ Hán tự và Nghĩa")
            return
        }

        viewModelScope.launch {
            _uiState.value = TaoFlashcardUiState.DangTai
            val success = repository.suaFlashcardCaNhan(idFlashcard, hanTu, pinyin, nghia)
            if (success) {
                _uiState.value = TaoFlashcardUiState.ThanhCong
                layDanhSachFlashcard(idNguoiDung)
            } else {
                _uiState.value = TaoFlashcardUiState.Loi("Sửa thẻ thất bại, vui lòng thử lại")
            }
        }
    }
}
