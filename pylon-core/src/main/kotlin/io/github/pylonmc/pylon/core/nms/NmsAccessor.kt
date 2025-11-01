package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus

/**
 * Internal, not for innocent eyes to see, move along now.
 */
@ApiStatus.Internal
@ApiStatus.NonExtendable
interface NmsAccessor {

    fun damageItem(itemStack: ItemStack, amount: Int, world: World, onBreak: (Material) -> Unit, force: Boolean = false)

    fun damageItem(itemStack: ItemStack, amount: Int, entity: LivingEntity, slot: EquipmentSlot?, force: Boolean = false)

    fun registerTranslationHandler(player: Player, handler: PlayerTranslationHandler)

    fun unregisterTranslationHandler(player: Player)

    fun resendInventory(player: Player)

    fun resendRecipeBook(player: Player)

    fun serializePdc(pdc: PersistentDataContainer): Component

    companion object {
        val instance = Class.forName("io.github.pylonmc.pylon.core.nms.NmsAccessorImpl")
            .getDeclaredField("INSTANCE")
            .get(null) as NmsAccessor
    }
}