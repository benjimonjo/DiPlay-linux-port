package com.shilapi.diplay.linux

import androidx.compose.ui.window.application
import com.shilapi.diplay.linux.app.DiPlayApplication

fun main() = application {
    DiPlayApplication(onExit = ::exitApplication)
}
