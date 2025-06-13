package io.github.pylonmc.pylon.core.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.waila.Waila.Companion.wailaEnabled
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researches
import io.github.pylonmc.pylon.core.item.research.addResearch
import io.github.pylonmc.pylon.core.item.research.hasResearch
import io.github.pylonmc.pylon.core.item.research.removeResearch
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.jetbrains.annotations.ApiStatus

@Suppress("unused")
@CommandAlias("pylon|py")
@ApiStatus.Internal
internal class PylonCommand : BaseCommand() {

    @Default
    @Description("Open the Pylon guide")
    @CommandPermission("pylon.command.open_guide")
    fun default(player: Player) {
        PylonGuide.open(player)
    }

    @Subcommand("guide")
    @Description("Obtain the Pylon guide")
    @CommandPermission("pylon.command.guide")
    fun guide(player: Player) {
        player.inventory.addItem(PylonGuide.STACK)
    }

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
        player.give(DebugWaxedWeatheredCutCopperStairs.STACK)
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
        BlockStorage.placeBlock(location, pylonBlock.key)
    }

    @Subcommand("waila")
    @Description("Toggle your WAILA bossbar")
    @CommandPermission("pylon.command.waila")
    fun toggleWaila(player: Player) {
        player.wailaEnabled = !player.wailaEnabled
    }

    init {
        Bukkit.getPluginManager().addPermission(
            Permission(
                "pylon.command.waila",
                PermissionDefault.TRUE
            )
        )
    }

    @Private
    @Subcommand("test")
    @CommandCompletion("@gametests")
    @Description("Run a game test")
    fun test(player: Player, test: NamespacedKey, @Optional location: Location?) {
        val spawnLocation = location ?: player.location
        val gameTest = PylonRegistry.GAMETESTS[test]
        if (gameTest == null) {
            player.sendRichMessage("<red>Game test not found: $test")
            return
        }
        PylonCore.launch {
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
        fun add(sender: CommandSender, p: OnlinePlayer, research: NamespacedKey) {
            val player = p.player
            val res = PylonRegistry.RESEARCHES[research]
            if (res == null) {
                player.sendRichMessage("<red>Research not found: $research")
                return
            }
            player.addResearch(res, sendMessage = false)
            val name = MiniMessage.miniMessage().serialize(res.name)
            sender.sendRichMessage("<green>Added research $name to ${player.name}")
        }

        @Subcommand("addall")
        @CommandCompletion("@players")
        @Description("Add all researches to a player")
        @CommandPermission("pylon.command.research.modify")
        fun addAll(p: OnlinePlayer) {
            val player = p.player
            for (res in PylonRegistry.RESEARCHES) {
                player.addResearch(res, sendMessage = true)
            }
        }

        @Subcommand("list")
        @Description("List all discovered researches")
        @CommandPermission("pylon.command.research.list")
        fun list(player: Player) {
            val researches = player.researches
            if (researches.isEmpty()) {
                player.sendMessage(Component.translatable("pylon.pyloncore.message.research.list.none"))
                return
            }
            val names = Component.join(JoinConfiguration.commas(true), researches.map(Research::name))
            player.sendMessage(Component.translatable(
                "pylon.pyloncore.message.research.list.discovered",
                PylonArgument.of("count", researches.size),
                PylonArgument.of("list", names)
            ))
        }

        init {
            Bukkit.getPluginManager().addPermission(
                Permission(
                    "pylon.command.research.list",
                    PermissionDefault.TRUE
                )
            )
        }

        // Intended for normal players to use
        @Subcommand("discover")
        @CommandCompletion("@researches")
        @Description("Research a research")
        @CommandPermission("pylon.command.research.discover")
        fun discover(player: Player, research: NamespacedKey) {
            val res = PylonRegistry.RESEARCHES[research]
            if (res == null) {
                player.sendRichMessage("<red>Research not found: $research")
                return
            }
            if (player.hasResearch(res)) {
                player.sendRichMessage("<red>You have already discovered this research")
                return
            }
            if (res.cost == null) {
                player.sendRichMessage("<red>This research cannot be unlocked using research points")
                return
            }
            if (player.researchPoints < res.cost) {
                player.sendMessage(
                    // Yes, I know this is the only translated command message;
                    // a future PR will convert all the messages to translatable
                    Component.translatable(
                        "pylon.pyloncore.message.research.not_enough_points",
                        PylonArgument.of("research", res.name),
                        PylonArgument.of("points", player.researchPoints),
                        PylonArgument.of("cost", res.cost)
                    )
                )
                return
            }
            player.addResearch(res, sendMessage = true)
            player.researchPoints -= res.cost
        }

        init {
            Bukkit.getPluginManager().addPermission(
                Permission(
                    "pylon.command.research.discover",
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
            val res = PylonRegistry.RESEARCHES[research]
            if (res == null) {
                player.sendRichMessage("<red>Research not found: $research")
                return
            }
            player.removeResearch(res)
            val name = MiniMessage.miniMessage().serialize(res.name)
            player.sendRichMessage("<green>Removed research $name from ${player.name}")
        }

        @Subcommand("points")
        inner class PointsCommand : BaseCommand() {

            @Subcommand("set")
            @CommandCompletion("@players")
            @Description("Set a player's research points")
            @CommandPermission("pylon.command.research.points.set")
            fun set(sender: CommandSender, p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints = points
                sender.sendRichMessage("<green>Set research points of ${player.name} to $points")
            }

            @Subcommand("add")
            @CommandCompletion("@players")
            @Description("Add research points to a player")
            @CommandPermission("pylon.command.research.points.set")
            fun add(sender: CommandSender, p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints += points
                sender.sendRichMessage("<green>Added $points research points to ${player.name}")
            }

            @Subcommand("subtract")
            @CommandCompletion("@players")
            @Description("Subtract research points from a player")
            @CommandPermission("pylon.command.research.points.set")
            fun subtract(sender: CommandSender, p: OnlinePlayer, points: Long) {
                val player = p.player
                player.researchPoints -= points
                sender.sendRichMessage("<green>Removed $points research points from ${player.name}")
            }

            @Subcommand("get")
            @CommandCompletion("@players")
            @Description("Get a player's research points")
            @CommandPermission("pylon.command.research.points.get")
            fun get(sender: CommandSender, p: OnlinePlayer) {
                val player = p.player
                val points = player.researchPoints
                sender.sendRichMessage("<green>Research points of ${player.name}: $points")
            }

            @Subcommand("me")
            @Description("Get your research points")
            @CommandPermission("pylon.command.research.points.get.self")
            fun me(player: Player) {
                val points = player.researchPoints
                player.sendRichMessage("<green>Research points: $points")
            }

            init {
                Bukkit.getPluginManager().addPermission(
                    Permission(
                        "pylon.command.research.points.get.self",
                        PermissionDefault.TRUE
                    )
                )
            }
        }
    }
}