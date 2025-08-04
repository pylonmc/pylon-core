@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.command

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.pylonmc.pylon.core.PylonCore
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
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researches
import io.github.pylonmc.pylon.core.item.research.addResearch
import io.github.pylonmc.pylon.core.item.research.hasResearch
import io.github.pylonmc.pylon.core.item.research.removeResearch
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
    requiresPlayer(permission = "pylon.command.guide")
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

            requiresPlayer(permission = "pylon.command.give")
            executes { givePlayers(this, 1) }

            argument("amount", IntegerArgumentType.integer(1)) {
                requiresPlayer(permission = "pylon.command.give")
                executes { givePlayers(this, IntegerArgumentType.getInteger(this, "amount")) }
            }
        }
    }
}

private val debug = buildCommand("debug") {
    requiresPlayer(permission = "pylon.command.debug")
    executesWithPlayer { player ->
        player.inventory.addItem(DebugWaxedWeatheredCutCopperStairs.STACK)
    }
}

private val key = buildCommand("key") {
    requiresPlayer(permission = "pylon.command.key")
    executesWithPlayer { player ->
        val item = PylonItem.fromStack(player.inventory.getItem(EquipmentSlot.HAND))
        if (item == null) {
            player.sendMessage(Component.translatable("pylon.pyloncore.message.command.key.not_item"))
            return@executesWithPlayer
        }
        player.sendMessage(item.key.toString())
    }
}

private val setblock = buildCommand("setblock") {
    argument("location", ArgumentTypes.blockPosition()) {
        argument("block", RegistryCommandArgument(PylonRegistry.BLOCKS)) {
            requiresPlayer(permission = "pylon.command.setblock")
            executesWithPlayer { player ->
                val location = getArgument<PaperBlockPosition>("location")
                val block = getArgument<PylonBlockSchema>("block")
                BlockStorage.placeBlock(location.toLocation(player.world), block.key)
            }
        }
    }
}

private val waila = buildCommand("waila") {
    requiresPlayer(permission = "pylon.command.waila")
    executesWithPlayer { player ->
        player.wailaEnabled = !player.wailaEnabled
    }
}

private val gametest = buildCommand("gametest") {
    argument("location", ArgumentTypes.blockPosition()) {
        argument("test", RegistryCommandArgument(PylonRegistry.GAMETESTS)) {
            requiresPlayer(permission = "pylon.command.gametest")
            executesWithPlayer { player ->
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
        fun addResearches(context: CommandContext<CommandSourceStack>, researches: List<Research>) {
            for (player in context.getArgument<List<Player>>("players")) {
                for (res in researches) {
                    player.addResearch(res, sendMessage = false)
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
                addResearches(this, PylonRegistry.RESEARCHES.toList())
            }
        }

        argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
            permission("pylon.command.research.modify")
            executes {
                val res = getArgument<Research>("research")
                addResearches(this, listOf(res))
            }
        }
    }
}

private val researchList = buildCommand("list") {
    fun listResearches(sender: CommandSender, player: Player) {
        val researches = player.researches
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

    requiresPlayer(permission = "pylon.command.research.list.self")
    executesWithPlayer { player ->
        listResearches(player, player)
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.list")
        executes { sender ->
            val player = getArgument<Player>("player")
            listResearches(sender, player)
        }
    }
}

private val researchDiscover = buildCommand("discover") {
    argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
        requiresPlayer(permission = "pylon.command.research.discover")
        executesWithPlayer { player ->
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
                removeResearches(this, PylonRegistry.RESEARCHES.toList())
            }
        }

        argument("research", RegistryCommandArgument(PylonRegistry.RESEARCHES)) {
            permission("pylon.command.research.modify")
            executes {
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

    requiresPlayer(permission = "pylon.command.research.points.get.self")
    executesWithPlayer { player ->
        getPoints(player, player)
    }

    argument("player", ArgumentTypes.player()) {
        permission("pylon.command.research.points.get")
        executes { sender ->
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

@JvmSynthetic
internal val ROOT_COMMAND = buildCommand("pylon") {
    requiresPlayer(permission = "pylon.command.guide")
    executesWithPlayer { player ->
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
}

@JvmSynthetic
internal val ROOT_COMMAND_PY_ALIAS = buildCommand("py") {
    redirect(ROOT_COMMAND)
}