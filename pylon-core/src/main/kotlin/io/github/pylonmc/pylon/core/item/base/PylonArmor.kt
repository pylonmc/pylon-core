package io.github.pylonmc.pylon.core.item.base

import net.kyori.adventure.key.Key
import io.github.pylonmc.pylon.core.item.PylonItem

/**
 * Represents a [PylonItem] that is wearable piece of armor.
 * Right now this is only used to determine the equipment type for custom armor textures.
 */
interface PylonArmor {
    /**
     * The equipment type of this armor piece, used for custom armor textures.
     * All armor pieces of the same type should have the same equipment type.
     *
     * For example,
     *
     * `Diamond Helmet` -> `minecraft:diamond`
     *
     * `Bronze Chestplate` -> `pylonbase:bronze`
     */
    val equipmentType: Key
}