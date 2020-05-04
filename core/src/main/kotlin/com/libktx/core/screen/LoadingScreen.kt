package com.libktx.core.screen

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.libktx.core.Game
import com.libktx.core.utils.ImageAssets.Atlas
import com.libktx.core.utils.MusicAssets.Rain
import com.libktx.core.utils.SoundAssets.Drop
import kotlinx.coroutines.launch
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.graphics.use

class LoadingScreen(private val game: Game,
                    private val assets: AssetStorage,
                    private val batch: Batch,
                    private val renderer: ShapeRenderer,
                    private val font: BitmapFont,
                    private val camera: OrthographicCamera,
                    private val engine: PooledEngine) : KtxScreen {

    override fun show() {
        loadAssets()
    }

    override fun render(delta: Float) {
        batch.use(camera.apply { update() }) { batch ->
            font.run {
                draw(batch, "Welcome to Drop!!! ", 100f, 150f)
                if (assets.progress.isFinished) {
                    draw(batch, "Tap anywhere to begin!", 100f, 100f)
                    processInput()
                } else draw(batch, "Loading assets...", 100f, 100f)
            }
        }
    }

    private fun loadAssets() {
        KtxAsync.initiate()
        val atlas = assets.loadAsync<TextureAtlas>(Atlas.path)
        val dropSound = assets.loadAsync<Sound>(Drop.path)
        val rainMusic = assets.loadAsync<Music>(Rain.path)
        KtxAsync.launch {
            game.addScreen(GameScreen(dropImage = atlas.await().findRegion("drop"),
                    bucketImage = atlas.await().findRegion("bucket"),
                    dropSound = dropSound.await(),
                    rainMusic = rainMusic.await(),
                    batch = batch, renderer = renderer, font = font, camera = camera, engine = engine))
        }
    }

    private fun processInput() {
        if (input.isTouched) {
            game.setScreen<GameScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()
        }
    }
}