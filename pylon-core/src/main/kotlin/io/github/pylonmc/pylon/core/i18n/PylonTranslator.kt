package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.event.PylonRegisterEvent
import io.github.pylonmc.pylon.core.event.PylonUnregisterEvent
import io.github.pylonmc.pylon.core.item.builder.customMiniMessage
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.VirtualComponent
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
import java.text.MessageFormat
import java.util.Locale
import java.util.WeakHashMap

class PylonTranslator(private val addon: PylonAddon) : Translator {

    private val addonNamespace = addon.key.namespace

    private val translations = addon.languages.associateWith {
        addon.mergeGlobalConfig("lang/$it.yml", "lang/$addonNamespace/$it.yml")
    }

    private val translationCache = mutableMapOf<Pair<Locale, String>, Component>()

    override fun canTranslate(key: String, locale: Locale): Boolean {
        return getRawTranslation(key, locale, warn = false) != null
    }

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        var translation = getRawTranslation(component.key(), locale, warn = true)
            ?: component.fallback()?.let {
                val translatable = Component.translatable(it)
                val translated = GlobalTranslator.render(translatable, locale)
                if (translated == translatable) Component.text(it) else translated
            }
            ?: return null
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
        return translation.children(translation.children().map { GlobalTranslator.render(it, locale) })
    }

    private fun getRawTranslation(translationKey: String, locale: Locale, warn: Boolean): Component? {
        return translationCache.getOrPut(locale to translationKey) {
            if (!translationKey.startsWith("pylon.")) return null
            val (_, addon, key) = translationKey.split('.', limit = 3)
            if (addon != addonNamespace) return null
            val commonLocale = findCommonLocale(locale)
            val translations = commonLocale?.let(this.translations::get)
            if (translations == null) {
                if (warn) {
                    PylonCore.logger.warning("No translations found for locale '$locale' in addon '$addon'")
                }
                return null
            }
            val translation = translations.get<String>(key)
            if (translation == null) {
                if (warn) {
                    PylonCore.logger.warning("Missing translation for key '$translationKey' in addon '$addon' for locale '$commonLocale'")
                }
                return null
            }
            customMiniMessage.deserialize(translation)
        }
    }

    private fun findCommonLocale(locale: Locale): Locale? {
        val languageRange = languageRanges.getOrPut(locale) {
            val lookupList = LocaleUtils.localeLookupList(locale).reversed()
            lookupList.mapIndexed { index, value ->
                Locale.LanguageRange(value.toString().replace('_', '-'), (index + 1.0) / lookupList.size)
            }
        }
        return Locale.lookup(languageRange, translations.keys)
    }

    override fun name(): Key = addon.key
    override fun translate(key: String, locale: Locale): MessageFormat? = null

    companion object : Listener {
        private val languageRanges = WeakHashMap<Locale, List<Locale.LanguageRange>>()

        private val translators = mutableMapOf<NamespacedKey, PylonTranslator>()

        @get:JvmStatic
        @get:JvmName("getTranslatorForAddon")
        val PylonAddon.translator: PylonTranslator
            get() = translators[this.key] ?: error("Addon does not have a translator; did you forget to call registerWithPylon()?")

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
            NmsAccessor.instance.resendInventory(event.player)
            NmsAccessor.instance.resendRecipeBook(event.player)
        }
    }
}