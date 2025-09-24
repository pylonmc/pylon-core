package io.github.pylonmc.pylon.core.block.context

import io.github.pylonmc.pylon.core.block.PylonBlock.Companion.pylonBlock
import io.github.pylonmc.pylon.core.block.base.CanExplodeBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import org.bukkit.block.Block
import java.util.concurrent.ThreadLocalRandom

abstract class BlockExplodeContext(val block: Block) : BlockBreakContext {
    override val normallyDrops: Boolean
        get() {
            // by now we are sure that block.pylonBlock is true
            val pylonBlock = block.pylonBlock!!

            // configurable by each pylon block
            if (pylonBlock is CanExplodeBlock) return pylonBlock.willExplode(this)

            // if not found, process a default
            return ThreadLocalRandom.current().nextDouble() < PylonConfig.defaultExplosionDropChance
        }
}
