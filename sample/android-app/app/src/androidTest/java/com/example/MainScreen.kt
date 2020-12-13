package com.example

import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.screen.Screen


class MainScreen : Screen<MainScreen>() {
    val text = KEditText { withId(R.id.edittext) }
}
