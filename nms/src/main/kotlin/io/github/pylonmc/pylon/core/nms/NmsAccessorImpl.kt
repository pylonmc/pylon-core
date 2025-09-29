package io.github.pylonmc.pylon.core.nms

import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer

@Suppress("unused")
object NmsAccessorImpl : NmsAccessor {

    //private val players = ConcurrentHashMap<UUID, PlayerPacketHandler>()

//    override fun registerTranslationHandler(player: Player, handler: PlayerTranslationHandler) {
//        if (players.containsKey(player.uniqueId)) return
//        val handler = PlayerPacketHandler((player as CraftPlayer).handle, handler)
//        players[player.uniqueId] = handler
//        handler.register()
//    }
//
//    override fun unregisterTranslationHandler(player: Player) {
//        val handler = players.remove(player.uniqueId) ?: return
//        handler.unregister()
//    }

    override fun resendInventory(player: Player) {
        val serverPlayer = (player as CraftPlayer).handle
        val inventory = serverPlayer.containerMenu
        for (slot in 0..45) {
            val item = inventory.getSlot(slot).item
            serverPlayer.containerSynchronizer.sendSlotChange(inventory, slot, item)
        }
    }

    override fun resendRecipeBook(player: Player) {
        val player = (player as CraftPlayer).handle
        player.recipeBook.sendInitialRecipeBook(player)
    }

    override fun serializePdc(pdc: PersistentDataContainer): String
        = (pdc as CraftPersistentDataContainer).serialize()
}