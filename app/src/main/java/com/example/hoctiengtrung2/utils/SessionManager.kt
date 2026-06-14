package com.example.hoctiengtrung2.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

object SessionManager {
    private const val PREF_NAME = "HocTiengTrungSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_DARK_MODE = "dark_mode"

    // State để UI quan sát và cập nhật ngay lập tức
    var isDarkModeState = mutableStateOf(false)

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun khoiTao(context: Context) {
        isDarkModeState.value = isDarkMode(context)
    }

    fun setDarkMode(context: Context, isEnabled: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_DARK_MODE, isEnabled)
        editor.apply()
        isDarkModeState.value = isEnabled
    }

    fun isDarkMode(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun luuDangNhap(context: Context, idNguoiDung: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USER_ID, idNguoiDung)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun daDangNhap(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun layIdNguoiDung(context: Context): String {
        return getSharedPreferences(context).getString(KEY_USER_ID, "") ?: ""
    }

    fun dangXuat(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}