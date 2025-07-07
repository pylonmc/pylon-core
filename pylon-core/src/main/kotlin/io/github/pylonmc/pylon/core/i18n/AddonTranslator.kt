package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.item.builder.customMiniMessage
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import org.apache.commons.lang3.LocaleUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLocaleChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.text.MessageFormat
import java.util.Locale
import java.util.WeakHashMap

class AddonTranslator(private val addon: PylonAddon) : Translator {

    private val addonNamespace = addon.key.namespace

    private val translations = addon.languages.associateWith {
        addon.mergeGlobalConfig("lang/$it.yml", "lang/$addonNamespace/$it.yml")
    }

    private val translationCache = mutableMapOf<Pair<Locale, String>, Component>()

    private val languageRanges = WeakHashMap<Locale, List<Locale.LanguageRange>>()

    override fun name(): Key = addon.key

    override fun translate(key: String, locale: Locale): MessageFormat? = null

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val fallback = component.fallback()
        var translated = getTranslation(component, locale)
            ?: fallback?.let {
                val translatable = Component.translatable(it)
                val translated = GlobalTranslator.render(translatable, locale)
                if (translated == translatable) Component.text(it) else translated
            }
            ?: return null
        for (arg in component.arguments()) {
            val component = arg.asComponent()
            if (component !is VirtualComponent) continue
            val argument = component.renderer() as? PylonArgument ?: continue
            val replacer = TextReplacementConfig.builder()
                .match("%${argument.name}%")
                .replacement(GlobalTranslator.render(argument.value.asComponent(), locale))
                .build()
            translated = translated.replaceText(replacer)
        }
        translated = translated.children(translated.children().map { GlobalTranslator.render(it, locale) })
        return translated
    }

    private fun getTranslation(translationKey: String, locale: Locale): Component? {
        return translationCache.getOrPut(locale to translationKey) {
            if (!translationKey.startsWith("pylon.")) return null
            val (_, addon, key) = translationKey.split('.', limit = 3)
            if (addon != addonNamespace) return null
            val languageRange = languageRanges.getOrPut(locale) {
                val lookupList = LocaleUtils.localeLookupList(locale).reversed()
                lookupList.mapIndexed { index, value ->
                    Locale.LanguageRange(value.toString().replace('_', '-'), (index + 1.0) / lookupList.size)
                }
            }
            val matchedLocale = Locale.lookup(languageRange, translations.keys) ?: return null
            val translation = translations[matchedLocale]?.get<String>(key) ?: return null
            customMiniMessage.deserialize(translation)
        }
    }

    private fun getTranslation(component: TranslatableComponent, locale: Locale): Component? {
        val translated = getTranslation(component.key(), locale) ?: return null
        return Component.text().style(component.style()).append(translated).build()
    }

    fun translationKeyExists(key: String, locale: Locale): Boolean
            = getTranslation(key, locale) != null

    companion object : Listener {

        val translators = mutableMapOf<PylonAddon, AddonTranslator>()

        @JvmSynthetic
        internal fun register(addon: PylonAddon) {
            val translator = AddonTranslator(addon)
            GlobalTranslator.translator().addSource(translator)
            translators[addon] = translator
        }

        @JvmSynthetic
        internal fun unregister(addon: PylonAddon) {
            translators.remove(addon)?.let(GlobalTranslator.translator()::removeSource)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player
            NmsAccessor.instance.registerTranslationHandler(player, PlayerTranslationHandler(player))
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            NmsAccessor.instance.unregisterTranslationHandler(event.player)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerChangeLanguage(event: PlayerLocaleChangeEvent) {
            NmsAccessor.instance.resendInventory(event.player)
        }
    }
}

