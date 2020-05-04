package com.libktx.core

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.libktx.core.screen.GameScreen
import com.libktx.core.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.newAsyncContext
import ktx.inject.Context
import ktx.inject.register
import ktx.log.debug
import ktx.log.info
import ktx.log.logger

private val log = logger<Game>()

class Game : KtxGame<KtxScreen>() {
    private val context = Context()

    override fun create() {

        context.register {
            bindSingleton(this@Game)
            bindSingleton<Batch>(SpriteBatch())
            bindSingleton(ShapeRenderer())
            bindSingleton(BitmapFont())
            bindSingleton(AssetStorage(newAsyncContext(4)))
            // The camera ensures we can render using our target resolution of 800x480
            //    pixels no matter what the screen resolution is.
            bindSingleton(OrthographicCamera().apply { setToOrtho(false, 800f, 480f) })
            bindSingleton(PooledEngine())
            addScreen(LoadingScreen(inject(), inject(), inject(), inject(), inject(), inject(), inject()))
        }
        setScreen<LoadingScreen>()
        super.create()
    }

    override fun dispose() {
        log.info { "Entities in engine: ${context.inject<PooledEngine>().entities.size()}" }
        context.dispose()
        super.dispose()
    }
}