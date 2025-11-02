package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import io.github.pylonmc.pylon.core.i18n.packet.PlayerPacketHandler
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.nbt.TextComponentTagVisitor
import net.minecraft.world.level.block.state.properties.Property
import org.bukkit.block.Block
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
        val player = (player as CraftPlayer).handle
        val inventory = player.containerMenu
        for (slot in 0..45) {
            val item = inventory.getSlot(slot).item
            player.containerSynchronizer.sendSlotChange(inventory, slot, item)
        }
    }

    override fun resendRecipeBook(player: Player) {
        val player = (player as CraftPlayer).handle
        player.recipeBook.sendInitialRecipeBook(player)
    }

    override fun serializePdc(pdc: PersistentDataContainer): Component
        = PaperAdventure.asAdventure(TextComponentTagVisitor("  ").visit((pdc as CraftPersistentDataContainer).toTagCompound()))

    override fun getStateProperties(block: Block, custom: Map<String, Pair<String, Int>>): Map<String, String> {
        val state = (block as CraftBlock).nms
        val map = mutableMapOf<String, String>()
        val possibleValues = mutableMapOf<String, Int>()
        for (property in state.properties) {
            @Suppress("UNCHECKED_CAST")
            property as Property<Comparable<Any>>
            map[property.name] = state.getOptionalValue(property).map(property::getName).orElse("none")
            possibleValues[property.name] = property.possibleValues.size
        }
        for ((name, pair) in custom) {
            map[name] = pair.first
            possibleValues[name] = pair.second
        }
        return map.toSortedMap().toSortedMap(compareBy<String> { possibleValues[it] ?: 0 }.reversed())
    }
}