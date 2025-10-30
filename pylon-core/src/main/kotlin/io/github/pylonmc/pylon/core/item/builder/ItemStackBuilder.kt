package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.editDataOrDefault
import io.github.pylonmc.pylon.core.util.editDataOrSet
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pickaxeMineable
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.TooltipDisplay
import io.papermc.paper.datacomponent.item.Tool
import io.papermc.paper.datacomponent.item.UseCooldown
import io.papermc.paper.datacomponent.item.Weapon
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import io.papermc.paper.registry.set.RegistryKeySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.util.TriState
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
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Consumer
import kotlin.collections.forEach

/**
 * Helper class for creating an [ItemStack] with various properties. Includes
 * methods for creating [PylonItem] stacks, and gui items.
 *
 * Implements InvUI's [ItemProvider], so can be used instead of an [ItemStack] in GUIs.
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

    fun <T : Any> get(type: DataComponentType.Valued<T>) = stack.getData(type)

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
     * Use [pylon] instead of this to create a stack for a [PylonItem].
     */
    fun defaultTranslatableName(key: NamespacedKey) =
        name(Component.translatable(nameKey(key)))

    /**
     * In order to get around potions and player heads overriding `item_name`, Pylon by default
     * does unspeakable things to the item to get it to work. This method disables those
     * hacks, which may have unintended side effects.
     */
    fun disableNameHacks() = editPdc { pdc -> pdc.set(disableNameHacksKey, PylonSerializers.BOOLEAN, true) }

    fun lore(loreToAdd: List<ComponentLike>) = apply {
        val lore = ItemLore.lore()
        stack.getData(DataComponentTypes.LORE)?.let { lore.addLines(it.lines()) }
        lore.addLines(loreToAdd)
        stack.setData(DataComponentTypes.LORE, lore)
    }

    fun lore(vararg loreToAdd: ComponentLike) = lore(loreToAdd.toList())

    fun lore(vararg lore: String) = lore(*lore.map(::fromMiniMessage).toTypedArray())

    fun clearLore() = apply {
        stack.setData(DataComponentTypes.LORE, ItemLore.lore())
    }

    fun hideFromTooltip(componentType: DataComponentType) = apply {
        val tooltipDisplay = stack.getData(DataComponentTypes.TOOLTIP_DISPLAY)
        val hidden = tooltipDisplay?.hiddenComponents()?.toMutableSet() ?: mutableSetOf()
        hidden.add(componentType)
        stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
            .hideTooltip(tooltipDisplay?.hideTooltip() == true)
            .hiddenComponents(hidden))
    }

    /**
     * Sets the item's lore to the default language file key (for example
     * `pylon.pyloncore.item.my_dumb_item.lore`), based on the item [key] given.
     *
     * Use [pylon] instead of this to create a stack for a [PylonItem].
     */
    fun defaultTranslatableLore(key: NamespacedKey) =
        lore(Component.translatable(loreKey(key), ""))

    fun editCustomModelData(editFunction: Consumer<CustomModelData.Builder>) = apply {
        val customModelData = stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
        val newCustomModelData = CustomModelData.customModelData()

        if (customModelData != null) {
            newCustomModelData.addFlags(customModelData.flags())
            newCustomModelData.addStrings(customModelData.strings())
            newCustomModelData.addFloats(customModelData.floats())
            newCustomModelData.addColors(customModelData.colors())
        }

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
    fun addCustomModelDataFloat(float: Float) =
        editCustomModelData { it.addFloat(float) }

    /**
     * Adds a boolean to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataFlag(boolean: Boolean) =
        editCustomModelData { it.addFlag(boolean) }

    /**
     * Adds a color to the item's custom model data, used in resource packs.
     */
    fun addCustomModelDataColor(color: Color) =
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

    fun chestplate(
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

    fun durability(durability: Int) = durability(durability, 0)

    fun durability(durability: Int, damage: Int) = apply {
        set(DataComponentTypes.MAX_DAMAGE, durability)
        set(DataComponentTypes.DAMAGE, damage)
    }

    fun useCooldown(cooldownTicks: Int, cooldownGroup: Key?)
            = set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldownTicks / 20.0f).cooldownGroup(cooldownGroup))

    public override fun clone(): ItemStackBuilder {
        return of(stack.clone())
    }

    fun build(): ItemStack = stack.clone()

    /**
     * Ignore this method; InvUI item provider implementation.
     */
    @ApiStatus.Internal
    override fun get(lang: String?) = build()

    companion object {

        val baseArmor = NamespacedKey.minecraft("base_armor")
        val baseArmorToughness = NamespacedKey.minecraft("base_armor_toughness")

        val baseAttackDamage = NamespacedKey.minecraft("base_attack_damage")
        val baseAttackSpeed = NamespacedKey.minecraft("base_attack_speed")
        val baseAttackKnockback = NamespacedKey.minecraft("base_attack_knockback")

        val disableNameHacksKey = pylonKey("disable_name_hacks")

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
        fun of(item: Item): ItemStackBuilder {
            return of(ItemStack(item.itemProvider.get()))
        }

        /**
         * Creates a new [ItemStack] for a GUI item, sets its pdc key and adds
         * a custom model data string for resource packs.
         */
        @JvmStatic
        fun gui(stack: ItemStack, key: String): ItemStackBuilder {
            return ItemStackBuilder(stack)
                .editPdc { it.set(GuiItems.pylonGuiItemKeyKey, PylonSerializers.STRING, key) }
                .addCustomModelDataString(key.toString())
        }

        /**
         * Creates a new [ItemStack] for a GUI item, sets its pdc key and adds
         * a custom model data string for resource packs.
         */
        @JvmStatic
        fun gui(material: Material, key: String): ItemStackBuilder {
            return gui(ItemStack(material), key)
        }

        /**
         * Creates a new [ItemStack] for a GUI item, sets its pdc key and adds
         * a custom model data string for resource packs.
         */
        @JvmStatic
        fun gui(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
            return gui(stack, key.toString())
        }

        /**
         * Creates a new [ItemStack] for a GUI item, sets its pdc key and adds
         * a custom model data string for resource packs.
         */
        @JvmStatic
        fun gui(material: Material, key: NamespacedKey): ItemStackBuilder {
            return gui(ItemStack(material), key)
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         */
        @JvmStatic
        fun pylon(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
            return ItemStackBuilder(stack)
                .editPdc { it.set(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY, key) }
                .addCustomModelDataString(key.toString())
                .defaultTranslatableName(key)
                .defaultTranslatableLore(key)
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         */
        @JvmStatic
        fun pylon(material: Material, key: NamespacedKey): ItemStackBuilder {
            return pylon(ItemStack(material), key)
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         *
         * The provided [consumer] is called with the created [ItemStackBuilder] and
         * the [Settings][Settings.get] for the item, allowing you to further customize the item
         * based on its config.
         */
        @JvmStatic
        fun pylon(stack: ItemStack, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Any): ItemStackBuilder {
            val builder = pylon(stack, key)
            val settings = Settings.get(key)
            consumer(builder, settings)
            return builder
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         *
         * The provided [consumer] is called with the created [ItemStackBuilder] and
         * the [Settings][Settings.get] for the item, allowing you to further customize the item
         * based on its config.
         */
        @JvmStatic
        fun pylon(material: Material, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Any): ItemStackBuilder {
            return pylon(ItemStack(material), key, consumer)
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [helmet][EquipmentSlotGroup.HEAD] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-helmet.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.5 # double
         * durability: 250 # integer
         * ```
         */
        @JvmStatic
        fun pylonHelmet(stack: ItemStack, key: NamespacedKey, hasDurability: Boolean) = pylonArmor(stack, key, EquipmentSlotGroup.HEAD, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [helmet][EquipmentSlotGroup.HEAD] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-helmet.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.5 # double
         * durability: 250 # integer
         * ```
         */
        @JvmStatic
        fun pylonHelmet(material: Material, key: NamespacedKey, hasDurability: Boolean) = pylonHelmet(ItemStack(material), key, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [chestplate][EquipmentSlotGroup.CHEST] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-chestplate.yml`
         * ```yml
         * armor: 6.0 # double
         * armor-toughness: 1.0 # double
         * durability: 400 # integer
         * ```
         */
        @JvmStatic
        fun pylonChestplate(stack: ItemStack, key: NamespacedKey, hasDurability: Boolean) = pylonArmor(stack, key, EquipmentSlotGroup.CHEST, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [chestplate][EquipmentSlotGroup.CHEST] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-chestplate.yml`
         * ```yml
         * armor: 6.0 # double
         * armor-toughness: 1.0 # double
         * durability: 400 # integer
         * ```
         */
        @JvmStatic
        fun pylonChestplate(material: Material, key: NamespacedKey, hasDurability: Boolean) = pylonChestplate(ItemStack(material), key, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [leggings][EquipmentSlotGroup.LEGS] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-leggings.yml`
         * ```yml
         * armor: 5.0 # double
         * armor-toughness: 0.5 # double
         * durability: 350 # integer
         * ```
         */
        @JvmStatic
        fun pylonLeggings(stack: ItemStack, key: NamespacedKey, hasDurability: Boolean) = pylonArmor(stack, key, EquipmentSlotGroup.LEGS, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [leggings][EquipmentSlotGroup.LEGS] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-leggings.yml`
         * ```yml
         * armor: 5.0 # double
         * armor-toughness: 0.5 # double
         * durability: 350 # integer
         * ```
         */
        @JvmStatic
        fun pylonLeggings(material: Material, key: NamespacedKey, hasDurability: Boolean) = pylonLeggings(ItemStack(material), key, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [boots][EquipmentSlotGroup.FEET] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-boots.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.0 # double
         * durability: 300 # integer
         * ```
         */
        @JvmStatic
        fun pylonBoots(stack: ItemStack, key: NamespacedKey, hasDurability: Boolean) = pylonArmor(stack, key, EquipmentSlotGroup.FEET, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [boots][EquipmentSlotGroup.FEET] slot.
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-boots.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.0 # double
         * durability: 300 # integer
         * ```
         */
        @JvmStatic
        fun pylonBoots(material: Material, key: NamespacedKey, hasDurability: Boolean) = pylonBoots(ItemStack(material), key, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the specified [slot].
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-armor.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.5 # double
         * durability: 250 # integer
         * ```
         */
        @JvmStatic
        fun pylonArmor(stack: ItemStack, key: NamespacedKey, slot: EquipmentSlotGroup, hasDurability: Boolean) = pylon(stack, key) { builder, settings ->
            builder.armor(
                slot,
                settings.getOrThrow("armor", ConfigAdapter.DOUBLE),
                settings.getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
            )

            if (hasDurability) {
                builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
            } else {
                builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
            }
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be worn in the specified [slot].
         * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-armor.yml`
         * ```yml
         * armor: 2.0 # double
         * armor-toughness: 0.5 # double
         * durability: 250 # integer
         * ```
         */
        @JvmStatic
        fun pylonArmor(material: Material, key: NamespacedKey, slot: EquipmentSlotGroup, hasDurability: Boolean)
                = pylonArmor(ItemStack(material), key, slot, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a tool for the [mineable] blocks. (See [pickaxeMineable]
         * and related for basic tools) You must provide a value for `mining-speed` and `mining-durability-damage` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-tool.yml`
         * ```yml
         * mining-speed: 8.0 # double
         * mining-durability-damage: 1 # integer
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonTool(stack: ItemStack, key: NamespacedKey, mineable: RegistryKeySet<BlockType>, hasDurability: Boolean) = pylon(stack, key) { builder, settings ->
            builder.tool(
                mineable,
                settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
                settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
            )

            if (hasDurability) {
                builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
            } else {
                builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
            }
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a tool for the [mineable] blocks. (See [pickaxeMineable]
         * and related for basic tools) You must provide a value for `mining-speed` and `mining-durability-damage` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * `settings/example-tool.yml`
         * ```yml
         * mining-speed: 8.0 # double
         * mining-durability-damage: 1 # integer
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonTool(material: Material, key: NamespacedKey, mineable: RegistryKeySet<BlockType>, hasDurability: Boolean)
                = pylonTool(ItemStack(material), key, mineable, hasDurability)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon.
         * You must provide a value for `attack-damage`, `attack-speed` and `attack-durability-damage` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * If [hasKnockback] is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
         * Otherwise, removes any existing attack knockback.
         *
         * If `disablesShield` is true, gives the item a shield disable time defined by `disable-shield-seconds` in the [Settings][Settings.get].
         * If false, the item will not disable shields when used in attacking.
         *
         * `settings/example-weapon.yml`
         * ```yml
         * attack-damage: 6.0 # double
         * attack-speed: 1.6 # double
         * attack-durability-damage: 2 # integer
         * attack-knockback: 0.4 # double
         * disable-shield-seconds: 3.0 # float
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonWeapon(stack: ItemStack, key: NamespacedKey, hasDurability: Boolean, hasKnockback: Boolean, disablesShield: Boolean) = pylon(stack, key) { builder, settings ->
            builder.weapon(
                settings.getOrThrow("attack-damage", ConfigAdapter.DOUBLE),
                settings.getOrThrow("attack-speed", ConfigAdapter.DOUBLE),
                settings.getOrThrow("attack-durability-damage", ConfigAdapter.INT),
                if (disablesShield) settings.getOrThrow("disable-shield-seconds", ConfigAdapter.FLOAT) else 0f
            )

            if (hasKnockback) {
                builder.attackKnockback(settings.getOrThrow("attack-knockback", ConfigAdapter.DOUBLE))
            } else {
                builder.removeAttributeModifiers(Attribute.ATTACK_KNOCKBACK)
            }

            if (hasDurability) {
                builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
            } else {
                builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
            }
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon.
         * You must provide a value for `attack-damage`, `attack-speed` and `attack-durability-damage` in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * If [hasKnockback] is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
         * Otherwise, removes any existing attack knockback.
         *
         * If `disablesShield` is true, gives the item a shield disable time defined by `disable-shield-seconds` in the [Settings][Settings.get].
         * If false, the item will not disable shields when used in attacking.
         *
         * `settings/example-weapon.yml`
         * ```yml
         * attack-damage: 6.0 # double
         * attack-speed: 1.6 # double
         * attack-durability-damage: 2 # integer
         * attack-knockback: 0.4 # double
         * disable-shield-seconds: 3.0 # float
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonWeapon(material: Material, key: NamespacedKey, hasDurability: Boolean, hasKnockback: Boolean, disablesShield: Boolean)
                = pylonWeapon(ItemStack(material), key, hasDurability, hasKnockback, disablesShield)

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon and tool for the [mineable] blocks. (See [pickaxeMineable] and related for basic tools)
         * You must provide a value for `attack-damage`, `attack-speed`, `attack-durability-damage`, `mining-speed` and `mining-durability-damage`
         * in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * If [hasDurability] is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
         * Otherwise, removes any existing attack knockback.
         *
         * If [hasKnockback] is true, gives the item a shield disable time defined by `disable-shield-seconds` in the [Settings][Settings.get].
         * If false, the item will not disable shields when used in attacking.
         *
         * `settings/example-tool-weapon.yml`
         * ```yml
         * attack-damage: 6.0 # double
         * attack-speed: 1.6 # double
         * attack-durability-damage: 2 # integer
         * attack-knockback: 0.4 # double
         * disable-shield-seconds: 1.0 # float
         *
         * mining-speed: 8.0 # double
         * mining-durability-damage: 1 # integer
         *
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonToolWeapon(stack: ItemStack, key: NamespacedKey, mineable: RegistryKeySet<BlockType>, hasDurability: Boolean, hasKnockback: Boolean, disablesShield: Boolean): ItemStackBuilder {
            val settings = Settings.get(key)
            return pylonWeapon(stack, key, hasDurability, hasKnockback, disablesShield).tool(
                mineable,
                settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
                settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
            )
        }

        /**
         * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon and tool for the [mineable] blocks. (See [pickaxeMineable] and related for basic tools)
         * You must provide a value for `attack-damage`, `attack-speed`, `attack-durability-damage`, `mining-speed` and `mining-durability-damage`
         * in the [Settings][Settings.get] for the item.
         *
         * If [hasDurability] is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
         * Otherwise, removes any existing durability.
         *
         * If [hasKnockback] is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
         * Otherwise, removes any existing attack knockback.
         *
         * If `disablesShield` is true, gives the item a shield disable time defined by `disable-shield-seconds` in the [Settings][Settings.get].
         * If false, the item will not disable shields when used in attacking.
         *
         * `settings/example-tool-weapon.yml`
         * ```yml
         * attack-damage: 6.0 # double
         * attack-speed: 1.6 # double
         * attack-durability-damage: 2 # integer
         * attack-knockback: 0.4 # double
         * disable-shield-seconds: 1.0 # float
         *
         * mining-speed: 8.0 # double
         * mining-durability-damage: 1 # integer
         *
         * durability: 500 # integer
         * ```
         */
        @JvmStatic
        fun pylonToolWeapon(material: Material, key: NamespacedKey, mineable: RegistryKeySet<BlockType>, hasDurability: Boolean, hasKnockback: Boolean, disablesShield: Boolean)
                = pylonToolWeapon(ItemStack(material), key, mineable, hasDurability, hasKnockback, disablesShield)

        fun ItemAttributeModifiers.Builder.copy(modifiers: List<ItemAttributeModifiers.Entry>?) : ItemAttributeModifiers.Builder {
            modifiers?.forEach { entry ->
                this.addModifier(entry.attribute(), entry.modifier(), entry.group, entry.display())
            }
            return this
        }
    }
}
