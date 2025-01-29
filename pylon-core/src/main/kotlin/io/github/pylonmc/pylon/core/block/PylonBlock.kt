package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.PylonDataReader
import io.github.pylonmc.pylon.core.persistence.PylonDataWriter
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.annotation.DoNotUse
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

open class PylonBlock<S: PylonBlockSchema>private constructor(val schema: S, val block: Block, open val blockHint: BossBar) : HintText {

    constructor(reader: PylonDataReader, block: Block)
            : this(getSchemaOfType<S>(reader.id), block, Bukkit.createBossBar(block.type.toString(), BarColor.RED, BarStyle.SOLID))
    constructor(schema: S, block: Block) : this(schema, block, Bukkit.createBossBar(block.type.toString(), BarColor.RED, BarStyle.SOLID))

    fun write(writer: PylonDataWriter) {}

    // TODO listener
    fun tick() {}

    // TODO listener
    fun onRightClick() {}

    // TODO listener
    fun onBreak() {}

    override fun getHint() : BossBar{
        return blockHint
    }

    companion object {
        /*
         * Convenience function to use in the (StateReader, Block) constructor
         */
        private fun <S : PylonBlockSchema> getSchemaOfType(key: NamespacedKey): S {
            val schema = PylonRegistry.BLOCKS.getOrThrow(key)

            // Dealing with deserialization, so not really any way around this
            @Suppress("UNCHECKED_CAST")
            return schema as S
        }
    }
}