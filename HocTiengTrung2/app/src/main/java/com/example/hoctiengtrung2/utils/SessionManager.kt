package com.example.hoctiengtrung2.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "HocTiengTrungSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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