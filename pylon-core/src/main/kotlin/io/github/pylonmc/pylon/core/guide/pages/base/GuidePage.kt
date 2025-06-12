package io.github.pylonmc.pylon.core.guide.pages.base

import org.bukkit.Keyed
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.ItemProvider

interface GuidePage : Keyed {
    val item: ItemProvider
    fun open(player: Player)
}