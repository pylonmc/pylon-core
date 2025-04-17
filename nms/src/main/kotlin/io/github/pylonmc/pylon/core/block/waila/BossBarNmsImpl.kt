package io.github.pylonmc.pylon.core.block.waila

import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import org.bukkit.boss.BossBar
import org.bukkit.craftbukkit.boss.CraftBossBar

object BossBarNmsImpl : BossBarNms {

    override fun setTitle(bar: BossBar, title: Component) {
        val craftBar = bar as CraftBossBar
        craftBar.handle.name = PaperAdventure.asVanilla(title)
        craftBar.handle.broadcast(ClientboundBossEventPacket::createUpdateNamePacket)
    }

    override fun getTitle(bar: BossBar): Component {
        val craftBar = bar as CraftBossBar
        return PaperAdventure.asAdventure(craftBar.handle.name)
    }
}