package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder
import io.github.pylonmc.pylon.core.i18n.wrapping.TextWrapper
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.translation.GlobalTranslator
import org.apache.commons.lang3.LocaleUtils
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Consumer

@Suppress("UnstableApiUsage")
open class ItemStackBuilder private constructor(private val stack: ItemStack) : ItemProvider {

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

    fun unset(type: DataComponentType.NonValued) = apply {
        stack.unsetData(type)
    }

    fun reset(type: DataComponentType.NonValued) = apply {
        stack.unsetData(type)
    }

    fun editMeta(consumer: Consumer<in ItemMeta>) = apply {
        stack.editMeta(consumer)
    }

    fun name(name: Component) = set(DataComponentTypes.ITEM_NAME, name)

    fun name(name: String) = name(fromMiniMessage(name))

    fun defaultTranslatableName(key: NamespacedKey) =
        name(Component.translatable("pylon.${key.namespace}.item.${key.key}.name"))

    fun lore(vararg loreToAdd: ComponentLike) = apply {
        val lore = ItemLore.lore()
        stack.getData(DataComponentTypes.LORE)?.let { lore.addLines(it.lines()) }
        lore.addLines(loreToAdd.toList())
        stack.setData(DataComponentTypes.LORE, lore)
    }

    fun lore(vararg lore: String) = lore(*lore.map(::fromMiniMessage).toTypedArray())

    fun defaultTranslatableLore(key: NamespacedKey) =
        lore(Component.translatable("pylon.${key.namespace}.item.${key.key}.lore"))

    fun build(): ItemStack = stack.clone()

    override fun get(lang: String?): ItemStack {
        val item = build()
        val split = lang?.split('_')?.toMutableList() ?: return item
        if (split.size > 1) {
            split[1] = split[1].uppercase()
        }
        val locale = LocaleUtils.toLocale(split.joinToString("_"))
        item.editData(DataComponentTypes.ITEM_NAME) {
            GlobalTranslator.render(it, locale)
        }
        item.editData(DataComponentTypes.LORE) {
            val wrapper = TextWrapper(PylonConfig.translationWrapLimit)
            val newLore = it.lines().flatMap { line ->
                val translated = GlobalTranslator.render(line, locale)
                val encoded = LineWrapEncoder.encode(translated)
                val wrapped = encoded.copy(lines = encoded.lines.flatMap(wrapper::wrap))
                wrapped.toComponentLines()
            }
            ItemLore.lore(newLore)
        }
        return item
    }

    companion object {
        @JvmStatic
        fun of(stack: ItemStack): ItemStackBuilder {
            return ItemStackBuilder(stack)
        }

        @JvmStatic
        fun of(material: Material): ItemStackBuilder {
            return of(ItemStack(material))
        }

        /**
         * Returns an [ItemStackBuilder] with name and lore set to the default translation keys
         */
        @JvmStatic
        fun defaultBuilder(material: Material, key: NamespacedKey): ItemStackBuilder {
            return of(material)
                .defaultTranslatableName(key)
                .defaultTranslatableLore(key)
        }
    }
}
