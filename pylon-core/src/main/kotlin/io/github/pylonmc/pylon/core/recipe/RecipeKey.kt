@file:Suppress("DEPRECATED_JAVA_ANNOTATION")

package io.github.pylonmc.pylon.core.recipe

import java.lang.annotation.ElementType

/**
 * Used to annotate the "key" component of a recipe record.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
@java.lang.annotation.Target(ElementType.RECORD_COMPONENT)
annotation class RecipeKey
