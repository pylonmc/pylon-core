@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translate
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.Locale
import java.util.Optional
import java.util.WeakHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.system.measureNanoTime
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItemStack

@ApiStatus.Internal
object PlayerTranslationHandler : PacketListener {

    override fun onPacketSend(event: PacketSendEvent) {
        val locale = Bukkit.getPlayer(event.user.uuid)?.locale() ?: return
        when (event.packetType) {
            PacketType.Play.Server.WINDOW_ITEMS -> handleWindowItemsPacket(WrapperPlayServerWindowItems(event), locale)
            PacketType.Play.Server.SET_SLOT -> handleSetSlotPacket(WrapperPlayServerSetSlot(event), locale)
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packetType) {
            PacketType.Play.Client.CLICK_WINDOW -> handleClickWindowPacket(WrapperPlayClientClickWindow(event))
        }
    }

    private fun handleWindowItemsPacket(packet: WrapperPlayServerWindowItems, locale: Locale) {
        println("A: " + measureNanoTime {
            packet.items = packet.items.map { translateItem(it, locale) }
            packet.setCarriedItem(packet.carriedItem.getOrNull()?.let { translateItem(it, locale) })
        })
    }

    private fun handleSetSlotPacket(packet: WrapperPlayServerSetSlot, locale: Locale) {
        println("A: " + measureNanoTime {
            packet.item = translateItem(packet.item, locale)
        })
    }

    private fun handleClickWindowPacket(packet: WrapperPlayClientClickWindow) {
        packet.setStateID(Optional.of(-1))
    }

    private fun translateItem(item: PacketItemStack, locale: Locale): PacketItemStack {
        if (item.isEmpty) return item
        val pylonItem = PylonItem.fromStack(SpigotConversionUtil.toBukkitItemStack(item)) ?: return item
        return SpigotConversionUtil.fromBukkitItemStack(translateItem(pylonItem, locale))
    }

    private fun resetItem(item: PacketItemStack): PacketItemStack {
        if (item.isEmpty) return item
        val pylonItem = PylonItem.fromStack(SpigotConversionUtil.toBukkitItemStack(item)) ?: return item
        return SpigotConversionUtil.fromBukkitItemStack(reset(pylonItem))
    }

    private fun translateItem(item: PylonItem, locale: Locale): ItemStack {
        val stack = item.stack
        if (stack.persistentDataContainer.has(translatedKey)) return stack

        stack.translate(locale, item.getPlaceholders())
        stack.editData(DataComponentTypes.LORE) { lore ->
            val newLore = lore.lines().toMutableList()
            newLore.add(GlobalTranslator.render(item.addon.displayName, locale))
            if (item.isDisabled) {
                newLore.add(
                    GlobalTranslator.render(
                        Component.translatable("pylon.pyloncore.message.disabled.lore"),
                        locale
                    )
                )
            }
            ItemLore.lore(newLore)
        }

        stack.itemMeta.persistentDataContainer.set(translatedKey, PylonSerializers.BOOLEAN, true)

        return stack
    }

    private fun reset(item: PylonItem): ItemStack {
        val name = names.getOrPut(item.schema) {
            item.schema.itemStack.getData(DataComponentTypes.ITEM_NAME)!!
        }
        val lore = lores.getOrPut(item.schema) {
            item.schema.itemStack.getData(DataComponentTypes.LORE)!!
        }
        item.stack.setData(DataComponentTypes.ITEM_NAME, name)
        item.stack.setData(DataComponentTypes.LORE, lore)
        return item.stack
    }

    private val names = WeakHashMap<PylonItemSchema, Component>()
    private val lores = WeakHashMap<PylonItemSchema, ItemLore>()
    private val translatedKey = pylonKey("translated")
}