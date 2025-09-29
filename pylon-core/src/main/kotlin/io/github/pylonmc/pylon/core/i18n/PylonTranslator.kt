package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.event.PylonRegisterEvent
import io.github.pylonmc.pylon.core.event.PylonUnregisterEvent
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder
import io.github.pylonmc.pylon.core.item.builder.customMiniMessage
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.mergeGlobalConfig
import io.github.pylonmc.pylon.core.util.withArguments
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import org.apache.commons.lang3.LocaleUtils
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLocaleChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.text.MessageFormat
import java.util.Locale
import java.util.WeakHashMap
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

/**
 * The [Translator] for a given [PylonAddon]. This translator handles the translation of
 * any keys in the form of `pylon.<addon>.<key>`, where `<addon>` is the namespace of the addon
 * and `<key>` is the path to the translation key within the language files for that addon.
 *
 * Use [translator] to obtain an instance.
 */
class PylonTranslator private constructor(private val addon: PylonAddon) : Translator {

    private val addonNamespace = addon.key.namespace

    private val translations: Map<Locale, Config>

    val languages: Set<Locale>
        get() = translations.keys

    private val translationCache = mutableMapOf<Pair<Locale, String>, Component>()
    private val warned = mutableSetOf<Locale>()

    init {
        for (lang in addon.languages) {
            mergeGlobalConfig(addon, "lang/$lang.yml", "lang/$addonNamespace/$lang.yml")
        }
        val langsDir = PylonCore.dataPath.resolve("lang").resolve(addonNamespace)
        translations = if (!langsDir.exists()) {
            emptyMap()
        } else {
            langsDir.listDirectoryEntries("*.yml").associate {
                val split = it.nameWithoutExtension.split('_', limit = 3)
                Locale.of(split.first(), split.getOrNull(1).orEmpty(), split.getOrNull(2).orEmpty()) to Config(it)
            }
        }
    }

    override fun canTranslate(key: String, locale: Locale): Boolean {
        return getRawTranslation(key, locale, warn = false) != null
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        var translation = getRawTranslation(component.key(), locale, warn = true) ?: return null
        for (arg in component.arguments()) {
            val componentArg = arg.asComponent()
            if (componentArg !is VirtualComponent) continue
            val argument = componentArg.renderer()
            if (argument !is PylonArgument) continue
            val replacer = TextReplacementConfig.builder()
                .match("%${argument.name}%")
                .replacement(GlobalTranslator.render(argument.value.asComponent(), locale))
                .build()
            translation = translation.replaceText(replacer)
        }
        return translation
            .children(translation.children().map { GlobalTranslator.render(it, locale) })
            .style(translation.style().merge(component.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET))
    }

    private fun getRawTranslation(translationKey: String, locale: Locale, warn: Boolean): Component? {
        return translationCache.getOrPut(locale to translationKey) {
            if (!translationKey.startsWith("pylon.")) return null
            val (_, addon, key) = translationKey.split('.', limit = 3)
            if (addon != addonNamespace) return null
            val translations = findCommonLocale(locale)?.let(this.translations::get)
            if (translations == null) {
                if (warn && locale !in warned) {
                    this.addon.javaPlugin.logger.warning("No translations found for locale '$locale'")
                    warned.add(locale)
                }
                return Component.text("Language '$locale' not supported")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, true)
            }
            val translation = translations.get(key, ConfigAdapter.STRING) ?: return null
            customMiniMessage.deserialize(translation)
        }
    }

    private fun findCommonLocale(locale: Locale): Locale? {
        val languageRange = languageRanges.getOrPut(locale) {
            val lookupList = LocaleUtils.localeLookupList(locale).reversed()
            lookupList
                .mapIndexed { index, value ->
                    Locale.LanguageRange(value.toString().replace('_', '-'), (index + 1.0) / lookupList.size)
                }
                .sortedByDescending { it.weight }
        }
        return Locale.lookup(languageRange, translations.keys)
    }

    override fun name(): Key = addon.key
    override fun translate(key: String, locale: Locale): MessageFormat? = null

    companion object : Listener {
        private val languageRanges = WeakHashMap<Locale, List<Locale.LanguageRange>>()

        private val translators = mutableMapOf<NamespacedKey, PylonTranslator>()

        @JvmStatic
        fun wrapText(text: String, limit: Int): List<String> {
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = StringBuilder()

            for (word in words) {
                if (currentLine.length + word.length + 1 > limit) {
                    currentLine.append(' ')
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
            return lines
        }

        @get:JvmStatic
        @get:JvmName("getTranslatorForAddon")
        val PylonAddon.translator: PylonTranslator
            get() = translators[this.key]
                ?: error("Addon does not have a translator; did you forget to call registerWithPylon()?")

        /**
         * Modifies the [ItemStack] to translate its name and lore into the specified [locale].
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("translateItem")
        @Suppress("UnstableApiUsage")
        fun ItemStack.translate(locale: Locale, arguments: List<PylonArgument> = emptyList()) {
            fun isPylon(component: Component): Boolean {
                if (component is TranslatableComponent) {
                    return component.key().startsWith("pylon.")
                }
                return component.children().any(::isPylon)
            }

            editData(DataComponentTypes.ITEM_NAME) {
                if (!isPylon(it)) return@editData it
                val translated = GlobalTranslator.render(it.withArguments(arguments), locale)
                if (translated is TranslatableComponent && translated.fallback() != null) {
                    Component.text(translated.fallback()!!)
                } else {
                    translated
                }
            }
            editData(DataComponentTypes.LORE) { lore ->
                val newLore = lore.lines().flatMap { line ->
                    if (!isPylon(line)) return@flatMap listOf(line)
                    val translated = GlobalTranslator.render(line.withArguments(arguments), locale)
                    val encoded = LineWrapEncoder.encode(translated)
                    val wrapped = encoded.copy(
                        lines = encoded.lines.flatMap { wrapText(it, PylonConfig.translationWrapLimit) }
                    )
                    wrapped.toComponentLines().map {
                        Component.text()
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.GRAY)
                            .append(it)
                            .build()
                    }
                }
                ItemLore.lore(newLore)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onAddonRegister(event: PylonRegisterEvent) {
            if (event.registry != PylonRegistry.ADDONS) return
            val addon = event.value as? PylonAddon ?: return
            val translator = PylonTranslator(addon)
            GlobalTranslator.translator().addSource(translator)
            translators[addon.key] = translator
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onAddonUnregister(event: PylonUnregisterEvent) {
            if (event.registry != PylonRegistry.ADDONS) return
            val addon = event.value as? PylonAddon ?: return
            translators.remove(addon.key)?.let(GlobalTranslator.translator()::removeSource)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player
            NmsAccessor.instance.registerTranslationHandler(player, PlayerTranslationHandler(player))
            // Since the recipe book is initially sent before the event, and therefore before
            // we can register the translation handler, we need to resend it here so that it
            // gets translated properly.
            NmsAccessor.instance.resendRecipeBook(player)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            NmsAccessor.instance.unregisterTranslationHandler(event.player)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerChangeLanguage(event: PlayerLocaleChangeEvent) {
            if (!event.player.isOnline) return
            NmsAccessor.instance.resendInventory(event.player)
            NmsAccessor.instance.resendRecipeBook(event.player)
        }
    }
}