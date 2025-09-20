@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.resourcepack.armor

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonArmor
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object ArmorTextureEngine : PacketListener {
    val customArmorTexturesKey = pylonKey("custom_armor_textures")
    val armorSlots = setOf(5, 6, 7, 8)

    @get:JvmStatic
    @set:JvmStatic
    var Player.customArmorTextures: Boolean
        get() = this.persistentDataContainer.getOrDefault(customArmorTexturesKey, PersistentDataType.BOOLEAN, false)
        set(value) = this.persistentDataContainer.set(customArmorTexturesKey, PersistentDataType.BOOLEAN, value)

    override fun onPacketSend(event: PacketSendEvent?) {
        if (event == null) return

        val player = event.getPlayer<Player>()
        if (player == null || !player.customArmorTextures) return

        when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(WrapperPlayServerSetSlot(event))
            PacketType.Play.Server.WINDOW_ITEMS -> handleWindowItems(WrapperPlayServerWindowItems(event))
            PacketType.Play.Server.SET_PLAYER_INVENTORY -> handlePlayerInventorySlot(WrapperPlayServerSetPlayerInventory(event))
            PacketType.Play.Server.ENTITY_EQUIPMENT -> handleEntityEquipmentPacket(WrapperPlayServerEntityEquipment(event))
            else -> {}
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent?) {
        if (event == null) return

        val player = event.getPlayer<Player>()
        if (player == null) return

        when (event.packetType) {
            PacketType.Play.Client.CREATIVE_INVENTORY_ACTION -> handleCreativeAction(WrapperPlayClientCreativeInventoryAction(event))
            else -> {}
        }
    }

    private fun handleSetSlot(packet: WrapperPlayServerSetSlot) {
        if (armorSlots.contains(packet.slot)) {
            packet.item = addArmorTexture(packet.item)
        }
    }

    private fun handleWindowItems(packet: WrapperPlayServerWindowItems) {
        packet.items.toList().forEachIndexed { index, itemStack ->
            if (armorSlots.contains(index)) {
                packet.items[index] = addArmorTexture(itemStack)
            }
        }
    }

    private fun handlePlayerInventorySlot(packet : WrapperPlayServerSetPlayerInventory) {
        if (armorSlots.contains(packet.slot)) {
            packet.stack = addArmorTexture(packet.stack)
        }
    }

    private fun handleEntityEquipmentPacket(packet: WrapperPlayServerEntityEquipment) {
        packet.equipment.forEach { equipment ->
            equipment.item = addArmorTexture(equipment.item)
        }
    }

    private fun addArmorTexture(packetStack: ItemStack?) : ItemStack? {
        if (packetStack == null) return null
        val stack = SpigotConversionUtil.toBukkitItemStack(packetStack)
        val item = PylonItem.fromStack(stack)
        if (item != null && item is PylonArmor) {
            val template = item.schema.itemStack
            val defaultAssetId = template.getDataOrDefault(DataComponentTypes.EQUIPPABLE, template.type.getDefaultData(DataComponentTypes.EQUIPPABLE))?.assetId()
            val component = stack.getDataOrDefault(DataComponentTypes.EQUIPPABLE, stack.type.getDefaultData(DataComponentTypes.EQUIPPABLE))
            if (component == null || component.assetId() != defaultAssetId) {
                return packetStack
            }

            stack.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(component.slot())
                .assetId(item.equipmentType)
                .swappable(component.swappable())
                .allowedEntities(component.allowedEntities())
                .cameraOverlay(component.cameraOverlay())
                .canBeSheared(component.canBeSheared())
                .damageOnHurt(component.damageOnHurt())
                .dispensable(component.dispensable())
                .equipSound(component.equipSound())
                .shearSound(component.shearSound())
                .canBeSheared(component.canBeSheared()))
            return SpigotConversionUtil.fromBukkitItemStack(stack)
        }
        return packetStack
    }

    private fun handleCreativeAction(packet: WrapperPlayClientCreativeInventoryAction) {
        if (packet.itemStack == null) return
        val stack = SpigotConversionUtil.toBukkitItemStack(packet.itemStack)
        val item = PylonItem.fromStack(stack)
        if (item != null && item is PylonArmor) {
            val component = stack.getData(DataComponentTypes.EQUIPPABLE) ?: return
            if (component.assetId() == item.equipmentType) {
                val template = item.schema.itemStack
                val defaultComponent = template.getData(DataComponentTypes.EQUIPPABLE)
                if (defaultComponent == null) {
                    stack.resetData(DataComponentTypes.EQUIPPABLE)
                } else {
                    stack.setData(DataComponentTypes.EQUIPPABLE, defaultComponent)
                }
                packet.itemStack = SpigotConversionUtil.fromBukkitItemStack(stack)
            }
        }
    }
}