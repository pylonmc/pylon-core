package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import io.github.pylonmc.pylon.core.i18n.packet.PlayerPacketHandler
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Transformation
import org.joml.Matrix4f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.mojang.math.Transformation as MojangTransformation

@Suppress("unused")
object NmsAccessorImpl : NmsAccessor {

    private val players = ConcurrentHashMap<UUID, PlayerPacketHandler>()

    override fun registerTranslationHandler(player: Player, handler: PlayerTranslationHandler) {
        if (players.containsKey(player.uniqueId)) return
        val handler = PlayerPacketHandler((player as CraftPlayer).handle, handler)
        players[player.uniqueId] = handler
        handler.register()
    }

    override fun unregisterTranslationHandler(player: Player) {
        val handler = players.remove(player.uniqueId) ?: return
        handler.unregister()
    }

    override fun resendInventory(player: Player) {
        val handler = players[player.uniqueId] ?: return
        handler.resendInventory()
    }

    override fun serializePdc(pdc: PersistentDataContainer): String
        = (pdc as CraftPersistentDataContainer).serialize()

    override fun decomposeMatrix(matrix: Matrix4f): Transformation {
        val mojangTransform = MojangTransformation(matrix)
        return Transformation(
            mojangTransform.translation,
            mojangTransform.leftRotation,
            mojangTransform.scale,
            mojangTransform.rightRotation
        )
    }
}