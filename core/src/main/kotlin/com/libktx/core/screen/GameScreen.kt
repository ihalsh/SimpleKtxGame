package com.libktx.core.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxScreen
import ktx.assets.invoke
import ktx.assets.pool
import ktx.collections.gdxArrayOf
import ktx.collections.iterate
import ktx.graphics.use
import ktx.log.info

class GameScreen(private val dropImage: TextureRegion,
                 private val bucketImage: TextureRegion,
                 private val dropSound: Sound,
                 private val rainMusic: Music,
                 private val batch: Batch,
                 private val renderer: ShapeRenderer,
                 private val font: BitmapFont,
                 private val camera: OrthographicCamera) : KtxScreen {

    // create a Rectangle to logically represent the bucket
    // center the bucket horizontally
    // bottom left bucket corner is 20px above  bottom screen edge
    private val bucket = Rectangle(800f / 2f - 64f / 2f, 20f, 64f, 64f)

    // create the touchPos to store mouse click position
    private val touchPos = Vector3()

    private val raindropsPool = pool { Rectangle() } // pool to reuse raindrop rectangles
    private val activeRaindrops = gdxArrayOf<Rectangle>()
    private var lastDropTime = 0f
    private var dropsGathered = 0

    override fun show() {
        rainMusic.play()
        spawnRaindrop()
    }

    override fun render(delta: Float) {
        update(delta)
        processUserInput(delta)
        drawRandropsAndBucket()
        drawDebugRectangles()
    }

    private fun spawnRaindrop() {
        activeRaindrops.add(raindropsPool().set(MathUtils.random(0f, 800f - 64f), 480f, 64f, 64f))
        lastDropTime = 0f
    }

    private fun update(delta: Float) {
        // check if we need to create a new raindrop
        lastDropTime += delta
        if (lastDropTime > 1) spawnRaindrop()

        // move the raindrops, remove any that are beneath the bottom edge of the
        // screen or that hit the bucket. In the latter case, play back a sound effect also
        activeRaindrops.iterate { raindrop, mutableIterator ->
            raindrop.run {
                y -= 200 * Gdx.graphics.deltaTime
                if (y + 64 < 0) {
                    mutableIterator.remove()
                    raindropsPool(this)
                    info { "Missed a raindrop! Pool free objects: ${raindropsPool.free}" }
                }

                if (overlaps(bucket)) {
                    dropsGathered++
                    dropSound.play()
                    mutableIterator.remove()
                    raindropsPool(this)
                    info { "Pool free objects: ${raindropsPool.free}" }
                }
            }
        }
    }

    private fun processUserInput(delta: Float) {
        if (input.isTouched) {
            touchPos.set(input.x.toFloat(), input.y.toFloat(), 0f)
            camera.unproject(touchPos)
            bucket.x = touchPos.x - 64f / 2f
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * delta
        if (input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * delta
        // make sure the bucket stays within the screen bounds
        bucket.x = MathUtils.clamp(bucket.x, 0f, 800f - 64f)
    }

    private fun drawRandropsAndBucket() {
        // generally good practice to update the camera's matrices once per frame
        // tell the SpriteBatch to render in the coordinate system specified by the camera.
        // begin a new batch and draw the bucket and all drops
        batch.use(camera.apply { update() }) { batch ->
            font.draw(batch, "Drops Collected: $dropsGathered", 0f, 480f)
            activeRaindrops.forEach { raindrop -> batch.draw(dropImage, raindrop.x, raindrop.y) }
            batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height)
        }
    }

    private fun drawDebugRectangles() {
        renderer.use(Line) { renderer ->
            renderer.projectionMatrix = camera.combined
            activeRaindrops.forEach { raindrop -> renderer.rect(raindrop.x, raindrop.y, raindrop.width, raindrop.height) }
            renderer.rect(bucket.x, bucket.y, bucket.width, bucket.height)
        }
    }

    override fun dispose() {
        dropSound.dispose()
        rainMusic.dispose()
        info { "${javaClass.simpleName} disposed." }
    }
}