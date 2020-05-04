package com.libktx.core.screen

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.libktx.core.ecs.*
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with

class GameScreen(private val dropImage: TextureRegion,
                 private val bucketImage: TextureRegion,
                 private val dropSound: Sound,
                 private val rainMusic: Music,
                 private val batch: Batch,
                 private val renderer: ShapeRenderer,
                 private val font: BitmapFont,
                 private val camera: OrthographicCamera,
                 private val engine: PooledEngine) : KtxScreen {

    private val bucket: Entity by lazy {
        engine.entity {
            with<BucketComponent>()
            with<TransformComponent> { bounds.set(800f / 2f - 64f / 2f, 20f, 64f, 64f) }
            with<MoveComponent>()
            with<RenderComponent> { sprite.setRegion(bucketImage) }
        }
    }
    private val touchPosition = Vector3()

    override fun show() {
        fun initializeEntityEngine() {
            engine.apply {
                // add systems
                addSystem(SpawnSystem(dropImage))
                addSystem(MoveSystem())
                addSystem(RenderSystem(bucket, batch, font, camera))
                addSystem(DebugRenderSystem(renderer, camera))
                // add CollisionSystem last as it removes entities and this should always
                // happen at the end of an engine update frame
                addSystem(CollisionSystem(bucket, dropSound))
            }
        }

        rainMusic.apply { isLooping = true; play()}
        initializeEntityEngine()
    }

    override fun render(delta: Float) {
        fun processInput() {
            if (input.isTouched) {
                touchPosition.set(input.x.toFloat(), input.y.toFloat(), 0f)
                camera.unproject(touchPosition)
                bucket[TransformComponent.mapper]?.let { transform -> transform.bounds.x = touchPosition.x - 64f / 2f }
            }
            when {
                input.isKeyPressed(Input.Keys.LEFT) -> bucket[MoveComponent.mapper]?.let { move -> move.speed.x = -200f }
                input.isKeyPressed(Input.Keys.RIGHT) -> bucket[MoveComponent.mapper]?.let { move -> move.speed.x = 200f }
                else -> bucket[MoveComponent.mapper]?.let { move -> move.speed.x = 0f }
            }
        }

        processInput()
        engine.update(delta)
    }
}