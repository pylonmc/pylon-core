@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.waila.Waila
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND_PY_ALIAS
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.fluid.connecting.ConnectingService
import io.github.pylonmc.pylon.core.i18n.PylonTranslator
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.recipe.ConfigurableRecipeType
import io.github.pylonmc.pylon.core.recipe.DisplayRecipeType
import io.github.pylonmc.pylon.core.recipe.PylonRecipeListener
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import java.util.Locale
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

object PylonCore : JavaPlugin(), PylonAddon {

    override fun onEnable() {
        InvUI.getInstance().setPlugin(this)

        saveDefaultConfig()

        Bukkit.getPluginManager().registerEvents(PylonTranslator, this)
        Bukkit.getPluginManager().registerEvents(PylonAddon, this)

        // Anything that listens for addon registration must be above this line
        registerWithPylon()

        Bukkit.getPluginManager().registerEvents(BlockStorage, this)
        Bukkit.getPluginManager().registerEvents(BlockListener, this)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)
        Bukkit.getPluginManager().registerEvents(MobDropListener, this)
        Bukkit.getPluginManager().registerEvents(TickManager, this)
        Bukkit.getPluginManager().registerEvents(MultiblockCache, this)
        Bukkit.getPluginManager().registerEvents(EntityStorage, this)
        Bukkit.getPluginManager().registerEvents(EntityListener, this)
        Bukkit.getPluginManager().registerEvents(Waila, this)
        Bukkit.getPluginManager().registerEvents(Research, this)
        Bukkit.getPluginManager().registerEvents(PylonGuiBlock, this)
        Bukkit.getPluginManager().registerEvents(PylonEntityHolderBlock, this)
        Bukkit.getPluginManager().registerEvents(PylonSimpleMultiblock, this)
        Bukkit.getPluginManager().registerEvents(PylonFluidBufferBlock, this)
        Bukkit.getPluginManager().registerEvents(PylonFluidTank, this)
        Bukkit.getPluginManager().registerEvents(PylonRecipeListener, this)
        Bukkit.getPluginManager().registerEvents(ConnectingService, this)
        Bukkit.getPluginManager().registerEvents(PylonTickingBlock, this)

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
        addDefaultPermission("pylon.command.research.points.get.self")
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

        PylonEntity.register<BlockDisplay, PylonSimpleMultiblock.MultiblockGhostBlock>(
            PylonSimpleMultiblock.MultiblockGhostBlock.KEY,
        )

        PylonEntity.register<ItemDisplay, FluidPointDisplay>(FluidPointDisplay.KEY)
        PylonEntity.register<Interaction, FluidPointInteraction>(FluidPointInteraction.KEY)
        PylonEntity.register<ItemDisplay, FluidPipeDisplay>(FluidPipeDisplay.KEY)

        PylonBlock.register<FluidPipeMarker>(FluidPipeMarker.KEY, Material.STRUCTURE_VOID)
        PylonBlock.register<FluidPipeConnector>(FluidPipeConnector.KEY, Material.STRUCTURE_VOID)

        DisplayRecipeType.register()
        RecipeType.addVanillaRecipes()

        launch {
            delay(1)
            postStartStuff()
        }
    }

    /**
     * Run after the server has fully started
     */
    private fun postStartStuff() {
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
            recipesDir.walk()
                .filter { it.extension == "yml" }
                .mapNotNull { path ->
                    NamespacedKey.fromString(path.nameWithoutExtension)
                        ?.let(PylonRegistry.RECIPE_TYPES::get)
                        ?.let { it as? ConfigurableRecipeType }
                        ?.let { type -> type to Config(path) }
                }
                .forEach { (type, config) -> type.loadFromConfig(config) }
        }
    }

    override fun onDisable() {
        ConnectingService.cleanup()
        BlockStorage.cleanupEverything()
        EntityStorage.cleanupEverything()
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