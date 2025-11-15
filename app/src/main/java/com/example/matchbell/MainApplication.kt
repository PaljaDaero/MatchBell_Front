package com.example.matchbell // (본인 패키지 이름인지 확인!)

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // [!!!] 이 Hilt 전원 스위치가 빠졌을 겁니다!
class MainApplication : Application() {
    // 내용은 비어있어도 됩니다.
}