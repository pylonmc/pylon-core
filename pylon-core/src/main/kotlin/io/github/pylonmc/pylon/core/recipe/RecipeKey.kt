@file:Suppress("DEPRECATED_JAVA_ANNOTATION")

package io.github.pylonmc.pylon.core.recipe

import java.lang.annotation.ElementType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
@java.lang.annotation.Target(ElementType.RECORD_COMPONENT)
annotation class RecipeKey
