package com.libktx.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.libktx.core.Game

class AndroidLauncher : AndroidApplication() {

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        with(AndroidApplicationConfiguration()) {
            title = "SimpleKtxGame"
            useAccelerometer = false
            useCompass = false
            initialize(Game(), this)
        }
    }
}
