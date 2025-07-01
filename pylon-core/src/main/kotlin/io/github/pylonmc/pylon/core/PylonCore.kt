package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import com.github.retrooper.packetevents.PacketEvents
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.addon.PylonAddonListener
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonGuiBlock
import io.github.pylonmc.pylon.core.block.base.PylonSimpleMultiblock
import io.github.pylonmc.pylon.core.block.waila.Waila
import io.github.pylonmc.pylon.core.command.PylonCommand
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.recipe.PylonRecipeListener
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.BlockDisplay
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.InvUI
import java.util.Locale


object PylonCore : JavaPlugin(), PylonAddon {

    private lateinit var manager: PaperCommandManager

    val packetEvents = SpigotPacketEventsBuilder.build(this)

    override fun onLoad() {
        PacketEvents.setAPI(packetEvents)
        packetEvents.load()
    }

    override fun onEnable() {
        packetEvents.init()
        InvUI.getInstance().setPlugin(this)

        val platform = SpigotEntityLibPlatform(this)
        val settings = APIConfig(PacketEvents.getAPI())
            .debugMode()
            .tickTickables()
            .trackPlatformEntities()
            .usePlatformLogger()

        EntityLib.init(platform, settings)

        saveDefaultConfig()

        Bukkit.getPluginManager().registerEvents(BlockStorage, this)
        Bukkit.getPluginManager().registerEvents(BlockListener, this)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)
        Bukkit.getPluginManager().registerEvents(MobDropListener, this)
        Bukkit.getPluginManager().registerEvents(TickManager, this)
        Bukkit.getPluginManager().registerEvents(PylonAddonListener, this)
        Bukkit.getPluginManager().registerEvents(MultiblockCache, this)
        Bukkit.getPluginManager().registerEvents(EntityStorage, this)
        Bukkit.getPluginManager().registerEvents(AddonTranslator, this)
        Bukkit.getPluginManager().registerEvents(EntityListener, this)
        Bukkit.getPluginManager().registerEvents(Waila, this)
        Bukkit.getPluginManager().registerEvents(Research, this)
        Bukkit.getPluginManager().registerEvents(PylonGuiBlock, this)
        Bukkit.getPluginManager().registerEvents(PylonEntityHolderBlock, this)
        Bukkit.getPluginManager().registerEvents(PylonSimpleMultiblock, this)
        Bukkit.getPluginManager().registerEvents(PylonRecipeListener, this)

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

        registerWithPylon()

        PylonItem.register(DebugWaxedWeatheredCutCopperStairs::class.java, DebugWaxedWeatheredCutCopperStairs.STACK)
        PylonGuide.hideItem(DebugWaxedWeatheredCutCopperStairs.KEY)

        PylonItem.register(PhantomBlock.ErrorItem::class.java, PhantomBlock.ErrorItem.STACK)
        PylonGuide.hideItem(PhantomBlock.ErrorItem.KEY)

        PylonItem.register(PylonGuide::class.java, PylonGuide.STACK)
        PylonGuide.hideItem(PylonGuide.KEY)

        PylonEntity.register(
            PylonSimpleMultiblock.MultiblockGhostBlock.KEY,
            BlockDisplay::class.java,
            PylonSimpleMultiblock.MultiblockGhostBlock::class.java
        )

        RecipeType.addVanillaRecipes()
    }

    override fun onDisable() {
        BlockStorage.cleanupEverything()
        EntityStorage.cleanupEverything()
        packetEvents.terminate()
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