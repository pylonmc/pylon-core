package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.waila.Waila
import io.github.pylonmc.pylon.core.command.PylonCommand
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeConnector
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeDisplay
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeMarker
import io.github.pylonmc.pylon.core.content.fluid.FluidPointDisplay
import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.fluid.connecting.ConnectingService
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.i18n.MinecraftTranslator
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.recipe.PylonRecipeListener
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import java.util.Locale

object PylonCore : JavaPlugin(), PylonAddon {

    private lateinit var manager: PaperCommandManager

    override fun onEnable() {
        InvUI.getInstance().setPlugin(this)

        saveDefaultConfig()

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
        Bukkit.getPluginManager().registerEvents(AddonTranslator, this)
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

        manager = PaperCommandManager(this)
        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }
        addRegistryCompletion("gametests", PylonRegistry.GAMETESTS)
        addRegistryCompletion("items", PylonRegistry.ITEMS)
        addRegistryCompletion("blocks", PylonRegistry.BLOCKS)
        addRegistryCompletion("researches", PylonRegistry.RESEARCHES)

        manager.registerCommand(PylonCommand())

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

        RecipeType.addVanillaRecipes()
    }

    override fun onDisable() {
        ConnectingService.cleanup()
        BlockStorage.cleanupEverything()
        EntityStorage.cleanupEverything()
    }

    private fun addRegistryCompletion(name: String, registry: PylonRegistry<*>) {
        manager.commandCompletions.registerCompletion(name) { _ ->
            registry.map { it.key.toString() }.sorted()
        }
    }

    override val javaPlugin = this

    override val material = Material.BEDROCK

    override val languages: Set<Locale> = setOf(
        Locale.ENGLISH,
        Locale.of("enws")
    )
}