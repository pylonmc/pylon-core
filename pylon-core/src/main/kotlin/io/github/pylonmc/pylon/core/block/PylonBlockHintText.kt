package io.github.pylonmc.pylon.core.block

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player


open class PylonBlockHintText(hintText: String, hintColor: BarColor, hintStyle: BarStyle) {
    private var bossBar = Bukkit.createBossBar(hintText, hintColor, hintStyle)
    fun activateFor(player: Player){
        bossBar.addPlayer(player)
    }
    fun deactivateFor(player: Player){
        bossBar.removePlayer(player)
    }
    fun changeHintText(newHintText: String){
        bossBar.setTitle(newHintText)
    }
    fun changeHintColor(newHintColor: BarColor){
        bossBar.color = newHintColor
    }
    fun changeHintStyle(newHintStyle: BarStyle){
        bossBar.style = newHintStyle
    }
    fun getHintText(): String {
        return bossBar.title
    }
    fun getHintColor(): BarColor {
        return bossBar.color
    }
    fun getHintStyle(): BarStyle {
        return bossBar.style
    }
}