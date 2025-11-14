package com.example.matchbell.di

import com.example.matchbell.network.AuthApi // (빨간 줄 뜨면 Alt+Enter)
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Retrofit (전화기) 만들기 (기존에 있었어야 할 코드)
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            // 에뮬레이터에서 내 컴퓨터(로컬) 서버에 접속할 때 쓰는 주소입니다.
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2. AuthApi (전화번호부) 만들기 (이번에 추가하는 코드)
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}