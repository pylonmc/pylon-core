package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.pluginInstance
import java.nio.file.Path

data class Language(val name: String, val code: String) {

    val directory: Path = pluginInstance.dataPath.resolve("lang").resolve(code)

    companion object {
        @JvmSynthetic
        internal val languages = mutableSetOf<Language>()

        @JvmStatic
        val loadedLanguages: Set<Language> get() = languages
    }
}
