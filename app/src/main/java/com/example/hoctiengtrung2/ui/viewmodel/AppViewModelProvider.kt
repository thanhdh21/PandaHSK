package com.example.hoctiengtrung2.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hoctiengtrung2.HocTiengTrungApplication

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            XacThucViewModel(
                hocTiengTrungApplication().container.xacThucRepository
            )
        }
        initializer {
            HomeViewModel(
                hocTiengTrungApplication().container.homeRepository
            )
        }
        initializer {
            TuVungViewModel(
                hocTiengTrungApplication().container.tuVungRepository
            )
        }
        initializer {
            TracNghiemViewModel(
                hocTiengTrungApplication().container.tuVungRepository
            )
        }
        initializer {
            BaiHocViewModel(
                hocTiengTrungApplication().container.homeRepository
            )
        }
        initializer {
            CapDoViewModel(
                hocTiengTrungApplication().container.homeRepository
            )
        }
        initializer {
            TaoFlashcardViewModel(
                hocTiengTrungApplication().container.tuVungRepository
            )
        }
        initializer {
            TraCuuViewModel(
                hocTiengTrungApplication().container.traCuuRepository
            )
        }
    }
}

fun CreationExtras.hocTiengTrungApplication(): HocTiengTrungApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HocTiengTrungApplication)
