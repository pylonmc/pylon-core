@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.command

import com.github.shynixn.mccoroutine.bukkit.launch
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
import io.github.pylonmc.pylon.core.util.getArgument
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import io.papermc.paper.math.BlockPosition as PaperBlockPosition

private val guide = buildCommand("guide") {
    permission("pylon.command.guide")
    executesWithPlayer { player ->
        player.inventory.addItem(PylonGuide.STACK)
    }
}

private val give = buildCommand("give") {
    argument("players", ArgumentTypes.players()) {
        argument("item", RegistryCommandArgument(PylonRegistry.ITEMS)) {
            // Why does Brigadier not support default values for arguments?
            // https://github.com/Mojang/brigadier/issues/110

            fun givePlayers(context: CommandContext<CommandSourceStack>, amount: Int) {
                val item = context.getArgument<PylonItemSchema>("item")
                for (player in context.getArgument<List<Player>>("players")) {
                    player.inventory.addItem(item.itemStack.asQuantity(amount))
                }
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
    }
}

private val key = buildCommand("key") {
    permission("pylon.command.key")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py key")
        val item = PylonItem.fromStack(player.inventory.getItem(EquipmentSlot.HAND))
        if (item == null) {
            player.sendMessage(Component.translatable("pylon.pyloncore.message.command.key.no_item"))
            return@executesWithPlayer
        }
        player.sendMessage(item.key.toString())
    }
}

private val setblock = buildCommand("setblock") {
    argument("location", ArgumentTypes.blockPosition()) {
        argument("block", RegistryCommandArgument(PylonRegistry.BLOCKS)) {
            permission("pylon.command.setblock")
            executesWithPlayer { player ->
                PylonMetrics.onCommandRun("/py setblock")
                val location = getArgument<PaperBlockPosition>("location")
                val block = getArgument<PylonBlockSchema>("block")
                BlockStorage.placeBlock(location.toLocation(player.world), block.key)
            }
        }
    }
}

private val waila = buildCommand("waila") {
    permission("pylon.command.waila")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py waila")
        player.wailaEnabled = !player.wailaEnabled
    }
}

private val gametest = buildCommand("gametest") {
    argument("location", ArgumentTypes.blockPosition()) {
        argument("test", RegistryCommandArgument(PylonRegistry.GAMETESTS)) {
            permission("pylon.command.gametest")
            executesWithPlayer { player ->
                PylonMetrics.onCommandRun("/py gametest")
                val location = getArgument<PaperBlockPosition>("location")
                val test = getArgument<GameTestConfig>("test")
                PylonCore.launch {
                    val result = test.launch(BlockPosition(location.toLocation(player.world))).await()
                    if (result != null) {
                        player.sendMessage(
                            Component.translatable(
                                "pylon.pyloncore.message.command.gametest.failed",
                                PylonArgument.of("test", test.key.toString()),
                                PylonArgument.of("reason", result.message ?: "Unknown error")
                            )
                        )
                    } else {
                        player.sendMessage(
                            Component.translatable(
                                "pylon.pyloncore.message.command.gametest.success",
                                PylonArgument.of("test", test.key.toString())
                            )
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
                    context.source.sender.sendMessage(
                        Component.translatable(
                            "pylon.pyloncore.message.command.research.added",
                            PylonArgument.of("research", res.name),
                            PylonArgument.of("player", player.name)
                        )
                    )
                }
            }
        }

        literal("*") {
            permission("pylon.command.research.modify")
            executes {
                // no confetti for all research otherwise server go big boom
                PylonMetrics.onCommandRun("/py research add")
                addResearches(this, PylonRegistry.RESEARCHES.toList())
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
    fun listResearches(sender: CommandSender, player: Player) {
        val researches = Research.getResearches(player)
        if (researches.isEmpty()) {
            sender.sendMessage(Component.translatable("pylon.pyloncore.message.command.research.list.none"))
            return
        }
        val names = Component.join(JoinConfiguration.commas(true), researches.map(Research::name))
        sender.sendMessage(
            Component.translatable(
                "pylon.pyloncore.message.command.research.list.discovered",
                PylonArgument.of("count", researches.size),
                PylonArgument.of("list", names)
            )
        )
    }

    permission("pylon.command.research.list.self")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py research list")
        listResearches(player, player)
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.list")
        executes { sender ->
            PylonMetrics.onCommandRun("/py research list")
            val player = getArgument<Player>("player")
            listResearches(sender, player)
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
                player.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.command.research.already_discovered",
                        PylonArgument.of("research", res.name)
                    )
                )
                return@executesWithPlayer
            }
            if (res.cost == null) {
                player.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.command.research.cannot_unlock",
                        PylonArgument.of("research", res.name)
                    )
                )
                return@executesWithPlayer
            }
            if (player.researchPoints < res.cost) {
                player.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.command.research.not_enough_points",
                        PylonArgument.of("research", res.name),
                        PylonArgument.of("points", player.researchPoints),
                        PylonArgument.of("cost", res.cost)
                    )
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
                        context.source.sender.sendMessage(
                            Component.translatable(
                                "pylon.pyloncore.message.command.research.removed",
                                PylonArgument.of("research", res.name),
                                PylonArgument.of("player", player.name)
                            )
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
                    sender.sendMessage(
                        Component.translatable(
                            "pylon.pyloncore.message.command.research.points.set",
                            PylonArgument.of("player", player.name),
                            PylonArgument.of("points", points)
                        )
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
                    sender.sendMessage(
                        Component.translatable(
                            "pylon.pyloncore.message.command.research.points.added",
                            PylonArgument.of("player", player.name),
                            PylonArgument.of("points", points)
                        )
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
                    sender.sendMessage(
                        Component.translatable(
                            "pylon.pyloncore.message.command.research.points.removed",
                            PylonArgument.of("player", player.name),
                            PylonArgument.of("points", points)
                        )
                    )
                }
            }
        }
    }
}

private val researchPointsGet = buildCommand("get") {
    fun getPoints(sender: CommandSender, player: Player) {
        val points = player.researchPoints
        sender.sendMessage(
            Component.translatable(
                "pylon.pyloncore.message.command.research.points.get",
                PylonArgument.of("player", player.name),
                PylonArgument.of("points", points)
            )
        )
    }

    permission("pylon.command.research.points.get.self")
    executesWithPlayer { player ->
        PylonMetrics.onCommandRun("/py research points get")
        getPoints(player, player)
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.points.get")
        executes { sender ->
            PylonMetrics.onCommandRun("/py research points get")
            val player = getArgument<Player>("player")
            getPoints(sender, player)
        }
    }
}

private val researchPoints = buildCommand("points") {
    then(researchPointsSet)
    then(researchPointsAdd)
    then(researchPointsSubtract)
    then(researchPointsGet)
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
                    sender.sendMessage(Component.translatable("pylon.pyloncore.message.command.exposerecipe.not-configurable"))
                    return@executes
                }
                sender.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.command.exposerecipe.warning",
                        PylonArgument.of("file", "plugins/PylonCore/${recipeType.filePath}")
                    )
                )
                addon.mergeGlobalConfig(recipeType.filePath, recipeType.filePath)
            }
        }
    }
}

private val confetti = buildCommand("confetti") {
    argument("amount", IntegerArgumentType.integer(1)) {
        permission("pylon.command.confetti")
        executes {
            PylonMetrics.onCommandRun("/py confetti")
            val sender = this.source.sender
            val amount = IntegerArgumentType.getInteger(this, "amount")

            if (sender !is Player) {
                sender.sendMessage(Component.translatable("pylon.pyloncore.message.command.error.must_be_player"))
                return@executes
            }

            ConfettiParticle.spawnMany(sender.location, amount).run()
            return@executes
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