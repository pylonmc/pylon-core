package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.ToolType
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

/**
 * Utility class for creating [ItemStack]s for [PylonItem]s backed by
 * [ItemStackBuilder]s.
 */
object PylonItemStackBuilder {

    /**
     * Creates a new [ItemStack] for a [PylonItem] by setting
     * the name and lore to the default translation keys, and setting the item's Pylon ID to the
     * provided [key].
     */
    @JvmStatic
    fun of(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
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
    fun of(material: Material, key: NamespacedKey): ItemStackBuilder {
        return of(ItemStack(material), key)
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
    fun of(stack: ItemStack, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Any): ItemStackBuilder {
        val builder = of(stack, key)
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
    fun of(material: Material, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Any): ItemStackBuilder {
        return of(ItemStack(material), key, consumer)
    }

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [helmet][EquipmentSlotGroup.HEAD] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun helmet(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.HEAD, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [helmet][EquipmentSlotGroup.HEAD] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun helmet(material: Material, key: NamespacedKey, durability: Boolean) = helmet(ItemStack(material), key, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [chestplate][EquipmentSlotGroup.CHEST] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun chestPlate(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.CHEST, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [chestplate][EquipmentSlotGroup.CHEST] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun chestPlate(material: Material, key: NamespacedKey, durability: Boolean) = chestPlate(ItemStack(material), key, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [leggings][EquipmentSlotGroup.LEGS] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun leggings(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.LEGS, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [leggings][EquipmentSlotGroup.LEGS] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun leggings(material: Material, key: NamespacedKey, durability: Boolean) = leggings(ItemStack(material), key, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [boots][EquipmentSlotGroup.FEET] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun boots(stack: ItemStack, key: NamespacedKey, durability: Boolean) = armor(stack, key, EquipmentSlotGroup.FEET, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the [boots][EquipmentSlotGroup.FEET] slot.
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun boots(material: Material, key: NamespacedKey, durability: Boolean) = boots(ItemStack(material), key, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the specified [slot].
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun armor(stack: ItemStack, key: NamespacedKey, slot: EquipmentSlotGroup, durability: Boolean) = create(stack, key) { builder, settings ->
        builder.armor(
            slot,
            settings.getOrThrow("armor", ConfigAdapter.DOUBLE),
            settings.getOrThrow("armor-toughness", ConfigAdapter.DOUBLE)
        )

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        } else {
            builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
        }
    }

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be worn in the specified [slot].
     * You must provide a value for `armor` and `armor-toughness` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun armor(material: Material, key: NamespacedKey, slot: EquipmentSlotGroup, durability: Boolean)
        = armor(ItemStack(material), key, slot, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a tool of the specified [toolType].
     * You must provide a value for `mining-speed` and `mining-durability-damage` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun tool(stack: ItemStack, key: NamespacedKey, toolType: ToolType, durability: Boolean) = create(stack, key) { builder, settings ->
        builder.tool(
            toolType.getTag(),
            settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
            settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
        )

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        } else {
            builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
        }
    }

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a tool of the specified [toolType].
     * You must provide a value for `mining-speed` and `mining-durability-damage` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
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
    fun tool(material: Material, key: NamespacedKey, toolType: ToolType, durability: Boolean)
        = tool(ItemStack(material), key, toolType, durability)

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon.
     * You must provide a value for `attack-damage`, `attack-speed` and `attack-durability-damage` in the [Settings][Settings.get] for the item.
     * 
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
     * Otherwise, removes any existing durability.
     * 
     * If `knockback` is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
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
    fun weapon(stack: ItemStack, key: NamespacedKey, durability: Boolean, knockback: Boolean, disablesShield: Boolean) = create(stack, key) { builder, settings ->
        builder.weapon(
            settings.getOrThrow("attack-damage", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-speed", ConfigAdapter.DOUBLE),
            settings.getOrThrow("attack-durability-damage", ConfigAdapter.INT),
            if (disablesShield) settings.getOrThrow("disable-shield-seconds", ConfigAdapter.FLOAT) else 0f
        )

        if (knockback) {
            builder.attackKnockback(settings.getOrThrow("attack-knockback", ConfigAdapter.DOUBLE))
        } else {
            builder.removeAttributeModifiers(Attribute.ATTACK_KNOCKBACK)
        }

        if (durability) {
            builder.durability(settings.getOrThrow("durability", ConfigAdapter.INT))
        } else {
            builder.unset(DataComponentTypes.DAMAGE).unset(DataComponentTypes.MAX_DAMAGE)
        }
    }

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon.
     * You must provide a value for `attack-damage`, `attack-speed` and `attack-durability-damage` in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
     * Otherwise, removes any existing durability.
     *
     * If `knockback` is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
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
    fun weapon(material: Material, key: NamespacedKey, durability: Boolean, knockback: Boolean, disablesShield: Boolean)
            = weapon(ItemStack(material), key, durability, knockback, disablesShield)
    
    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon and tool of the specified [toolType].
     * You must provide a value for `attack-damage`, `attack-speed`, `attack-durability-damage`, `mining-speed` and `mining-durability-damage`
     * in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
     * Otherwise, removes any existing durability.
     *
     * If `knockback` is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
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
    fun toolWeapon(stack: ItemStack, key: NamespacedKey, toolType: ToolType, durability: Boolean, knockback: Boolean, disablesShield: Boolean): ItemStackBuilder {
        val settings = Settings.get(key)
        return weapon(stack, key, durability, knockback, disablesShield).tool(
            toolType.getTag(),
            settings.getOrThrow("mining-speed", ConfigAdapter.FLOAT),
            settings.getOrThrow("mining-durability-damage", ConfigAdapter.INT)
        )
    }

    /**
     * Creates a new [ItemStack] for a [PylonItem] that can be used as a weapon and tool of the specified [toolType].
     * You must provide a value for `attack-damage`, `attack-speed`, `attack-durability-damage`, `mining-speed` and `mining-durability-damage`
     * in the [Settings][Settings.get] for the item.
     *
     * If `durability` is true, gives the item a max durability defined by `durability` in the [Settings][Settings.get].
     * Otherwise, removes any existing durability.
     *
     * If `knockback` is true, gives the item knockback defined by `attack-knockback` in the [Settings][Settings.get].
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
    fun toolWeapon(material: Material, key: NamespacedKey, toolType: ToolType, durability: Boolean, knockback: Boolean, disablesShield: Boolean)
        = toolWeapon(ItemStack(material), key, toolType, durability, knockback, disablesShield)

    private fun create(stack: ItemStack, key: NamespacedKey, consumer: (ItemStackBuilder, Config) -> Unit): ItemStackBuilder {
        val builder = of(stack, key)
        val settings = Settings.get(key)
        consumer(builder, settings)
        return builder
    }
}