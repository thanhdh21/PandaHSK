package com.example.hoctiengtrung2.data.di

import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource
import com.example.hoctiengtrung2.data.remote.TuVungRemoteDataSource
import com.example.hoctiengtrung2.data.remote.XacThucRemoteDataSource
import com.example.hoctiengtrung2.data.repository.TraCuuRepository
import com.example.hoctiengtrung2.data.repository.HomeRepository
import com.example.hoctiengtrung2.data.repository.TuVungRepository
import com.example.hoctiengtrung2.data.repository.XacThucRepository

interface AppContainer {
    val xacThucRepository: XacThucRepository
    val homeRepository: HomeRepository
    val tuVungRepository: TuVungRepository
    val traCuuRepository: TraCuuRepository
}

class AppContainerImpl : AppContainer {
    private val xacThucRemoteDataSource by lazy { XacThucRemoteDataSource() }
    private val homeRemoteDataSource by lazy { HomeRemoteDataSource() }
    private val tuVungRemoteDataSource by lazy { TuVungRemoteDataSource() }

    override val xacThucRepository: XacThucRepository by lazy {
        XacThucRepository(xacThucRemoteDataSource)
    }

    override val homeRepository: HomeRepository by lazy {
        HomeRepository(homeRemoteDataSource)
    }

    override val tuVungRepository: TuVungRepository by lazy {
        TuVungRepository(tuVungRemoteDataSource, homeRemoteDataSource)
    }

    override val traCuuRepository: TraCuuRepository by lazy {
        TraCuuRepository()
    }
}

