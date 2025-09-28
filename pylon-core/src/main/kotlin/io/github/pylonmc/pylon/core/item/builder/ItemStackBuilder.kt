package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translate
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.editDataOrDefault
import io.github.pylonmc.pylon.core.util.editDataOrSet
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.Tool
import io.papermc.paper.datacomponent.item.UseCooldown
import io.papermc.paper.datacomponent.item.Weapon
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import io.papermc.paper.registry.set.RegistryKeySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.util.TriState
import org.apache.commons.lang3.LocaleUtils
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.BlockType
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Consumer
import kotlin.collections.forEach

/**
 * Helper class for creating an [ItemStack], including utilities for creating Pylon
 * items specifically.
 *
 * Implements InvUI's [ItemProvider], so can be used instead of an [ItemStack] in GUIs.
 *
 * You should use this when using anything to do with [Component.translatable] including
 * [io.github.pylonmc.pylon.core.item.PylonItem]s in InvUI GUIs. Yes, this is confusing
 * and annoying - it is unfortunately necessary to get around InvUI's translation system.
 */
@Suppress("UnstableApiUsage")
open class ItemStackBuilder internal constructor(val stack: ItemStack) : ItemProvider {
    fun amount(amount: Int) = apply {
        stack.amount = amount
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, valueBuilder: DataComponentBuilder<T>) = apply {
        stack.setData(type, valueBuilder)
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, value: T) = apply {
        stack.setData(type, value)
    }

    fun set(type: DataComponentType.NonValued) = apply {
        stack.setData(type)
    }

    /**
     * @see ItemStack.unsetData
     */
    fun unset(type: DataComponentType) = apply {
        stack.unsetData(type)
    }

    /**
     * @see ItemStack.resetData
     */
    fun reset(type: DataComponentType) = apply {
        stack.resetData(type)
    }

    fun editMeta(consumer: Consumer<in ItemMeta>) = apply {
        stack.editMeta(consumer)
    }

    fun editPdc(consumer: Consumer<PersistentDataContainer>) = apply {
        stack.editPersistentDataContainer(consumer)
    }

    fun <T : Any> editData(type: DataComponentType.Valued<T>, block: (T) -> T) = apply {
        stack.editData(type, block)
    }

    fun <T : Any> editDataOrDefault(type: DataComponentType.Valued<T>, block: (T) -> T) = apply {
        stack.editDataOrDefault(type, block)
    }

    fun <T : Any> editDataOrSet(type: DataComponentType.Valued<T>, block: (T?) -> T) = apply {
        stack.editDataOrSet(type, block)
    }

    fun name(name: Component) = set(DataComponentTypes.ITEM_NAME, name)

    fun name(name: String) = name(fromMiniMessage(name))

    /**
     * Sets the item's name to the default language file key (for example
     * `pylon.pyloncore.item.my_dumb_item.name`), based on the item [key] given.
     *
     * Use [pylonItem] instead of this to create a stack for a [io.github.pylonmc.pylon.core.item.PylonItem].
     */
    fun defaultTranslatableName(key: NamespacedKey) =
        name(Component.translatable(nameKey(key)))

    fun lore(loreToAdd: List<ComponentLike>) = apply {
        val lore = ItemLore.lore()
        stack.getData(DataComponentTypes.LORE)?.let { lore.addLines(it.lines()) }
        lore.addLines(loreToAdd)
        stack.setData(DataComponentTypes.LORE, lore)
    }

    fun lore(vararg loreToAdd: ComponentLike) = lore(loreToAdd.toList())

    fun lore(vararg lore: String) = lore(*lore.map(::fromMiniMessage).toTypedArray())

    /**
     * Sets the item's lore to the default language file key (for example
     * `pylon.pyloncore.item.my_dumb_item.lore`), based on the item [key] given.
     *
     * Use [pylonItem] instead of this to create a stack for a [io.github.pylonmc.pylon.core.item.PylonItem].
     */
    fun defaultTranslatableLore(key: NamespacedKey) =
        lore(Component.translatable(loreKey(key), ""))

    fun editCustomModelData(editFunction: Consumer<CustomModelData.Builder>) = apply {
        val customModelData = stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
        val newCustomModelData = CustomModelData.customModelData()

        customModelData?.flags()?.let { newCustomModelData.addFlags(it) }
        customModelData?.strings()?.let { newCustomModelData.addStrings(it) }
        customModelData?.floats()?.let { newCustomModelData.addFloats(it) }
        customModelData?.colors()?.let { newCustomModelData.addColors(it) }

        editFunction.accept(newCustomModelData)

        stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, newCustomModelData)
    }

    /**
     * Adds a string to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataString(string: String) =
        editCustomModelData { it.addString(string) }

    /**
     * Adds a float to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataString(float: Float) =
        editCustomModelData { it.addFloat(float) }

    /**
     * Adds a boolean to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataString(boolean: Boolean) =
        editCustomModelData { it.addFlag(boolean) }

    /**
     * Adds a color to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataString(color: Color) =
        editCustomModelData { it.addColor(color) }

    @JvmOverloads
    fun addAttributeModifier(
        attribute: Attribute,
        modifier: AttributeModifier,
        replaceExisting: Boolean = true
    ) = apply {
        editDataOrSet(DataComponentTypes.ATTRIBUTE_MODIFIERS) { modifiers ->
            val copying = modifiers?.modifiers()?.filter { !replaceExisting || it.modifier().key != modifier.key }
            ItemAttributeModifiers.itemAttributes().copy(copying)
                .addModifier(attribute, modifier)
                .build()
        }
    }

    fun removeAttributeModifier(
        attribute: Attribute,
        modifierKey: NamespacedKey
    ) = removeAttributeModifiers(attribute) { it.key == modifierKey }

    fun removeAttributeModifiers(
        attribute: Attribute
    ) = removeAttributeModifiers(attribute) { true }

    fun removeAttributeModifiers(
        attribute: Attribute,
        predicate: (AttributeModifier) -> Boolean
    ) = apply {
        editDataOrSet(DataComponentTypes.ATTRIBUTE_MODIFIERS) { modifiers ->
            val copying = modifiers?.modifiers()?.filter { it.attribute() != attribute || !predicate(it.modifier()) }
            ItemAttributeModifiers.itemAttributes().copy(copying).build()
        }
    }

    fun helmet(
        armor: Double,
        armorToughness: Double
    ) = armor(EquipmentSlotGroup.HEAD, armor, armorToughness)

    fun chestPlate(
        armor: Double,
        armorToughness: Double
    ) = armor(EquipmentSlotGroup.CHEST, armor, armorToughness)

    fun leggings(
        armor: Double,
        armorToughness: Double
    ) = armor(EquipmentSlotGroup.LEGS, armor, armorToughness)

    fun boots(
        armor: Double,
        armorToughness: Double
    ) = armor(EquipmentSlotGroup.FEET, armor, armorToughness)

    fun armor(
        slot: EquipmentSlotGroup,
        armor: Double,
        armorToughness: Double
    ) = apply {
        removeAttributeModifiers(Attribute.ARMOR)
        removeAttributeModifiers(Attribute.ARMOR_TOUGHNESS)
        addAttributeModifier(Attribute.ARMOR, AttributeModifier(baseArmor, armor, AttributeModifier.Operation.ADD_NUMBER, slot))
        addAttributeModifier(Attribute.ARMOR_TOUGHNESS, AttributeModifier(baseArmorToughness, armorToughness, AttributeModifier.Operation.ADD_NUMBER, slot))
    }

    fun axe(
        miningSpeed: Float,
        miningDurabilityDamage: Int
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_AXE), miningSpeed, miningDurabilityDamage)

    fun pickaxe(
        miningSpeed: Float,
        miningDurabilityDamage: Int
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_PICKAXE), miningSpeed, miningDurabilityDamage)

    fun shovel(
        miningSpeed: Float,
        miningDurabilityDamage: Int
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_SHOVEL), miningSpeed, miningDurabilityDamage)

    fun hoe(
        miningSpeed: Float,
        miningDurabilityDamage: Int
    ) = tool(Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_HOE), miningSpeed, miningDurabilityDamage)

    fun tool(
        blocks: RegistryKeySet<BlockType>,
        miningSpeed: Float,
        miningDurabilityDamage: Int
    ) = apply {
        set(DataComponentTypes.TOOL, Tool.tool()
            .defaultMiningSpeed(miningSpeed)
            .damagePerBlock(miningDurabilityDamage)
            .addRule(Tool.rule(blocks, miningSpeed, TriState.TRUE)))
    }

    fun noTool() = unset(DataComponentTypes.TOOL)

    fun weapon(
        attackDamage: Double,
        attackSpeed: Double,
        attackDurabilityDamage: Int,
        disableShieldSeconds: Float
    ) = apply {
        addAttributeModifier(Attribute.ATTACK_DAMAGE, AttributeModifier(baseAttackDamage, -1.0 + attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
        addAttributeModifier(Attribute.ATTACK_SPEED, AttributeModifier(baseAttackSpeed, -4.0 + attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
        set(DataComponentTypes.WEAPON, Weapon.weapon()
            .itemDamagePerAttack(attackDurabilityDamage)
            .disableBlockingForSeconds(disableShieldSeconds))
    }

    fun attackKnockback(knockback: Double) = apply {
        removeAttributeModifiers(Attribute.ATTACK_KNOCKBACK)
        addAttributeModifier(Attribute.ATTACK_KNOCKBACK, AttributeModifier(baseAttackKnockback, knockback, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
    }

    fun durability(durability: Int) = set(DataComponentTypes.MAX_DAMAGE, durability)

    fun useCooldown(cooldownTicks: Int, cooldownGroup: Key?)
            = set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldownTicks / 20.0f).cooldownGroup(cooldownGroup))

    fun build(): ItemStack = stack.clone()

    /**
     * Ignore this method; InvUI item provider implementation.
     */
    override fun get(lang: String?): ItemStack {
        val item = build()
        val split = lang?.split('_')?.toMutableList() ?: return item
        if (split.size > 1) {
            split[1] = split[1].uppercase()
        }
        val locale = LocaleUtils.toLocale(split.joinToString("_"))
        item.translate(locale)
        return item
    }

    companion object {

        val baseArmor = NamespacedKey.minecraft("base_armor")
        val baseArmorToughness = NamespacedKey.minecraft("base_armor_toughness")

        val baseAttackDamage = NamespacedKey.minecraft("base_attack_damage")
        val baseAttackSpeed = NamespacedKey.minecraft("base_attack_speed")
        val baseAttackKnockback = NamespacedKey.minecraft("base_attack_knockback")

        /**
         * The default name language key for a Pylon item.
         */
        @JvmStatic
        fun nameKey(key: NamespacedKey)
                = "pylon.${key.namespace}.item.${key.key}.name"

        /**
         * The default lore language key for a Pylon item.
         */
        @JvmStatic
        fun loreKey(key: NamespacedKey)
                = "pylon.${key.namespace}.item.${key.key}.lore"

        /**
         * Creates a new ItemStackBuilder from [stack]. Any modifications made to the
         * ItemStackBuilder will also be made to [stack].
         */
        @JvmStatic
        fun of(stack: ItemStack): ItemStackBuilder {
            return ItemStackBuilder(stack)
        }

        @JvmStatic
        fun of(material: Material): ItemStackBuilder {
            return of(ItemStack(material))
        }

        @JvmStatic
        fun gui(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
            return ItemStackBuilder(stack)
                .addCustomModelDataString("${GuiItems.pylonGuiItemKeyKey}:$key")
        }

        @JvmStatic
        fun gui(material: Material, key: NamespacedKey): ItemStackBuilder {
            return gui(ItemStack(material), key)
        }

        /**
         * Creates a new [ItemStack] for a [io.github.pylonmc.pylon.core.item.PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         */
        @JvmStatic
        fun pylonItem(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
            return ItemStackBuilder(stack)
                .editPdc { it.set(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY, key) }
                .addCustomModelDataString("${PylonItemSchema.pylonItemKeyKey}:$key")
                .defaultTranslatableName(key)
                .defaultTranslatableLore(key)
        }

        /**
         * Creates a new [ItemStack] for a [io.github.pylonmc.pylon.core.item.PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         */
        @JvmStatic
        fun pylonItem(material: Material, key: NamespacedKey): ItemStackBuilder {
            return pylonItem(ItemStack(material), key)
        }

        fun ItemAttributeModifiers.Builder.copy(modifiers: List<ItemAttributeModifiers.Entry>?) : ItemAttributeModifiers.Builder {
            modifiers?.forEach { entry ->
                this.addModifier(entry.attribute(), entry.modifier(), entry.group, entry.display())
            }
            return this
        }
    }
}
