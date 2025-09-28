package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import io.papermc.paper.registry.set.RegistryKeySet
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.block.BlockType
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
object PylonItemStackBuilder {

    @JvmStatic
    fun helmet(stack: ItemStack, key: NamespacedKey) = armor(stack, key, EquipmentSlotGroup.HEAD)

    @JvmStatic
    fun helmet(material: Material, key: NamespacedKey) = helmet(ItemStack(material), key)

    @JvmStatic
    fun chestPlate(stack: ItemStack, key: NamespacedKey) = armor(stack, key, EquipmentSlotGroup.CHEST)

    @JvmStatic
    fun chestPlate(material: Material, key: NamespacedKey) = chestPlate(ItemStack(material), key)

    @JvmStatic
    fun leggings(stack: ItemStack, key: NamespacedKey) = armor(stack, key, EquipmentSlotGroup.LEGS)

    @JvmStatic
    fun leggings(material: Material, key: NamespacedKey) = leggings(ItemStack(material), key)

    @JvmStatic
    fun boots(stack: ItemStack, key: NamespacedKey) = armor(stack, key, EquipmentSlotGroup.FEET)

    @JvmStatic
    fun boots(material: Material, key: NamespacedKey) = boots(ItemStack(material), key)

    @JvmStatic
    fun armor(stack: ItemStack, key: NamespacedKey, slot: EquipmentSlotGroup) = create(stack, key) { builder, settings ->
        builder.armor(
            slot,
            settings.getOrThrow("armor", ConfigAdapter.DOUBLE),
            settings.getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
        )
    }

    @JvmStatic
    fun axe(stack: ItemStack, key: NamespacedKey) = tool(stack, key, Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_AXE))

    @JvmStatic
    fun axe(material: Material, key: NamespacedKey) = axe(ItemStack(material), key)

    @JvmStatic
    fun pickaxe(stack: ItemStack, key: NamespacedKey) = tool(stack, key, Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_PICKAXE))

    @JvmStatic
    fun pickaxe(material: Material, key: NamespacedKey) = pickaxe(ItemStack(material), key)

    @JvmStatic
    fun shovel(stack: ItemStack, key: NamespacedKey) = tool(stack, key, Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_SHOVEL))

    @JvmStatic
    fun shovel(material: Material, key: NamespacedKey) = shovel(ItemStack(material), key)

    @JvmStatic
    fun hoe(stack: ItemStack, key: NamespacedKey) = tool(stack, key, Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_HOE))

    @JvmStatic
    fun hoe(material: Material, key: NamespacedKey) = hoe(ItemStack(material), key)

    @JvmStatic
    fun tool(stack: ItemStack, key: NamespacedKey, blocks: RegistryKeySet<BlockType>) = create(stack, key) { builder, settings ->
        builder.tool(
            blocks,
            settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
            settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
        )
    }

    @JvmStatic
    fun weapon(stack: ItemStack, key: NamespacedKey, knockback: Boolean, disablesShield: Boolean) = create(stack, key) { builder, settings ->
        builder.weapon(
            settings.getOrThrow("attack-damage", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-speed", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-durability-damage", ConfigAdapter.INT),
            if (disablesShield) settings.getOrThrow("disable-shield-seconds", ConfigAdapter.FLOAT) else 0f
        )

        if (knockback) {
            builder.attackKnockback(settings.getOrThrow("attack-knockback", ConfigAdapter.DOUBLE))
        }
    }

    private fun create(stack: ItemStack, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Unit): ItemStackBuilder {
        val builder = ItemStackBuilder.pylonItem(stack, key)
        val settings = Settings.get(key)
        consumer(builder, settings)
        return builder
    }
}