package com.libktx.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.libktx.core.Game

object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        with(LwjglApplicationConfiguration()) {
            title = "Drop"
            width = 800
            height = 480
            LwjglApplication(Game(), this)
        }
    }
}