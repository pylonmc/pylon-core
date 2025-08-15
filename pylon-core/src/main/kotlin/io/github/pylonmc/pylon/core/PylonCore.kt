@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.waila.Waila
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND
import io.github.pylonmc.pylon.core.command.ROOT_COMMAND_PY_ALIAS
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.fluid.connecting.ConnectingService
import io.github.pylonmc.pylon.core.i18n.MinecraftTranslator
import io.github.pylonmc.pylon.core.i18n.PylonTranslator
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.recipe.DisplayRecipeType
import io.github.pylonmc.pylon.core.recipe.PylonRecipeListener
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import java.util.Locale

object PylonCore : JavaPlugin(), PylonAddon {

    override fun onEnable() {
        InvUI.getInstance().setPlugin(this)

        saveDefaultConfig()

        Bukkit.getPluginManager().registerEvents(PylonTranslator, this)
        Bukkit.getPluginManager().registerEvents(PylonAddon, this)
        registerWithPylon()

        // Start loading of vanilla translations as to not hang the server when it is first needed
        MinecraftTranslator

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
            delay(1.ticks)
            for (type in PylonRegistry.RECIPE_TYPES) {
                for (addon in PylonRegistry.ADDONS) {
                    val configStream = addon.javaPlugin.getResource("recipes/${type.key}.yml") ?: continue
                    val config = configStream.reader().use { ConfigSection(YamlConfiguration.loadConfiguration(it)) }
                    type.loadFromConfig(config)
                }
            }
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