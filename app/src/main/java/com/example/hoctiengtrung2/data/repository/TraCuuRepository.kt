package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.TatoebaSentencePair
import com.example.hoctiengtrung2.data.model.TraCuuResult
import com.example.hoctiengtrung2.data.remote.MyMemoryApiService
import com.example.hoctiengtrung2.data.remote.TatoebaApiService
import com.example.hoctiengtrung2.data.remote.ThivienApiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TraCuuRepository {
    private val myMemoryService: MyMemoryApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryApiService::class.java)
    }

    private val thivienService: ThivienApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://hvdic.thivien.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ThivienApiService::class.java)
    }

    private val tatoebaService: TatoebaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.tatoeba.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TatoebaApiService::class.java)
    }

    suspend fun traCuuTu(tu: String): TraCuuResult? = coroutineScope {
        val myMemoryDeferred = async {
            try {
                myMemoryService.traCuu(query = tu)
            } catch (e: Exception) {
                null
            }
        }

        val thivienDeferred = async {
            try {
                thivienService.convertHanViet(input = tu)
            } catch (e: Exception) {
                null
            }
        }

        val tatoebaDeferred = async {
            try {
                tatoebaService.getRelatedSentences(query = tu)
            } catch (e: Exception) {
                null
            }
        }

        val myMemoryResponse = myMemoryDeferred.await()
        val thivienResponse = thivienDeferred.await()
        val tatoebaResponse = tatoebaDeferred.await()

        val translatedText = myMemoryResponse?.responseData?.translatedText ?: return@coroutineScope null

        val hanVietList = thivienResponse?.result?.mapNotNull { it.getFirstReading() }
        val hanViet = if (!hanVietList.isNullOrEmpty()) {
            hanVietList.joinToString(" ").trim()
        } else {
            null
        }

        val cauLienQuan = tatoebaResponse?.data?.mapNotNull { sentence ->
            val original = sentence.text
            val translation = sentence.translations?.firstOrNull()?.text
            if (original != null && translation != null) {
                TatoebaSentencePair(original, translation)
            } else {
                null
            }
        } ?: emptyList()

        var localHanViet: String? = null
        var localLoaiTu: String? = null

        try {
            val db = FirebaseFirestore.getInstance()
            val tuVungQuery = db.collection("TuVung")
                .whereEqualTo("hanTu", tu)
                .get()
                .await()
            
            if (!tuVungQuery.isEmpty) {
                val doc = tuVungQuery.documents[0]
                val idLoaiTu = doc.getString("idLoaiTu")
                val hanVietDb = doc.getString("hanViet")
                if (!hanVietDb.isNullOrEmpty()) {
                    localHanViet = hanVietDb
                }
                if (!idLoaiTu.isNullOrEmpty()) {
                    val loaiTuDoc = db.collection("LoaiTu")
                        .document(idLoaiTu)
                        .get()
                        .await()
                    localLoaiTu = loaiTuDoc.getString("tenLoaiTu")
                }
            }
        } catch (e: Exception) {
        }

        TraCuuResult(
            query = tu,
            nghia = translatedText,
            loaiTu = localLoaiTu,
            hanViet = localHanViet ?: hanViet,
            cauLienQuan = cauLienQuan
        )
    }
}
