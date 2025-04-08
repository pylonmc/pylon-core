package io.github.pylonmc.pylon.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.plus
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull

@Suppress("unused")
@CommandAlias("pylon|py")
object PylonCommand : BaseCommand() {

    @Subcommand("give")
    @CommandCompletion("@players @items")
    @Description("Give a Pylon item to a player")
    @CommandPermission("pylon.command.give")
    fun give(p: OnlinePlayer, item: NamespacedKey, @Default("1") amount: Int) {
        val player = p.player
        val pylonItem = PylonRegistry.ITEMS[item]
        if (pylonItem == null) {
            player.sendMessage(NamedTextColor.RED + "Item not found: $item")
            return
        }
        val stack = pylonItem.itemStack
        stack.amount = amount
        player.inventory.addItem(stack)
    }

    @Subcommand("setblock")
    @CommandCompletion("0 0 0 @blocks @worlds")
    @Description("Set a block to be a pylon block")
    @CommandPermission("pylon.command.setblock")
    fun setBlock(x: Int, y: Int, z: Int, block: NamespacedKey, @Default @NotNull world: World) {
        val location = BlockPosition(world, x, y, z)
        location.world!!.loadChunk(location.world!!.getChunkAt(location.block))
        location.block.setType(Material.AIR)
        val pylonBlock = PylonRegistry.BLOCKS[block]
        if(pylonBlock == null) {
            currentCommandIssuer.sendMessage("Block not found: $block")
            return
        }
        BlockStorage.placeBlock(location, pylonBlock)
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
                player.sendMessage(NamedTextColor.RED + "Game test $test failed: ${result.message}")
            } else {
                player.sendMessage(NamedTextColor.GREEN + "Game test $test succeeded")
            }
        }
    }
}