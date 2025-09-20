package io.github.pylonmc.pylon.core.util

import org.bukkit.Material
import org.bukkit.Tag

/**
 * Represents the blocks that a tool made of some material is allowed to break.
 *
 * For example, at a mining level of iron you cannot break obsidian.
 */
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

    /**
     * @return Whether this mining level is capable of mining the given [material]
     */
    fun canMine(material: Material): Boolean {
        return when {
            UNBREAKABLE.contains(material) -> false
            incorrectTag == null -> !material.createBlockData().requiresCorrectToolForDrops()
            else -> !incorrectTag.isTagged(material)
        }
    }

    /**
     * @return Whether this mining level is at least the given [level]. For example, iron is at least any, wood, stone, gold, and iron.
     */
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
