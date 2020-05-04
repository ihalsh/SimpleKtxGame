package com.libktx.core.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.ashley.mapperFor

class BucketComponent : Component {
    companion object {
        val mapper = mapperFor<BucketComponent>()
    }
    var dropsGathered = 0
    var dropsMissed = 0
}

class CollisionComponent : Component

class MoveComponent : Component {
    companion object {
        val mapper = mapperFor<MoveComponent>()
    }

    val speed = Vector2()
}

class RenderComponent : Component {
    companion object {
        val mapper = mapperFor<RenderComponent>()
    }

    val sprite = Sprite()
    var z = 0
}

class TransformComponent : Component {
    companion object {
        val mapper = mapperFor<TransformComponent>()
    }

    val bounds = Rectangle()
}