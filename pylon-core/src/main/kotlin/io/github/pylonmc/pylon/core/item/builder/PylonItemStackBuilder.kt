package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.util.ToolType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

object PylonItemStackBuilder {

    @JvmStatic
    fun helmet(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.HEAD, durability)

    @JvmStatic
    fun helmet(material: Material, key: NamespacedKey, durability: Boolean) = helmet(ItemStack(material), key, durability)

    @JvmStatic
    fun chestPlate(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.CHEST, durability)

    @JvmStatic
    fun chestPlate(material: Material, key: NamespacedKey, durability: Boolean) = chestPlate(ItemStack(material), key, durability)

    @JvmStatic
    fun leggings(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.LEGS, durability)

    @JvmStatic
    fun leggings(material: Material, key: NamespacedKey, durability: Boolean) = leggings(ItemStack(material), key, durability)

    @JvmStatic
    fun boots(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.FEET, durability)

    @JvmStatic
    fun boots(material: Material, key: NamespacedKey, durability: Boolean) = boots(ItemStack(material), key, durability)

    @JvmStatic
    fun armor(stack: ItemStack, key: NamespacedKey, slot: EquipmentSlotGroup, durability: Boolean) = create(stack, key) { builder, settings ->
        builder.armor(
            slot,
            settings.getOrThrow("armor", ConfigAdapter.DOUBLE),
            settings.getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
        )

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        }
    }

    @JvmStatic
    fun armor(material: Material, key: NamespacedKey, slot: EquipmentSlotGroup, durability: Boolean)
        = armor(ItemStack(material), key, slot, durability)

    @JvmStatic
    fun tool(stack: ItemStack, key: NamespacedKey, toolType: ToolType, durability: Boolean) = create(stack, key) { builder, settings ->
        builder.tool(
            toolType.getTag(),
            settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
            settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
        )

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        }
    }

    @JvmStatic
    fun tool(material: Material, key: NamespacedKey, toolType: ToolType, durability: Boolean)
        = tool(ItemStack(material), key, toolType, durability)

    @JvmStatic
    fun toolWeapon(stack: ItemStack, key: NamespacedKey, toolType: ToolType, durability: Boolean, knockback: Boolean, disablesShield: Boolean): ItemStackBuilder {
        val settings = Settings.get(key)
        return weapon(stack, key, durability, knockback, disablesShield).tool(
            toolType.getTag(),
            settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
            settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
        )
    }

    @JvmStatic
    fun toolWeapon(material: Material, key: NamespacedKey, toolType: ToolType, durability: Boolean, knockback: Boolean, disablesShield: Boolean)
        = toolWeapon(ItemStack(material), key, toolType, durability, knockback, disablesShield)

    @JvmStatic
    fun weapon(stack: ItemStack, key: NamespacedKey, durability: Boolean, knockback: Boolean, disablesShield: Boolean) = create(stack, key) { builder, settings ->
        builder.weapon(
            settings.getOrThrow("attack-damage", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-speed", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-durability-damage", ConfigAdapter.INT),
            if (disablesShield) settings.getOrThrow("disable-shield-seconds", ConfigAdapter.FLOAT) else 0f
        )

        if (knockback) {
            builder.attackKnockback(settings.getOrThrow("attack-knockback", ConfigAdapter.DOUBLE))
        }

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        }
    }

    @JvmStatic
    fun weapon(material: Material, key: NamespacedKey, durability: Boolean, knockback: Boolean, disablesShield: Boolean)
        = weapon(ItemStack(material), key, durability, knockback, disablesShield)

    private fun create(stack: ItemStack, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Unit): ItemStackBuilder {
        val builder = ItemStackBuilder.pylonItem(stack, key)
        val settings = Settings.get(key)
        consumer(builder, settings)
        return builder
    }
}