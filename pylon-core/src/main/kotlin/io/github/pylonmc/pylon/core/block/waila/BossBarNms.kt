@file:JvmSynthetic // hide extensions from Java

package io.github.pylonmc.pylon.core.block.waila

import net.kyori.adventure.text.Component
import org.bukkit.boss.BossBar
import org.jetbrains.annotations.ApiStatus

/**
 * This exists because, while Minecraft uses components in boss bar titles,
 * the Paper API does not allow you to use components
 */
@ApiStatus.NonExtendable
interface BossBarNms {

    fun setTitle(bar: BossBar, title: Component)
    fun getTitle(bar: BossBar): Component

    companion object {
        val instance = Class.forName("io.github.pylonmc.pylon.core.block.waila.BossBarNmsImpl")
            .getDeclaredField("INSTANCE")
            .get(null) as BossBarNms
    }
}

var BossBar.titleComponent: Component
    get() = BossBarNms.instance.getTitle(this)
    set(value) = BossBarNms.instance.setTitle(this, value)