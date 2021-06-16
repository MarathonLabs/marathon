package com.example

import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.screen.Screen


class MainScreen : Screen<MainScreen>() {
    val text = KEditText { withId(R.id.edittext) }
}
