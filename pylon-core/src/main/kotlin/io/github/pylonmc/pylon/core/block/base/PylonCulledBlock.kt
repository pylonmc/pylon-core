package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.isVisibilityInverted
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

interface PylonCulledBlock {
    /**
     * Any entities that should be culled when the block is considered culled by the BlockTextureEngine.
     * You **cannot** include entities in this list that are invisible by default, if you do they will not be culled properly.
     */
    val culledEntityIds: Iterable<UUID>

    fun hideEntities(player: Player) {
        for (entityId in culledEntityIds) {
            if (!player.isVisibilityInverted(entityId)) {
                Bukkit.getEntity(entityId)?.let { entity ->
                    player.hideEntity(PylonCore, entity)
                }
            }
        }
    }

    fun showEntities(player: Player) {
        for (entityId in culledEntityIds) {
            if (player.isVisibilityInverted(entityId)) {
                Bukkit.getEntity(entityId)?.let { entity ->
                    player.showEntity(PylonCore, entity)
                }
            }
        }
    }
}