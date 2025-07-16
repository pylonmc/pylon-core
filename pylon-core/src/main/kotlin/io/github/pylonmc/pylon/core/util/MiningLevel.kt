package io.github.pylonmc.pylon.core.util

import org.bukkit.Material
import org.bukkit.Tag

enum class MiningLevel(val numericalLevel: Int, private val incorrectTag: Tag<Material>?) {
    ANY(0, null),
    WOOD(1, Tag.INCORRECT_FOR_WOODEN_TOOL),
    GOLD(1, Tag.INCORRECT_FOR_GOLD_TOOL),
    STONE(2, Tag.INCORRECT_FOR_STONE_TOOL),
    IRON(3, Tag.INCORRECT_FOR_IRON_TOOL),
    DIAMOND(4, Tag.INCORRECT_FOR_DIAMOND_TOOL),

    // Internally netherite is higher than diamond, but practically they are the same
    NETHERITE(4, Tag.INCORRECT_FOR_NETHERITE_TOOL),
    ;

    fun canMine(material: Material): Boolean {
        return when {
            UNBREAKABLE.contains(material) -> false
            incorrectTag == null -> !material.createBlockData().requiresCorrectToolForDrops()
            else -> !incorrectTag.isTagged(material)
        }
    }

    fun isAtLeast(level: MiningLevel): Boolean {
        return numericalLevel >= level.numericalLevel
    }

    override fun toString(): String {
        return name.lowercase()
    }

    companion object {
        @JvmField
        val UNBREAKABLE: Set<Material> = setOf(
            Material.BARRIER,
            Material.BEDROCK,
            Material.STRUCTURE_BLOCK,
            Material.JIGSAW,
            Material.END_PORTAL_FRAME,
            Material.COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
        )
    }
}
