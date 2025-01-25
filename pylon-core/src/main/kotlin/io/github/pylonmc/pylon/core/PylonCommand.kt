package io.github.pylonmc.pylon.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.plus
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

@Suppress("unused")
@CommandAlias("pylon|py")
object PylonCommand : BaseCommand() {

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