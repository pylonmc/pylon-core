package io.github.pylonmc.rebar.config

import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter
import io.github.pylonmc.rebar.waila.Waila
import net.kyori.adventure.bossbar.BossBar

/**
 * The config options for Rebar.
 */
object RebarConfig {

    private val config = Config(Rebar, "config.yml")

    @JvmField
    val REBAR_GUIDE_ON_FIRST_JOIN = config.getOrThrow("rebar-guide-on-first-join", ConfigAdapter.BOOLEAN)

    @JvmField
    val DEFAULT_TICK_INTERVAL = config.getOrThrow("default-tick-interval", ConfigAdapter.INT)

    @JvmField
    val ALLOWED_BLOCK_ERRORS = config.getOrThrow("allowed-block-errors", ConfigAdapter.INT)

    @JvmField
    val ALLOWED_ENTITY_ERRORS = config.getOrThrow("allowed-entity-errors", ConfigAdapter.INT)

    @JvmField
    val FLUID_TICK_INTERVAL = config.getOrThrow("fluid-tick-interval", ConfigAdapter.INT)

    @JvmField
    val BLOCK_DATA_AUTOSAVE_INTERVAL_SECONDS = config.getOrThrow("block-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmField
    val ENTITY_DATA_AUTOSAVE_INTERVAL_SECONDS = config.getOrThrow("entity-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmField
    val RESEARCHES_ENABLED = config.getOrThrow("research.enabled", ConfigAdapter.BOOLEAN)

    @JvmField
    val RESEARCH_BASE_CONFETTI_AMOUNT = config.get("research.confetti.base-amount", ConfigAdapter.DOUBLE, 70.0)

    @JvmField
    val RESEARCH_MULTIPLIER_CONFETTI_AMOUNT = config.get("research.confetti.multiplier", ConfigAdapter.DOUBLE, 0.2)

    @JvmField
    val RESEARCH_MAX_CONFETTI_AMOUNT = config.get("research.confetti.max-amount", ConfigAdapter.INT, 700)

    @JvmField
    val RESEARCH_SOUNDS = config.getOrThrow("research.sounds", ConfigAdapter.MAP.from(ConfigAdapter.LONG, ConfigAdapter.RANDOMIZED_SOUND))

    @JvmField
    val PIPE_PLACEMENT_TASK_INTERVAL_TICKS = config.getOrThrow("pipe-placement.tick-interval", ConfigAdapter.LONG)

    @JvmField
    val PIPE_PLACEMENT_MAX_LENGTH = config.getOrThrow("pipe-placement.max-length", ConfigAdapter.LONG)

    @JvmField
    val PIPE_PLACEMENT_CANCEL_DISTANCE = config.getOrThrow("pipe-placement.cancel-distance", ConfigAdapter.INT)

    @JvmField
    val TRANSLATION_WRAP_LIMIT = config.getOrThrow("translation-wrap-limit", ConfigAdapter.INT)

    @JvmField
    val METRICS_SAVE_INTERVAL_TICKS = config.getOrThrow("metrics-save-interval-ticks", ConfigAdapter.LONG)

    @JvmField
    val DISABLED_ITEMS = config.getOrThrow("disabled-items", ConfigAdapter.SET.from(ConfigAdapter.NAMESPACED_KEY))

    @JvmField
    val INVENTORY_TICKER_BASE_RATE = config.getOrThrow("inventory-ticker-base-rate", ConfigAdapter.LONG)

    @JvmField
    val CARGO_TICK_INTERVAL = config.getOrThrow("cargo-tick-interval", ConfigAdapter.INT)

    @JvmField
    val CARGO_TRANSFER_RATE_MULTIPLIER = config.getOrThrow("cargo-transfer-rate-multiplier", ConfigAdapter.INT)

    object WailaConfig {
        private val config = Config(Rebar, "config.yml")

        @JvmStatic
        val enabled
            get() = TICK_INTERVAL > 0 && ENABLED_TYPES.isNotEmpty()

        @JvmField
        val TICK_INTERVAL = config.getOrThrow("waila.tick-interval", ConfigAdapter.INT)

        @JvmField
        val ENABLED_TYPES = config.getOrThrow("waila.enabled-types", ConfigAdapter.LIST.from(ConfigAdapter.ENUM.from(Waila.Type::class.java)))

        @JvmField
        val DEFAULT_TYPE = config.getOrThrow("waila.default-type", ConfigAdapter.ENUM.from(Waila.Type::class.java)).apply {
            if (!ENABLED_TYPES.contains(this)) {
                throw IllegalStateException("Default Waila type $this is not in the list of enabled types: $ENABLED_TYPES")
            }
        }

        @JvmField
        val ALLOWED_BOSS_BAR_COLORS = config.getOrThrow("waila.bossbar.allowed-colors", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Color::class.java)))

        @JvmField
        val ALLOWED_BOSS_BAR_OVERLAYS = config.getOrThrow("waila.bossbar.allowed-overlays", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Overlay::class.java)))

        @JvmField
        val DEFAULT_DISPLAY = config.getOrThrow("waila.default-display.bossbar", ConfigAdapter.WAILA_DISPLAY).apply {
            if (!ALLOWED_BOSS_BAR_COLORS.contains(color)) {
                throw IllegalStateException("Default bossbar color $color is not in the list of allowed colors: $ALLOWED_BOSS_BAR_COLORS")
            }
            if (!ALLOWED_BOSS_BAR_OVERLAYS.contains(overlay)) {
                throw IllegalStateException("Default bossbar overlay $overlay is not in the list of allowed overlays: $ALLOWED_BOSS_BAR_OVERLAYS")
            }
        }
    }

    object ArmorTextureConfig {

        private val config = Config(Rebar, "config.yml")

        @JvmField
        val ENABLED = config.getOrThrow("custom-armor-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmField
        val FORCED = config.getOrThrow("custom-armor-textures.force", ConfigAdapter.BOOLEAN)

    }

    object BlockTextureConfig {

        private val config = Config(Rebar, "config.yml")

        @JvmField
        val ENABLED = config.getOrThrow("custom-block-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmField
        val DEFAULT = config.getOrThrow("custom-block-textures.default", ConfigAdapter.BOOLEAN)

        @JvmField
        val FORCED = config.getOrThrow("custom-block-textures.force", ConfigAdapter.BOOLEAN)

        @JvmField
        val OCCLUDING_CACHE_REFRESH_INTERVAL = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-interval", ConfigAdapter.INT)

        @JvmField
        val OCCLUDING_CACHE_REFRESH_SHARE = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-share", ConfigAdapter.DOUBLE)

        @JvmField
        val CULLING_PRESETS = config.getOrThrow("custom-block-textures.culling.presets", ConfigAdapter.MAP.from(ConfigAdapter.STRING, ConfigAdapter.CULLING_PRESET))

        @JvmField
        val DEFAULT_CULLING_PRESET = run {
            val key = config.getOrThrow<String>("custom-block-textures.culling.default-preset", ConfigAdapter.STRING)
            CULLING_PRESETS[key] ?: error("No culling preset with id '$key' found")
        }

    }

}