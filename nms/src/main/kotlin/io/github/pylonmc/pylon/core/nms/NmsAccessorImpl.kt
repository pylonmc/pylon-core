package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import io.github.pylonmc.pylon.core.i18n.packet.PlayerPacketHandler
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.nbt.TextComponentTagVisitor
import net.minecraft.world.item.Item
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.craftbukkit.CraftEquipmentSlot
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object NmsAccessorImpl : NmsAccessor {

    private val players = ConcurrentHashMap<UUID, PlayerPacketHandler>()

    override fun damageItem(itemStack: ItemStack, amount: Int, world: World, onBreak: (Material) -> Unit, force: Boolean) {
        (itemStack as CraftItemStack).handle.hurtAndBreak(amount, (world as CraftWorld).handle, null, { item: Item ->
            onBreak(CraftItemType.minecraftToBukkit(item))
        }, force)
    }

    override fun damageItem(itemStack: ItemStack, amount: Int, entity: LivingEntity, slot: EquipmentSlot?, force: Boolean) {
        val nmsSlot = slot?.let { CraftEquipmentSlot.getNMS(it) }
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // slot can be null, the nms method parameter is annotated as such, but for some reason it still has a warning
        (itemStack as CraftItemStack).handle.hurtAndBreak(amount, (entity as CraftLivingEntity).handle, nmsSlot, force)
    }

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
}