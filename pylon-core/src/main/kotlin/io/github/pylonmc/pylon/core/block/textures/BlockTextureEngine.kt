package io.github.pylonmc.pylon.core.block.textures

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.util.Octree
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.collections.Map.Entry
import kotlin.math.ceil

object BlockTextureEngine : Listener {
    private val presetKey = pylonKey("culling_preset")

    private val chunkData = mutableMapOf<ChunkPosition, ChunkData>()

    private val octrees = mutableMapOf<UUID, Octree<PylonBlock>>()
    private val jobs = mutableMapOf<UUID, Job>()

    internal object UpdateSnapshotTask : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            var refreshed = 0
            var toRefresh = ceil(chunkData.size * PylonConfig.cullingSnapshotRefreshShare)
            var entries = mutableListOf<Entry<ChunkPosition, ChunkData>>()
            entries.addAll(chunkData.entries)
            entries.sortBy { it.value.timestamp }

            for ((pos, data) in entries) {
                if (now - data.timestamp <= PylonConfig.cullingSnapshotRefreshInterval) continue

                val world = pos.world ?: continue
                if (world.isChunkLoaded(pos.x, pos.z)) {
                    for (position in data.occluding.keys.toSet()) {
                        data.occluding[position] = position.block.blockData.isOccluding
                    }

                    if (++refreshed >= toRefresh) break
                }
            }
        }
    }

    @get:JvmStatic
    @set:JvmStatic
    var Player.customBlockTextures: Boolean
        get() = this.persistentDataContainer.getOrDefault(pylonKey("custom_block_textures"), PersistentDataType.BOOLEAN, true)
        set(value) = this.persistentDataContainer.set(pylonKey("custom_block_textures"), PersistentDataType.BOOLEAN, value)

    @get:JvmStatic
    @set:JvmStatic
    var Player.cullingPreset: CullingPreset
        get() = PylonConfig.cullingPresets.getOrElse(this.persistentDataContainer.getOrDefault(presetKey, PersistentDataType.STRING, PylonConfig.defaultCullingPreset.id)) {
            PylonConfig.cullingPresets["off"]!!
        }
        set(value) = this.persistentDataContainer.set(presetKey, PersistentDataType.STRING, value.id)

    fun insert(block: PylonBlock) {
        if (!PylonConfig.customBlockTexturesEnabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).insert(block)
    }

    fun remove(block: PylonBlock) {
        if (!PylonConfig.customBlockTexturesEnabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).remove(block)
    }

    fun getOctree(world: World): Octree<PylonBlock> {
        if (!PylonConfig.customBlockTexturesEnabled) return DummyOctree

        val border = world.worldBorder
        return octrees.getOrPut(world.uid) {
            Octree(
                bounds = BoundingBox.of(
                    Vector(border.center.x - border.size / 2, world.minHeight.toDouble(), border.center.z - border.size / 2),
                    Vector(border.center.x + border.size / 2, world.maxHeight.toDouble(), border.center.z + border.size / 2)
                ),
                depth = 0,
                entryStrategy = { BoundingBox.of(it.block) }
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        jobs[uuid] = PylonCore.launch(PylonCore.asyncDispatcher) {
            val visible = mutableSetOf<PylonBlock>()
            var tick = 0
            while (true) {
                val player = Bukkit.getPlayer(uuid)
                if (player == null) {
                    visible.forEach { it.blockTextureEntity?.removeViewer(uuid) }
                    visible.clear()
                    break
                }

                if (!player.customBlockTextures) {
                    visible.forEach { it.blockTextureEntity?.removeViewer(uuid) }
                    visible.clear()
                    delay(20.ticks)
                    continue
                }

                val world = player.world
                val eye = player.eyeLocation.toVector()
                val preset = player.cullingPreset
                val octree = getOctree(world)
                if (preset.id == "off") {
                    val radius = player.sendViewDistance * 16 / 2.0
                    val query = octree.query(BoundingBox.of(eye, radius, radius, radius))
                    visible.toSet().subtract(query).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                    for (block in query) {
                        if (!visible.contains(block)) {
                            block.blockTextureEntity?.addViewer(uuid)
                            visible.add(block)
                        }
                    }
                    delay(20.ticks)
                    continue
                }

                val query = octree.query(BoundingBox.of(eye, preset.cullRadius.toDouble(), preset.cullRadius.toDouble(), preset.cullRadius.toDouble()))
                visible.toSet().subtract(query).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                for (block in query) {
                    val entity = block.blockTextureEntity
                    if (entity == null) continue

                    val seen = entity.hasViewer(uuid)
                    val distance = block.block.location.distanceSquared(player.location)
                    if (distance <= preset.alwaysShowRadius * preset.alwaysShowRadius) {
                        entity.addViewer(uuid)
                        visible.add(block)
                    } else if (distance > preset.cullRadius * preset.cullRadius) {
                        entity.removeViewer(uuid)
                        visible.remove(block)
                    } else if ((seen && (tick % preset.visibleInterval) == 0) || (!seen && (tick % preset.hiddenInterval) == 0)) {
                        // TODO: If necessary, have a 3d scan rather than a line
                        var occluding = 0
                        val end = block.block.location.toCenterLocation().toVector()
                        val totalDistance = eye.distanceSquared(end)
                        val current = eye.clone()
                        val direction = end.clone().subtract(eye).normalize()
                        while (current.distanceSquared(eye) < totalDistance) {
                            current.add(direction)
                            if (current.distanceSquared(eye) > totalDistance) {
                                current.copy(end)
                            }

                            val position = BlockPosition(world, current)
                            val chunkPos = position.chunk
                            val occludes = chunkData[chunkPos]?.isOccluding(position) ?: current.toLocation(world).block.blockData.isOccluding
                            if (occludes && ++occluding > preset.maxOccludingCount) {
                                break
                            }
                        }

                        val shouldSee = occluding <= preset.maxOccludingCount
                        if (shouldSee && !seen) {
                            entity.addViewer(uuid)
                            visible.add(block)
                        } else if (!shouldSee && seen) {
                            entity.removeViewer(uuid)
                            visible.remove(block)
                        }
                    }
                }

                delay(1.ticks)
                tick++
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkLoad(event: ChunkLoadEvent) {
        chunkData[ChunkPosition(event.chunk)] = ChunkData(
            timestamp = System.currentTimeMillis(),
            occluding = mutableMapOf()
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockPlace(event: BlockPlaceEvent) {
        chunkData[ChunkPosition(event.block)]?.occluding[BlockPosition(event.block)] = event.block.blockData.isOccluding
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockBreak(event: BlockBreakEvent) {
        chunkData[ChunkPosition(event.block)]?.occluding[BlockPosition(event.block)] = false
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockBreak(event: PylonBlockBreakEvent) {
        // TODO: Delete this when pylon no longer cancels BlockBreakEvents for PylonBlocks
        chunkData[ChunkPosition(event.block)]?.occluding[BlockPosition(event.block)] = false
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        chunkData.remove(ChunkPosition(event.chunk))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        jobs.remove(event.player.uniqueId)?.cancel()
    }

    data class ChunkData(
        val timestamp: Long,
        val occluding: MutableMap<BlockPosition, Boolean>
    ) {
        fun isOccluding(position: BlockPosition): Boolean {
            return occluding.getOrPut(position) {
                position.block.blockData.isOccluding
            }
        }
    }

    private object DummyOctree : Octree<PylonBlock>(
        bounds = BoundingBox(),
        depth = 0,
        entryStrategy = { BoundingBox.of(it.block) }
    ) {
        override fun insert(entry: PylonBlock): Boolean = false
        override fun remove(entry: PylonBlock): Boolean = false
    }
}
