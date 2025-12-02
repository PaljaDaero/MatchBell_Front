package com.example.matchbell.network

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

    // [중요] 백엔드 언니가 알려준 서버 주소 (끝에 슬래시 / 필수!)
    private const val BASE_URL = "http://3.239.45.21:8080/"

    // 1. Retrofit (전화기) 만들기
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // 위에서 설정한 진짜 서버 주소 사용
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2. AuthApi (전화번호부) 만들기
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi {
        return retrofit.create(ChatApi::class.java)
    }
}