package io.github.pylonmc.pylon.core.block

import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent

interface BlockCreateContext {

    data class PlayerPlace(val player: Player, val event: BlockPlaceEvent) : BlockCreateContext

    data object Default : BlockCreateContext
}