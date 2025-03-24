package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

data class BlockLoadContext(override val block: Block, val pdc: PersistentDataContainer) : BlockContext