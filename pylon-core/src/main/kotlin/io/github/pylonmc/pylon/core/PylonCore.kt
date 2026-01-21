@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.base.PylonFallingBlock.PylonFallingBlockEntity
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND_PY_ALIAS
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.fluid.placement.FluidPipePlacementService
import io.github.pylonmc.pylon.core.i18n.PylonTranslator
import io.github.pylonmc.pylon.core.item.PylonInventoryTicker
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.logistics.CargoRoutes
import io.github.pylonmc.pylon.core.metrics.PylonMetrics
import io.github.pylonmc.pylon.core.recipe.ConfigurableRecipeType
import io.github.pylonmc.pylon.core.recipe.PylonRecipeListener
import io.github.pylonmc.pylon.core.recipe.RecipeCompletion
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.resourcepack.armor.ArmorTextureEngine
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine
import io.github.pylonmc.pylon.core.util.mergeGlobalConfig
import io.github.pylonmc.pylon.core.waila.Waila
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.delay
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityIdProvider
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Display
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.ItemDisplay
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.i18n.Languages
import java.util.*
import kotlin.io.path.*

/**
 * The one and only Pylon Core plugin!
 */
object PylonCore : JavaPlugin(), PylonAddon {

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()

        val expectedVersion = pluginMeta.apiVersion
        val actualVersion = ServerBuildInfo.buildInfo().minecraftVersionId()
        if (actualVersion != expectedVersion) {
            logger.severe("!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!!!")
            logger.severe("You are running Pylon on Minecraft version $actualVersion")
            logger.severe("This build of Pylon expects Minecraft version $expectedVersion")
            logger.severe("Pylon may run fine, but you may encounter bugs ranging from mild to catastrophic")
            logger.severe("Please update your Pylon version accordingly")
            logger.severe("Please see https://github.com/pylonmc/pylon-core/releases for available Pylon versions")
            logger.severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        }

        InvUI.getInstance().setPlugin(this)
        Languages.getInstance().enableServerSideTranslations(false) // we do our own

        val packetEvents = PacketEvents.getAPI()
        packetEvents.init()

        val entityLibPlatform = SpigotEntityLibPlatform(this)
        val entityLibSettings = APIConfig(packetEvents).tickTickables()
        EntityLib.init(entityLibPlatform, entityLibSettings)
        entityLibPlatform.entityIdProvider = EntityIdProvider { _, _ ->
            @Suppress("DEPRECATION")
            Bukkit.getUnsafe().nextEntityId()
        }

        saveDefaultConfig()
        // Add any keys that are missing from global config - saveDefaultConfig will not do anything if config already present
        mergeGlobalConfig(PylonCore, "config.yml", "config.yml")

        val pm = Bukkit.getPluginManager()
        pm.registerEvents(PylonTranslator, this)
        pm.registerEvents(PylonAddon, this)

        PylonMetrics // initialize metrics by referencing it

        // Anything that listens for addon registration must be above this line
        registerWithPylon()

        pm.registerEvents(BlockStorage, this)
        pm.registerEvents(BlockListener, this)
        pm.registerEvents(PylonCopperBlock, this)
        pm.registerEvents(PylonItemListener, this)
        Bukkit.getScheduler().runTaskTimer(this, PylonInventoryTicker(), 0, PylonConfig.INVENTORY_TICKER_BASE_RATE)
        pm.registerEvents(MultiblockCache, this)
        pm.registerEvents(EntityStorage, this)
        pm.registerEvents(EntityListener, this)
        pm.registerEvents(Research, this)
        pm.registerEvents(PylonGuiBlock, this)
        pm.registerEvents(PylonEntityHolderBlock, this)
        pm.registerEvents(PylonSimpleMultiblock, this)
        pm.registerEvents(PylonProcessor, this)
        pm.registerEvents(PylonRecipeProcessor, this)
        pm.registerEvents(PylonFluidBufferBlock, this)
        pm.registerEvents(PylonFluidTank, this)
        pm.registerEvents(PylonRecipeListener, this)
        pm.registerEvents(PylonDirectionalBlock, this)
        pm.registerEvents(FluidPipePlacementService, this)
        pm.registerEvents(PylonTickingBlock, this)
        pm.registerEvents(PylonGuide, this)
        pm.registerEvents(PylonLogisticBlock, this)
        pm.registerEvents(PylonCargoBlock, this)
        pm.registerEvents(CargoRoutes, this)
        pm.registerEvents(CargoDuct, this)
        pm.registerEvents(RecipeCompletion, this)

        if (PylonConfig.WailaConfig.enabled) {
            pm.registerEvents(Waila, this)
        }

        if (PylonConfig.ArmorTextureConfig.ENABLED) {
            packetEvents.eventManager.registerListener(ArmorTextureEngine, PacketListenerPriority.HIGHEST)
        }

        if (PylonConfig.BlockTextureConfig.ENABLED) {
            pm.registerEvents(BlockTextureEngine, this)
            BlockTextureEngine.updateOccludingCacheJob.start()
        }

        Bukkit.getScheduler().runTaskTimer(
            this,
            MultiblockCache.MultiblockChecker,
            MultiblockCache.MultiblockChecker.INTERVAL_TICKS,
            MultiblockCache.MultiblockChecker.INTERVAL_TICKS
        )

        addDefaultPermission("pylon.command.guide")
        addDefaultPermission("pylon.command.waila")
        addDefaultPermission("pylon.command.research.list.self")
        addDefaultPermission("pylon.command.research.discover")
        addDefaultPermission("pylon.command.research.points.query.self")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(ROOT_COMMAND)
            it.registrar().register(ROOT_COMMAND_PY_ALIAS)
        }

        PylonItem.register<DebugWaxedWeatheredCutCopperStairs>(DebugWaxedWeatheredCutCopperStairs.STACK)
        PylonGuide.hideItem(DebugWaxedWeatheredCutCopperStairs.KEY)

        PylonItem.register<PhantomBlock.ErrorItem>(PhantomBlock.ErrorItem.STACK)
        PylonGuide.hideItem(PhantomBlock.ErrorItem.KEY)

        PylonItem.register<PylonGuide>(PylonGuide.STACK)
        PylonGuide.hideItem(PylonGuide.KEY)

        PylonEntity.register<Display, PylonSimpleMultiblock.MultiblockGhostBlock>(
            PylonSimpleMultiblock.MultiblockGhostBlock.KEY,
        )

        PylonEntity.register<ItemDisplay, FluidEndpointDisplay>(FluidEndpointDisplay.KEY)
        PylonEntity.register<ItemDisplay, FluidIntersectionDisplay>(FluidIntersectionDisplay.KEY)
        PylonEntity.register<ItemDisplay, FluidPipeDisplay>(FluidPipeDisplay.KEY)

        PylonEntity.register<FallingBlock, PylonFallingBlockEntity>(PylonFallingBlock.KEY)

        PylonBlock.register<FluidSectionMarker>(FluidSectionMarker.KEY, Material.STRUCTURE_VOID)
        PylonBlock.register<FluidIntersectionMarker>(FluidIntersectionMarker.KEY, Material.STRUCTURE_VOID)

        RecipeType.addVanillaRecipes()

        launch {
            delay(1.ticks)
            loadRecipes()
            loadResearches()
        }

        val end = System.currentTimeMillis()
        logger.info("Loaded in ${(end - start) / 1000.0}s")
    }

    private fun loadRecipes() {
        val start = System.currentTimeMillis()

        logger.info("Loading recipes...")
        for (type in PylonRegistry.RECIPE_TYPES) {
            if (type !is ConfigurableRecipeType) continue
            for (addon in PylonRegistry.ADDONS) {
                val configStream = addon.javaPlugin.getResource(type.filePath) ?: continue
                val config = configStream.reader().use { ConfigSection(YamlConfiguration.loadConfiguration(it)) }
                type.loadFromConfig(config)
            }
        }

        val recipesDir = dataPath.resolve("recipes")
        if (recipesDir.exists()) {
            for (recipeDir in recipesDir.listDirectoryEntries()) {
                if (!recipeDir.isDirectory()) continue
                val namespace = recipeDir.nameWithoutExtension
                for (recipe in recipeDir.listDirectoryEntries()) {
                    if (!recipe.isRegularFile() || recipe.extension != "yml") continue
                    val key = NamespacedKey(namespace, recipe.nameWithoutExtension)
                    val type = PylonRegistry.RECIPE_TYPES[key] as? ConfigurableRecipeType ?: continue
                    type.loadFromConfig(Config(recipe))
                }
            }
        }

        val end = System.currentTimeMillis()
        logger.info("Loaded recipes in ${(end - start) / 1000.0}s")
    }

    private fun loadResearches() {
        logger.info("Loading researches...")
        val start = System.currentTimeMillis()

        for (addon in PylonRegistry.ADDONS) {
            mergeGlobalConfig(addon, "researches.yml", "researches/${addon.key.namespace}.yml", false)
        }

        val researchDir = dataPath.resolve("researches")
        if (researchDir.exists()) {
            for (namespaceDir in researchDir.listDirectoryEntries()) {
                val namespace = namespaceDir.nameWithoutExtension

                if (!namespaceDir.isRegularFile()) continue

                val mainResearchConfig = Config(namespaceDir)
                for (key in mainResearchConfig.keys) {
                    val nsKey = NamespacedKey(namespace, key)
                    val section = mainResearchConfig.getSection(key) ?: continue

                    Research.loadFromConfig(section, nsKey).register()
                }
            }
        }

        val end = System.currentTimeMillis()
        logger.info("Loaded researches in ${(end - start) / 1000.0}s")
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
        FluidPipePlacementService.cleanup()
        BlockStorage.cleanupEverything()
        EntityStorage.cleanupEverything()
        PylonMetrics.save()
    }

    override val javaPlugin = this

    override val material = Material.BEDROCK

    override val languages: Set<Locale> = setOf(
        Locale.ENGLISH,
        Locale.of("enws")
    )
}

private fun addDefaultPermission(permission: String) {
    Bukkit.getPluginManager().addPermission(Permission(permission, PermissionDefault.TRUE))
}