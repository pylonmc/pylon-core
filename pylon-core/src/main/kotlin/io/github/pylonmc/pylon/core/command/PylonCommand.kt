@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.command

import com.destroystokyo.paper.profile.PlayerProfile
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.block.waila.Waila.Companion.wailaEnabled
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.entity.display.transform.Rotation
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.item.research.addResearch
import io.github.pylonmc.pylon.core.item.research.hasResearch
import io.github.pylonmc.pylon.core.item.research.removeResearch
import io.github.pylonmc.pylon.core.metrics.PylonMetrics
import io.github.pylonmc.pylon.core.particles.ConfettiParticle
import io.github.pylonmc.pylon.core.recipe.ConfigurableRecipeType
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.test.GameTestConfig
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.vanillaDisplayName
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.math.FinePosition
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import kotlin.reflect.typeOf
import io.papermc.paper.math.BlockPosition as PaperBlockPosition

private val guide = buildCommand("guide") {
    permission("pylon.command.guide")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py guide")
        player.inventory.addItem(PylonGuide.STACK)
    }
    argument("players", ArgumentTypes.players()) {
        permission("pylon.command.guide.others")
        executes {
            PylonMetrics.onCommandRun("/py guide")
            val players = getArgument<List<Player>>("players")
            for (player in players) {
                player.inventory.addItem(PylonGuide.STACK)
            }
            val singular = players.size == 1
            source.sender.sendVanillaFeedback(
                "give.success." + if (singular) "single" else "multiple",
                Component.text(1),
                PylonGuide.STACK.vanillaDisplayName(),
                if (singular) players[0].name() else Component.text(players.size)
            )
        }
    }
}

private val give = buildCommand("give") {
    argument("players", ArgumentTypes.players()) {
        argument("item", RegistryCommandArgument(PylonRegistry.ITEMS)) {
            // Why does Brigadier not support default values for arguments?
            // https://github.com/Mojang/brigadier/issues/110

            fun givePlayers(context: CommandContext<CommandSourceStack>, amount: Int) {
                val item = context.getArgument<PylonItemSchema>("item")
                val players = context.getArgument<List<Player>>("players")
                val singular = players.size == 1
                for (player in players) {
                    player.inventory.addItem(item.itemStack.asQuantity(amount))
                }
                context.source.sender.sendVanillaFeedback(
                    "give.success." + if (singular) "single" else "multiple",
                    Component.text(amount),
                    item.itemStack.vanillaDisplayName(),
                    if (singular) players[0].name() else Component.text(players.size)
                )
            }

            permission("pylon.command.give")
            executes {
                PylonMetrics.onCommandRun("/py give")
                givePlayers(this, 1)
            }

            argument("amount", IntegerArgumentType.integer(1)) {
                permission("pylon.command.give")
                executes {
                    PylonMetrics.onCommandRun("/py give")
                    givePlayers(this, IntegerArgumentType.getInteger(this, "amount"))
                }
            }
        }
    }
}

private val debug = buildCommand("debug") {
    permission("pylon.command.debug")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py debug")
        player.inventory.addItem(DebugWaxedWeatheredCutCopperStairs.STACK)
        player.sendVanillaFeedback("give.success.single", Component.text(1), DebugWaxedWeatheredCutCopperStairs.STACK.vanillaDisplayName(), player.name())
    }
}

private val key = buildCommand("key") {
    permission("pylon.command.key")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py key")
        val item = PylonItem.fromStack(player.inventory.getItem(EquipmentSlot.HAND))
        if (item == null) {
            player.sendFeedback("key.no_item")
            return@executesWithPlayer
        }
        player.sendMessage(Component.text(item.key.toString())
            .hoverEvent(HoverEvent.showText(Component.translatable("pylon.pyloncore.message.command.key.hover")))
            .clickEvent(ClickEvent.copyToClipboard(item.key.toString())))
    }
}

private val setblock = buildCommand("setblock") {
    argument("pos", ArgumentTypes.blockPosition()) {
        argument("block", RegistryCommandArgument(PylonRegistry.BLOCKS)) {
            permission("pylon.command.setblock")
            executes {
                PylonMetrics.onCommandRun("/py setblock")
                val location = getArgument<PaperBlockPosition>("pos").toLocation(source.location.world)
                if (!location.world.isPositionLoaded(location)) {
                    source.sender.sendMessage(Component.translatable("argument.pos.unloaded"))
                    return@executes
                } else if (location.blockX !in -30000000..30000000 || location.blockZ !in -30000000..30000000 || location.blockY !in location.world.minHeight..location.world.maxHeight) {
                    source.sender.sendMessage(Component.translatable("argument.pos.outofworld"))
                    return@executes
                }

                val block = getArgument<PylonBlockSchema>("block")
                val failed = BlockStorage.placeBlock(location, block.key) == null
                source.sender.sendVanillaFeedback(
                    if (failed) "setblock.failed" else "setblock.success",
                    Component.text(location.blockX), Component.text(location.blockY), Component.text(location.blockZ)
                )
            }
        }
    }
}

private val waila = buildCommand("waila") {
    permission("pylon.command.waila")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py waila")
        player.wailaEnabled = !player.wailaEnabled
        player.sendFeedback(if (player.wailaEnabled) "waila.enabled" else "waila.disabled")
    }
}

private val gametest = buildCommand("gametest") {
    argument("pos", ArgumentTypes.blockPosition()) {
        argument("test", RegistryCommandArgument(PylonRegistry.GAMETESTS)) {
            permission("pylon.command.gametest")
            executesWithPlayer { player ->
                PylonMetrics.onCommandRun("/py gametest")
                val position = BlockPosition(getArgument<PaperBlockPosition>("pos").toLocation(player.world))
                val test = getArgument<GameTestConfig>("test")
                player.sendFeedback(
                    "gametest.started",
                    PylonArgument.of("test", test.key.toString()),
                    PylonArgument.of("location", position.toString())
                )
                PylonCore.launch {
                    val result = test.launch(position).await()
                    if (result != null) {
                        player.sendFeedback(
                            "gametest.failed",
                            PylonArgument.of("test", test.key.toString()),
                            PylonArgument.of("reason", result.message ?: "Unknown error")
                        )
                    } else {
                        player.sendFeedback(
                            "gametest.success",
                            PylonArgument.of("test", test.key.toString())
                        )
                    }
                }
            }
        }
    }
}

private val researchAdd = buildCommand("add") {
    argument("players", ArgumentTypes.players()) {
        fun addResearches(context: CommandContext<CommandSourceStack>, researches: List<Research>, confetti: Boolean = true) {
            for (player in context.getArgument<List<Player>>("players")) {
                for (res in researches) {
                    player.addResearch(res, false, confetti)
                    context.source.sender.sendFeedback(
                        "research.added",
                        PylonArgument.of("research", res.name),
                        PylonArgument.of("player", player.name)
                    )
                }
            }
        }

        literal("*") {
            permission("pylon.command.research.modify")
            executes {
                // no confetti for all research otherwise server go big boom
                PylonMetrics.onCommandRun("/py research add")
                addResearches(this, PylonRegistry.RESEARCHES.toList(), false)
            }
        }

        argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
            permission("pylon.command.research.modify")
            executes {
                PylonMetrics.onCommandRun("/py research add")
                val res = getArgument<Research>("research")
                addResearches(this, listOf(res))
            }
        }
    }
}

private val researchList = buildCommand("list") {
    fun listResearches(sender: CommandSender, player: Player, type: String) {
        val researches = Research.getResearches(player)
        if (researches.isEmpty()) {
            sender.sendFeedback("research.list.none$type", PylonArgument.of("player", player.name))
            return
        }
        val names = Component.join(JoinConfiguration.commas(true), researches.map(Research::name))
        sender.sendFeedback(
            "research.list.discovered$type",
            PylonArgument.of("player", player.name),
            PylonArgument.of("count", researches.size),
            PylonArgument.of("list", names)
        )
    }

    permission("pylon.command.research.list.self")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py research list")
        listResearches(player, player, "_self")
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.list")
        executes { sender ->
            PylonMetrics.onCommandRun("/py research list")
            val player = getArgument<Player>("player")
            listResearches(sender, player, "_other")
        }
    }
}

private val researchDiscover = buildCommand("discover") {
    argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
        permission("pylon.command.research.discover")
        executesWithPlayer { player ->
            PylonMetrics.onCommandRun("/py research discover")
            val res = getArgument<Research>("research")
            if (player.hasResearch(res)) {
                player.sendFeedback(
                    "research.already_discovered",
                    PylonArgument.of("research", res.name)
                )
                return@executesWithPlayer
            }
            if (res.cost == null) {
                player.sendFeedback(
                    "research.cannot_unlock",
                    PylonArgument.of("research", res.name)
                )
                return@executesWithPlayer
            }
            if (player.researchPoints < res.cost) {
                player.sendFeedback(
                    "research.not_enough_points",
                    PylonArgument.of("research", res.name),
                    PylonArgument.of("points", player.researchPoints),
                    PylonArgument.of("cost", res.cost)
                )
                return@executesWithPlayer
            }
            player.addResearch(res, sendMessage = true)
            player.researchPoints -= res.cost
        }
    }
}

private val researchRemove = buildCommand("remove") {
    argument("players", ArgumentTypes.players()) {
        fun removeResearches(context: CommandContext<CommandSourceStack>, researches: List<Research>) {
            for (player in context.getArgument<List<Player>>("players")) {
                for (res in researches) {
                    if (player.hasResearch(res)) {
                        player.removeResearch(res)
                        context.source.sender.sendFeedback(
                            "research.removed",
                            PylonArgument.of("research", res.name),
                            PylonArgument.of("player", player.name)
                        )
                    }
                }
            }
        }

        literal("*") {
            permission("pylon.command.research.modify")
            executes {
                PylonMetrics.onCommandRun("/py research remove")
                removeResearches(this, PylonRegistry.RESEARCHES.toList())
            }
        }

        argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
            permission("pylon.command.research.modify")
            executes {
                PylonMetrics.onCommandRun("/py research remove")
                val res = getArgument<Research>("research")
                removeResearches(this, listOf(res))
            }
        }
    }
}

private val researchPointsSet = buildCommand("set") {
    argument("players", ArgumentTypes.players()) {
        argument("points", LongArgumentType.longArg(0)) {
            permission("pylon.command.research.points.set")
            executes { sender ->
                PylonMetrics.onCommandRun("/py research points set")
                val points = getArgument<Long>("points")
                for (player in getArgument<List<Player>>("players")) {
                    player.researchPoints = points
                    sender.sendFeedback(
                        "research.points.set",
                        PylonArgument.of("player", player.name),
                        PylonArgument.of("points", points)
                    )
                }
            }
        }
    }
}

private val researchPointsAdd = buildCommand("add") {
    argument("players", ArgumentTypes.players()) {
        argument("points", LongArgumentType.longArg()) {
            permission("pylon.command.research.points.set")
            executes { sender ->
                PylonMetrics.onCommandRun("/py research points add")
                val points = getArgument<Long>("points")
                for (player in getArgument<List<Player>>("players")) {
                    player.researchPoints += points
                    sender.sendFeedback(
                        "research.points.added",
                        PylonArgument.of("player", player.name),
                        PylonArgument.of("points", points)
                    )
                }
            }
        }
    }
}

private val researchPointsSubtract = buildCommand("subtract") {
    argument("players", ArgumentTypes.players()) {
        argument("points", LongArgumentType.longArg()) {
            permission("pylon.command.research.points.set")
            executes { sender ->
                PylonMetrics.onCommandRun("/py research points subtract")
                val points = getArgument<Long>("points")
                for (player in getArgument<List<Player>>("players")) {
                    player.researchPoints -= points
                    sender.sendFeedback(
                        "research.points.removed",
                        PylonArgument.of("player", player.name),
                        PylonArgument.of("points", points)
                    )
                }
            }
        }
    }
}

private val researchPointQuery = buildCommand("query") {
    fun getPoints(sender: CommandSender, player: Player) {
        val points = player.researchPoints
        sender.sendFeedback(
            "research.points.query",
            PylonArgument.of("player", player.name),
            PylonArgument.of("points", points)
        )
    }

    permission("pylon.command.research.points.query.self")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py research points query")
        getPoints(player, player)
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.points.query")
        executes { sender ->
            PylonMetrics.onCommandRun("/py research points query")
            val player = getArgument<Player>("player")
            getPoints(sender, player)
        }
    }
}

private val researchPoints = buildCommand("points") {
    then(researchPointsSet)
    then(researchPointsAdd)
    then(researchPointsSubtract)
    then(researchPointQuery)
}

private val research = buildCommand("research") {
    then(researchAdd)
    then(researchList)
    then(researchDiscover)
    then(researchRemove)
    then(researchPoints)
}

private val exposeRecipeConfig = buildCommand("exposerecipeconfig") {
    argument("addon", RegistryCommandArgument(PylonRegistry.ADDONS)) {
        argument("recipe", RegistryCommandArgument(PylonRegistry.RECIPE_TYPES)) {
            permission("pylon.command.exposerecipeconfig")
            executes { sender ->
                PylonMetrics.onCommandRun("/py exposerecipeconfig")
                val addon = getArgument<PylonAddon>("addon")
                val recipeType = getArgument<RecipeType<*>>("recipe")
                if (recipeType !is ConfigurableRecipeType) {
                    sender.sendFeedback("exposerecipe.not-configurable")
                    return@executes
                }
                sender.sendFeedback(
                    "exposerecipe.warning",
                    PylonArgument.of("file", "plugins/PylonCore/${recipeType.filePath}")
                )
                addon.mergeGlobalConfig(recipeType.filePath, recipeType.filePath)
            }
        }
    }
}

private val confetti = buildCommand("confetti") {
    argument("amount", IntegerArgumentType.integer(1)) {
        permission("pylon.command.confetti")
        executesWithPlayer { player ->
            PylonMetrics.onCommandRun("/py confetti")
            ConfettiParticle.spawnMany(player.location, IntegerArgumentType.getInteger(this, "amount")).run()
            player.sendVanillaFeedback("commands.particle.success", Component.text("pyloncore:confetti"))
        }
        argument("speed", DoubleArgumentType.doubleArg(0.0)) {
            permission("pylon.command.confetti")
            executesWithPlayer { player ->
                PylonMetrics.onCommandRun("/py confetti")
                ConfettiParticle.spawnMany(player.location, IntegerArgumentType.getInteger(this, "amount"), DoubleArgumentType.getDouble(this, "speed"))
                player.sendVanillaFeedback("commands.particle.success", Component.text("pyloncore:confetti"))
            }
            argument("lifetime", IntegerArgumentType.integer(1)) {
                permission("pylon.command.confetti")
                executesWithPlayer { player ->
                    PylonMetrics.onCommandRun("/py confetti")
                    ConfettiParticle.spawnMany(
                        player.location,
                        IntegerArgumentType.getInteger(this, "amount"),
                        DoubleArgumentType.getDouble(this, "speed"),
                        IntegerArgumentType.getInteger(this, "lifetime")
                    )
                    player.sendVanillaFeedback("commands.particle.success", Component.text("pyloncore:confetti"))
                }
            }
        }
    }
}

@JvmSynthetic
internal val ROOT_COMMAND = buildCommand("pylon") {
    permission("pylon.command.guide")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py")
        PylonGuide.open(player)
    }

    then(guide)
    then(give)
    then(debug)
    then(key)
    then(setblock)
    then(waila)
    then(gametest)
    then(research)
    then(exposeRecipeConfig)
    then(confetti)
}

@JvmSynthetic
internal val ROOT_COMMAND_PY_ALIAS = buildCommand("py") {
    redirect(ROOT_COMMAND)
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <reified T> CommandContext<CommandSourceStack>.getArgument(name: String): T {
    return when (typeOf<T>()) {
        typeOf<PaperBlockPosition>() -> getArgument(name, BlockPositionResolver::class.java).resolve(source)
        typeOf<List<Entity>>() -> getArgument(name, EntitySelectorArgumentResolver::class.java).resolve(source)
        typeOf<Entity>() -> getArgument(name, EntitySelectorArgumentResolver::class.java).resolve(source).first()
        typeOf<FinePosition>() -> getArgument(name, FinePositionResolver::class.java).resolve(source)
        typeOf<List<PlayerProfile>>() -> getArgument(name, PlayerProfileListResolver::class.java).resolve(source)
        typeOf<PlayerProfile>() -> getArgument(name, PlayerProfileListResolver::class.java).resolve(source).first()
        typeOf<List<Player>>() -> getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(source)
        typeOf<Player>() -> getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(source).first()
        typeOf<Rotation>() -> getArgument(name, RotationResolver::class.java).resolve(source)
        else -> getArgument(name, T::class.java)
    } as T
}

private fun CommandSender.sendFeedback(key: String, vararg args: ComponentLike) {
    sendMessage(Component.translatable("pylon.pyloncore.message.command.$key").arguments(*args))
}

private fun CommandSender.sendVanillaFeedback(key: String, vararg args: ComponentLike) {
    sendMessage(Component.translatable("commands.$key", *args))
}