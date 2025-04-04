package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.pluginInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.Translator
import org.apache.commons.lang3.LocaleUtils
import java.text.MessageFormat
import java.util.Locale
import java.util.WeakHashMap

internal class AddonTranslator(private val addon: PylonAddon) : Translator {

    private val addonNamespace = addon.key.namespace

    private val translations = addon.languages.associateWith {
        val path = "lang/$addonNamespace/$it.yml"
        addon.mergeGlobalConfig(path)
        Config(pluginInstance.dataFolder.resolve(path))
    }

    private val languageRanges = WeakHashMap<Locale, List<Locale.LanguageRange>>()

    override fun translate(key: String, locale: Locale): MessageFormat? {
        if (!key.startsWith("pylon.")) return null
        val (_, addon, key) = key.split('.', limit = 3)
        if (addon != addonNamespace) return null
        val languageRange = languageRanges.getOrPut(locale) {
            val lookupList = LocaleUtils.localeLookupList(locale).reversed()
            lookupList.mapIndexed { index, value ->
                Locale.LanguageRange(value.toString().replace('_', '-'), (index + 1.0) / lookupList.size)
            }
        }
        val matchedLocale = Locale.lookup(languageRange, translations.keys) ?: return null
        val translation = translations[matchedLocale]?.get<String>(key) ?: return null
        return MessageFormat(translation, matchedLocale)
    }

    override fun name(): Key = addon.key
}

