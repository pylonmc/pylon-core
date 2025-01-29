package io.github.pylonmc.pylon.core.block

import org.bukkit.boss.BossBar

interface HintText {
    fun getHint() : BossBar
}