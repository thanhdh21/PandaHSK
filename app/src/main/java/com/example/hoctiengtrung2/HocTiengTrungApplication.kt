package com.example.hoctiengtrung2

import android.app.Application
import com.example.hoctiengtrung2.data.di.AppContainer
import com.example.hoctiengtrung2.data.di.AppContainerImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class HocTiengTrungApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        
        // Cấu hình Firestore Offline Cache Settings
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(100 * 1024 * 1024) // 100 MB cache
                        .build()
                )
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        container = AppContainerImpl()
    }
}
