package com.example.hoctiengtrung2.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Định nghĩa gói dữ liệu nhận về từ Internet
data class TuVungInternetDto(
    val hanzi: String,      // Chữ Hán
    val pinyin: String,     // Phiên âm
    val meaning: String     // Nghĩa tiếng Việt
)

// 2. Thiết lập đường link cổng API kết nối mạng
interface TuVungApiService {

    @GET("api/dictionary/search")
    suspend fun traTuTrenInternet(@Query("keyword") tuKhoa: String): List<TuVungInternetDto>

    companion object {
        // Đường dẫn máy chủ ví dụ cung cấp kho từ vựng mở rộng
        private const val BASE_URL = "https://api.tudientrungviet.com/"

        fun create(): TuVungApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TuVungApiService::class.java)
        }
    }
}