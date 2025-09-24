package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.Tool
import io.papermc.paper.datacomponent.item.UseCooldown
import io.papermc.paper.datacomponent.item.Weapon
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import io.papermc.paper.registry.set.RegistryKeySet
import net.kyori.adventure.util.TriState
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.BlockType
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
class PylonItemStackBuilder : ItemStackBuilder {
    private val itemKey: NamespacedKey
    
    internal constructor(stack: ItemStack, itemKey: NamespacedKey) : super(stack) {
        this.itemKey = itemKey
    }

    @JvmOverloads
    fun helmet(
        armor: Double = Settings.get(itemKey).getOrThrow("armor", ConfigAdapter.DOUBLE),
        armorToughness: Double = Settings.get(itemKey).getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
    ) = armor(EquipmentSlotGroup.HEAD, armor, armorToughness)

    @JvmOverloads
    fun chestPlate(
        armor: Double = Settings.get(itemKey).getOrThrow("armor", ConfigAdapter.DOUBLE),
        armorToughness: Double = Settings.get(itemKey).getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
    ) = armor(EquipmentSlotGroup.CHEST, armor, armorToughness)

    @JvmOverloads
    fun leggings(
        armor: Double = Settings.get(itemKey).getOrThrow("armor", ConfigAdapter.DOUBLE),
        armorToughness: Double = Settings.get(itemKey).getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
    ) = armor(EquipmentSlotGroup.LEGS, armor, armorToughness)

    @JvmOverloads
    fun boots(
        armor: Double = Settings.get(itemKey).getOrThrow("armor", ConfigAdapter.DOUBLE),
        armorToughness: Double = Settings.get(itemKey).getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
    ) = armor(EquipmentSlotGroup.FEET, armor, armorToughness)

    @JvmOverloads
    fun armor(
        slot: EquipmentSlotGroup,
        armor: Double = Settings.get(itemKey).getOrThrow("armor", ConfigAdapter.DOUBLE),
        armorToughness: Double = Settings.get(itemKey).getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
    ) = apply {
        editDataOrSet(DataComponentTypes.ATTRIBUTE_MODIFIERS) { modifiers ->
            val copying = modifiers?.modifiers()?.filter { it.modifier().key.namespace != "minecraft" || !it.modifier().key.key.contains("armor.") }
            ItemAttributeModifiers.itemAttributes().copy(copying)
                .addModifier(Attribute.ARMOR, AttributeModifier(itemKey, armor, AttributeModifier.Operation.ADD_NUMBER, slot))
                .addModifier(Attribute.ARMOR_TOUGHNESS, AttributeModifier(itemKey, armorToughness, AttributeModifier.Operation.ADD_NUMBER, slot))
                .build()
        }
    }

    @JvmOverloads
    fun axe(
        miningSpeed: Float = Settings.get(itemKey).getOrThrow("mining-speed", ConfigAdapter.FLOAT),
        miningDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("mining-durability-damage", ConfigAdapter.INT)
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_AXE), miningSpeed, miningDurabilityDamage)

    @JvmOverloads
    fun pickaxe(
        miningSpeed: Float = Settings.get(itemKey).getOrThrow("mining-speed", ConfigAdapter.FLOAT),
        miningDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("mining-durability-damage", ConfigAdapter.INT)
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_PICKAXE), miningSpeed, miningDurabilityDamage)

    @JvmOverloads
    fun shovel(
        miningSpeed: Float = Settings.get(itemKey).getOrThrow("mining-speed", ConfigAdapter.FLOAT),
        miningDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("mining-durability-damage", ConfigAdapter.INT)
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_SHOVEL), miningSpeed, miningDurabilityDamage)

    @JvmOverloads
    fun hoe(
        miningSpeed: Float = Settings.get(itemKey).getOrThrow("mining-speed", ConfigAdapter.FLOAT),
        miningDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("mining-durability-damage", ConfigAdapter.INT)
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_HOE), miningSpeed, miningDurabilityDamage)

    @JvmOverloads
    fun tool(
        blocks: RegistryKeySet<BlockType>,
        miningSpeed: Float = Settings.get(itemKey).getOrThrow("mining-speed", ConfigAdapter.FLOAT),
        miningDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("mining-durability-damage", ConfigAdapter.INT)
    ) = apply {
        set(DataComponentTypes.TOOL, Tool.tool()
            .defaultMiningSpeed(miningSpeed)
            .damagePerBlock(miningDurabilityDamage)
            .addRule(Tool.rule(blocks, miningSpeed, TriState.TRUE)))
    }

    fun noTool() = unset(DataComponentTypes.TOOL) as PylonItemStackBuilder

    @JvmOverloads
    fun weapon(
        disablesShield: Boolean = false,
        attackDamage: Double = Settings.get(itemKey).getOrThrow("attack-damage", ConfigAdapter.DOUBLE),
        attackSpeed: Double = Settings.get(itemKey).getOrThrow("attack-speed", ConfigAdapter.DOUBLE),
        attackDurabilityDamage: Int = Settings.get(itemKey).getOrThrow("attack-durability-damage", ConfigAdapter.INT),
        disableShieldSeconds: Float? = null
    ) = apply {
        addAttributeModifier(Attribute.ATTACK_DAMAGE, AttributeModifier(baseAttackDamage, -1.0 + attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
        addAttributeModifier(Attribute.ATTACK_SPEED, AttributeModifier(baseAttackSpeed, -4.0 + attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
        set(DataComponentTypes.WEAPON, Weapon.weapon()
            .itemDamagePerAttack(attackDurabilityDamage)
            .disableBlockingForSeconds(if (disablesShield) disableShieldSeconds ?: Settings.get(itemKey).getOrThrow("disable-shield-seconds", ConfigAdapter.FLOAT) else 0f))
    }

    @JvmOverloads
    fun attackKnockback(knockback: Double = Settings.get(itemKey).getOrThrow("attack-knockback", ConfigAdapter.DOUBLE)) =
        addAttributeModifier(Attribute.ATTACK_KNOCKBACK, AttributeModifier(itemKey, knockback, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)) as PylonItemStackBuilder

    @JvmOverloads
    fun durability(
        durability: Int = Settings.get(itemKey).getOrThrow("durability", ConfigAdapter.INT)
    ) = set(DataComponentTypes.MAX_DAMAGE, durability) as PylonItemStackBuilder

    @JvmOverloads
    fun useCooldown(
        cooldownTicks: Int = Settings.get(itemKey).getOrThrow("cooldown-ticks", ConfigAdapter.INT)
    ) = set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldownTicks / 20.0f).cooldownGroup(itemKey)) as PylonItemStackBuilder
}