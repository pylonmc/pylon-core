package io.github.pylonmc.pylon.core.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.waila.Waila
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.item.research.Research.Companion.addResearch
import io.github.pylonmc.pylon.core.item.research.Research.Companion.removeResearch
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

@Suppress("unused")
@CommandAlias("pylon|py")
internal class PylonCommand : BaseCommand() {

    @Subcommand("give")
    @CommandCompletion("@players @items")
    @Description("Give a Pylon item to a player")
    @CommandPermission("pylon.command.give")
    fun give(p: OnlinePlayer, item: NamespacedKey, @Default("1") amount: Int) {
        val player = p.player
        val pylonItem = PylonRegistry.ITEMS[item]
        if (pylonItem == null) {
            player.sendRichMessage("<red>Item not found: $item")
            return
        }
        val stack = pylonItem.itemStack
        stack.amount = amount
        player.inventory.addItem(stack)
    }

    @Subcommand("debug")
    @Description("Gives you the pylon debugging item to view pylon data")
    @CommandPermission("pylon.command.debug")
    fun debug(player: Player) {
        player.give(DebugWaxedWeatheredCutCopperStairs.itemStack)
    }

    @Subcommand("setblock")
    @CommandCompletion("@blocks")
    @Description("Set a block to be a pylon block")
    @CommandPermission("pylon.command.setblock")
    fun setBlock(player: Player, block: NamespacedKey) {
        val location = BlockPosition(player.location)
        location.block.type = Material.AIR
        val pylonBlock = PylonRegistry.BLOCKS[block]
        if (pylonBlock == null) {
            player.sendRichMessage("<red>Block not found: $block")
            return
        }
        BlockStorage.placeBlock(location, pylonBlock)
    }

    @Subcommand("waila")
    @Description("Toggle your WAILA bossbar")
    @CommandPermission("pylon.command.waila")
    fun waila(player: Player) {
        Waila.setWailaEnabled(player, !Waila.isWailaEnabled(player))
    }

    @Private
    @Subcommand("test")
    @CommandCompletion("@gametests")
    @Description("Run a game test")
    fun test(player: Player, test: NamespacedKey, @Optional location: Location?) {
        val spawnLocation = location ?: player.location
        val gameTest = PylonRegistry.GAMETESTS[test]
        if (gameTest == null) {
            player.sendMessage("Game test not found: $test")
            return
        }
        pluginInstance.launch {
            val result = gameTest.launch(BlockPosition(spawnLocation)).await()
            if (result != null) {
                player.sendRichMessage("<red>Game test $test failed: ${result.message}")
            } else {
                player.sendRichMessage("<green>Game test $test succeeded")
            }
        }
    }

    @Subcommand("research")
    inner class ResearchCommand : BaseCommand() {

        @Subcommand("add")
        @CommandCompletion("@players @researches")
        @Description("Add a research to a player")
        @CommandPermission("pylon.command.research.modify")
        fun add(p: OnlinePlayer, research: NamespacedKey) {
            val player = p.player
            for (res in getResearches(player, research) ?: return) {
                player.addResearch(res, deductPoints = false, sendMessage = false)
            }
        }

        // Intended for normal players to use
        @Subcommand("research")
        @CommandCompletion("@researches")
        @Description("Research a research")
        @CommandPermission("pylon.command.research.research")
        fun research(player: Player, research: NamespacedKey) {
            for (res in getResearches(player, research) ?: return) {
                player.addResearch(res, deductPoints = true, sendMessage = true)
            }
        }

        init {
            Bukkit.getPluginManager().addPermission(
                Permission(
                    "pylon.command.research.research",
                    PermissionDefault.TRUE
                )
            )
        }

        @Subcommand("remove")
        @CommandCompletion("@players @researches")
        @Description("Remove a research from a player")
        @CommandPermission("pylon.command.research.modify")
        fun remove(p: OnlinePlayer, research: NamespacedKey) {
            val player = p.player
            for (res in getResearches(player, research) ?: return) {
                player.removeResearch(res.key)
            }
        }

        private fun getResearches(player: Player, research: NamespacedKey): Iterable<Research>? {
            val researches = if (research.key == "all") {
                PylonRegistry.RESEARCHES
            } else {
                PylonRegistry.RESEARCHES[research]?.let(::listOf)
            }
            if (researches == null) {
                player.sendRichMessage("<red>Research not found: $research")
            }
            return researches
        }

        @Subcommand("points")
        inner class PointsCommand : BaseCommand() {

            @Subcommand("set")
            @CommandCompletion("@players")
            @Description("Set a player's research points")
            @CommandPermission("pylon.command.research.points.set")
            fun set(p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints = points
                player.sendRichMessage("<green>Set research points to $points")
            }

            @Subcommand("add")
            @CommandCompletion("@players")
            @Description("Add research points to a player")
            @CommandPermission("pylon.command.research.points.set")
            fun add(p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints += points
                player.sendRichMessage("<green>Added $points research points")
            }

            @Subcommand("remove")
            @CommandCompletion("@players")
            @Description("Remove research points from a player")
            @CommandPermission("pylon.command.research.points.set")
            fun remove(p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints -= points
                player.sendRichMessage("<green>Removed $points research points")
            }

            @Subcommand("get")
            @CommandCompletion("@players")
            @Description("Get a player's research points")
            @CommandPermission("pylon.command.research.points.get")
            fun get(p: OnlinePlayer) {
                val player = p.player
                val points = player.researchPoints
                player.sendRichMessage("<green>Research points: $points")
            }

            init {
                Bukkit.getPluginManager().addPermission(
                    Permission(
                        "pylon.command.research.points.get",
                        PermissionDefault.TRUE
                    )
                )
            }
        }
    }
}