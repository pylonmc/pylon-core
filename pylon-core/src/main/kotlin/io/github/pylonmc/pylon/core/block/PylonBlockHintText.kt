package io.github.pylonmc.pylon.core.block

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player


open class PylonBlockHintText(open var hintText: String, open var hintColor: BarColor, open var hintStyle: BarStyle) {
    private val activeHints: MutableMap<Player, BossBar> = HashMap()
    fun activateFor(player: Player){
        val bossBar = Bukkit.createBossBar(hintText, hintColor, hintStyle)
        bossBar.addPlayer(player)
        activeHints[player] = bossBar
    }
    fun deactivateFor(player: Player){
        if(activeHints.containsKey(player)){
            activeHints[player]!!.removeAll()
            activeHints.remove(player)
        }
    }
}