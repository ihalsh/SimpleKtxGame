package com.libktx.core.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use

class SpawnSystem(private val dropRegion: TextureRegion) : IntervalSystem(1f) {

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        // spawn an initial drop when the system is added to the engine
        updateInterval()
    }

    override fun updateInterval() {
        engine.entity {
            with<RenderComponent> {
                sprite.setRegion(dropRegion)
                z = 1
            }
            with<TransformComponent> { bounds.set(MathUtils.random(0f, 800 - 64f), 480f, 64f, 64f) }
            with<MoveComponent> { speed.set(0f, -200f) }
            with<CollisionComponent>()
        }
    }
}

class RenderSystem(bucket: Entity,
                   private val batch: Batch,
                   private val font: BitmapFont,
                   private val camera: OrthographicCamera) : SortedIteratingSystem(
        allOf(TransformComponent::class, RenderComponent::class).get(),
        // compareBy is used to render entities by their z-index (=bucket is drawn in the background; raindrops are drawn in the foreground)
        compareBy { entity: Entity -> entity[RenderComponent.mapper]?.z }) {

    private val bucketCmp = bucket[BucketComponent.mapper]!!

    override fun update(deltaTime: Float) {
        forceSort()
        batch.use(camera.apply { update() }) {
            it.projectionMatrix = camera.combined
            super.update(deltaTime)
            font.draw(batch, "Drops Collected: ${bucketCmp.dropsGathered}", 0f, 480f)
            font.draw(batch, "Drops Missed: ${bucketCmp.dropsMissed}", 680f, 480f)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[TransformComponent.mapper]?.let { transform ->
            entity[RenderComponent.mapper]?.let { render ->
                batch.draw(render.sprite, transform.bounds.x, transform.bounds.y)
            }
        }
    }
}

class DebugRenderSystem(private val renderer: ShapeRenderer,
                        private val camera: OrthographicCamera) : SortedIteratingSystem(
        allOf(TransformComponent::class, RenderComponent::class).get(),
        // compareBy is used to render entities by their z-index (=bucket is drawn in the background; raindrops are drawn in the foreground)
        compareBy { entity: Entity -> entity[RenderComponent.mapper]?.z }) {

    override fun update(deltaTime: Float) {
        forceSort()
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[TransformComponent.mapper]?.let { transform ->
            drawDebugRectangles(transform)
        }
    }

    private fun drawDebugRectangles(transform: TransformComponent) {
        renderer.use(Line) { renderer ->
            renderer.projectionMatrix = camera.combined
            renderer.rect(transform.bounds.x, transform.bounds.y, transform.bounds.width, transform.bounds.height)
        }
    }
}

class MoveSystem : IteratingSystem(allOf(TransformComponent::class, MoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[TransformComponent.mapper]?.let { transform ->
            entity[MoveComponent.mapper]?.let { move ->
                // make sure the entities stay within the screen bounds
                transform.bounds.x = MathUtils.clamp(transform.bounds.x + move.speed.x * deltaTime, 0f, 800f - 64f)
                transform.bounds.y += move.speed.y * deltaTime
            }
        }
    }
}

class CollisionSystem(bucket: Entity, private val dropSound: Sound) : IteratingSystem(allOf(TransformComponent::class, CollisionComponent::class).get()) {
    private val bucketBounds = bucket[TransformComponent.mapper]!!.bounds
    private val bucketCmp = bucket[BucketComponent.mapper]!!

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[TransformComponent.mapper]?.let { transform ->
            if (transform.bounds.y < 0) {
                bucketCmp.dropsMissed++
                engine.removeEntity(entity)
            } else if (transform.bounds.overlaps(bucketBounds)) {
                bucketCmp.dropsGathered++
                dropSound.play()
                engine.removeEntity(entity)
            }
        }
    }
}