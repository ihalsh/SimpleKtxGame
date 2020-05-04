package com.libktx.core.screen

import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.libktx.core.Game
import com.libktx.game.utils.ImageAssets.Atlas
import com.libktx.game.utils.MusicAssets.Rain
import com.libktx.game.utils.SoundAssets.Drop
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
                    private val camera: OrthographicCamera) : KtxScreen {

    override fun render(delta: Float) {
        draw()
        processInput()
    }

    private fun draw() {
        batch.use(camera.apply { update() }) { batch ->
            font.draw(batch, "Welcome to Drop!!! ", 100f, 150f)
            font.draw(batch, "Tap anywhere to begin!", 100f, 100f)
        }
    }

    private fun processInput() {

        fun loadAssetsAndSetNextScreen() {
            KtxAsync.initiate()
            val atlas = assets.loadAsync<TextureAtlas>(Atlas.path)
            val dropSound = assets.loadAsync<Sound>(Drop.path)
            val rainMusic = assets.loadAsync<Music>(Rain.path)
            KtxAsync.launch {
                game.addScreen(GameScreen(dropImage = atlas.await().findRegion("drop"),
                        bucketImage = atlas.await().findRegion("bucket"),
                        dropSound = dropSound.await(),
                        rainMusic = rainMusic.await().apply { isLooping = true },
                        batch = batch, renderer = renderer, font = font, camera = camera))
                game.setScreen<GameScreen>()
            }
        }

        if (input.isTouched) {
            loadAssetsAndSetNextScreen()
            game.removeScreen<LoadingScreen>()
            dispose()
        }
    }
}