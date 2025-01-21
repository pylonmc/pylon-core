package io.github.pylonmc.pylon.core.test

import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

internal object BedrockWorldGenerator : ChunkGenerator() {
    override fun generateBedrock(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        for (x in 0..15) {
            for (z in 0..15) {
                chunkData.setBlock(x, -1, z, Material.BEDROCK)
            }
        }
    }
}