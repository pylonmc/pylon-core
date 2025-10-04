package io.github.pylonmc.pylon.core.util

import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.Registry
import org.bukkit.block.BlockType

@Suppress("UnstableApiUsage")
enum class ToolType(val tagKey: TagKey<BlockType>) {
    AXE(BlockTypeTagKeys.MINEABLE_AXE),
    PICKAXE(BlockTypeTagKeys.MINEABLE_PICKAXE),
    SHOVEL(BlockTypeTagKeys.MINEABLE_SHOVEL),
    HOE(BlockTypeTagKeys.MINEABLE_HOE);

    fun getTag() = Registry.BLOCK.getTag(tagKey)
}