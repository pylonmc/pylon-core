package io.github.pylonmc.rebar.entity.display.transform

import org.joml.Matrix4f

interface TransformComponent {
    fun apply(matrix: Matrix4f)
}