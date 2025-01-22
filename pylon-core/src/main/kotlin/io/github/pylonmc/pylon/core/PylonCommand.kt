package io.github.pylonmc.pylon.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.registry.PylonRegistries
import io.github.pylonmc.pylon.core.registry.PyonRegistryKeys
import kotlinx.coroutines.future.await
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

@Suppress("unused")
@CommandAlias("pylon|py")
object PylonCommand : BaseCommand() {

    @Subcommand("test")
    @CommandCompletion("@gametests")
    @Description("Run a game test")
    fun test(player: Player, test: NamespacedKey, @Optional location: Location?) {
        val spawnLocation = location ?: player.location
        val gameTest = PylonRegistries.getRegistry(PyonRegistryKeys.GAMETESTS)[test]
        if (gameTest == null) {
            player.sendMessage("Game test not found: $test")
            return
        }
        pluginInstance.launch {
            val result = gameTest.launch(BlockPosition(spawnLocation)).await()
            if (result != null) {
                player.sendMessage("Game test failed: ${result.message}")
            } else {
                player.sendMessage("Game test succeeded")
            }
        }
    }
}