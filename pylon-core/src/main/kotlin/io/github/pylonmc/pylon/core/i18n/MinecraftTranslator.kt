package io.github.pylonmc.pylon.core.i18n

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.Translator
import org.bukkit.Bukkit
import java.net.URI
import java.nio.file.FileSystems
import java.text.MessageFormat
import java.util.Locale
import kotlin.io.path.*

internal object MinecraftTranslator : Translator {

    private const val LAUNCHER_META_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"
    private val LANG_REGEX = """^minecraft/lang/(.+)\.json$""".toRegex()

    private val cacheDir = Path("cache/pylon/mclang")
    private val translations = mutableMapOf<Locale, Map<String, MessageFormat>>()

    init {
        PylonCore.logger.info("Loading vanilla translations, this may take a while...")
        cacheDir.createDirectories()

        val launcherMeta = readJson(LAUNCHER_META_URL)
        val minecraftVersion = Bukkit.getMinecraftVersion()

        val versionUrl = launcherMeta.getAsJsonArray("versions")
            .map { it.asJsonObject }
            .first { it.get("id").asString == minecraftVersion }
            .get("url").asString
        val versionMeta = readJson(versionUrl)

        val assetIndexUrl = versionMeta.getAsJsonObject("assetIndex").get("url").asString
        val assetIndex = readJson(assetIndexUrl).getAsJsonObject("objects")
        for ((assetName, assetData) in assetIndex.entrySet()) {
            val match = LANG_REGEX.matchEntire(assetName) ?: continue
            val codes = match.groupValues[1].split('_', limit = 2)
            val locale = Locale.of(codes.first(), codes.getOrNull(1) ?: "")
            if (locale in translations) continue
            loadLang(locale, assetData.asJsonObject.get("hash").asString)
        }

        // why does en_us have to be so special?
        val enUsFile = cacheDir.resolve("en_us.json")
        if (!enUsFile.exists()) {
            val clientUrl = versionMeta.getAsJsonObject("downloads")
                .getAsJsonObject("client")
                .get("url").asString
            val clientFile = cacheDir.resolve("client.jar")
            if (!clientFile.exists()) {
                clientFile.outputStream().use { output ->
                    URI(clientUrl).toURL().openStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
            FileSystems.newFileSystem(clientFile).use { jar ->
                enUsFile.outputStream().use { output ->
                    jar.getPath("assets/minecraft/lang/en_us.json").inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
        loadLang(Locale.of("en", "us"), "")

        PylonCore.logger.info("Done loading vanilla translations")
    }

    private fun loadLang(locale: Locale, hash: String) {
        val file = cacheDir.resolve("${locale.toString().lowercase()}.json")
        if (!file.exists()) {
            val url = "https://resources.download.minecraft.net/${hash.take(2)}/$hash"
            file.outputStream().use { output ->
                URI(url).toURL().openStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
        val json = file.inputStream().reader().use(JsonParser::parseReader).asJsonObject
        val messages = mutableMapOf<String, MessageFormat>()
        for ((key, value) in json.entrySet()) {
            messages[key] = MessageFormat(value.asString, locale)
        }
        translations[locale] = messages
    }

    private fun readJson(url: String): JsonObject =
        URI(url).toURL().openStream().reader().use(JsonParser::parseReader).asJsonObject

    override fun translate(key: String, locale: Locale): MessageFormat? = translations[locale]?.get(key)
    override fun name(): Key = pylonKey("minecraft")
}