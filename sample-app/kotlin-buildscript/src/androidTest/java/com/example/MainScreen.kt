package com.example

import com.agoda.kakao.KEditText
import com.agoda.kakao.Screen

class MainScreen : Screen<MainScreen>() {
    val text = KEditText { withId(R.id.edittext) }
}