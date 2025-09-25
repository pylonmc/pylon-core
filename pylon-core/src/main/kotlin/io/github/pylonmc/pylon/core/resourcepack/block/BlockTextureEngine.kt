package io.github.pylonmc.pylon.core.resourcepack.block

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.util.Octree
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.CoroutineStart
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
import java.time.Duration
import java.util.UUID
import kotlin.collections.Map.Entry
import kotlin.math.ceil

object BlockTextureEngine : Listener {
    const val DISABLED_PRESET = "disabled"

    val customBlockTexturesKey = pylonKey("custom_block_textures")
    val presetKey = pylonKey("culling_preset")

    private val occludingCache = mutableMapOf<ChunkPosition, ChunkData>()

    private val octrees = mutableMapOf<UUID, Octree<PylonBlock>>()
    private val jobs = mutableMapOf<UUID, Job>()

    /**
     * Periodically updates a share of the occluding cache, to ensure it stays up to date with changes in the world.
     * Every [BlockTextureConfig.occludingCacheRefreshInterval] ticks, it will refresh [BlockTextureConfig.occludingCacheRefreshShare]
     * percent of the cache, starting with the oldest entries.
     *
     * Normally, blocks occluding state is cached the first time its requested, and is only updated when placed or broken.
     * If a block changes its occluding state in any other way the cache will no longer be accurate. This job corrects that.
     */
    internal val updateOccludingCacheJob = PylonCore.launch(start = CoroutineStart.LAZY) {
        while (true) {
            delay(BlockTextureConfig.occludingCacheRefreshInterval.ticks)
            val now = System.currentTimeMillis()
            var refreshed = 0
            var toRefresh = ceil(occludingCache.size * BlockTextureConfig.occludingCacheRefreshShare)
            var entries = mutableListOf<Entry<ChunkPosition, ChunkData>>()
            entries.addAll(occludingCache.entries)
            entries.sortBy { it.value.timestamp }

            for ((pos, data) in entries) {
                if (now - data.timestamp <= BlockTextureConfig.occludingCacheRefreshInterval) continue

                val world = pos.world ?: continue
                if (world.isChunkLoaded(pos.x, pos.z)) {
                    data.timestamp = now
                    for (position in data.occluding.asMap().keys.toSet()) {
                        data.occluding.put(position, position.block.blockData.isOccluding)
                    }

                    if (++refreshed >= toRefresh) break
                } else {
                    occludingCache.remove(pos)
                }
            }
        }
    }

    @get:JvmStatic
    @set:JvmStatic
    var Player.customBlockTextures: Boolean
        get() = this.persistentDataContainer.getOrDefault(customBlockTexturesKey, PersistentDataType.BOOLEAN, true)
        set(value) = this.persistentDataContainer.set(customBlockTexturesKey, PersistentDataType.BOOLEAN, value)

    @get:JvmStatic
    @set:JvmStatic
    var Player.cullingPreset: CullingPreset
        get() = BlockTextureConfig.cullingPresets.getOrElse(this.persistentDataContainer.getOrDefault(presetKey, PersistentDataType.STRING, BlockTextureConfig.defaultCullingPreset.id)) {
            BlockTextureConfig.defaultCullingPreset
        }
        set(value) = this.persistentDataContainer.set(presetKey, PersistentDataType.STRING, value.id)

    internal fun insert(block: PylonBlock) {
        if (!BlockTextureConfig.customBlockTexturesEnabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).insert(block)
    }

    internal fun remove(block: PylonBlock) {
        if (!BlockTextureConfig.customBlockTexturesEnabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).remove(block)
        block.blockTextureEntity?.let {
            for (viewer in it.viewers.toSet()) {
                it.removeViewer(viewer)
            }
        }
    }

    internal fun getOctree(world: World): Octree<PylonBlock> {
        check(BlockTextureConfig.customBlockTexturesEnabled) { "Tried to access BlockTextureEngine octree while custom block textures are disabled" }

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
                if (preset.id == DISABLED_PRESET) {
                    val radius = player.sendViewDistance * 16 / 2.0
                    val query = octree.query(BoundingBox.of(eye, radius, radius, radius))
                    visible.toSet().subtract(query).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                    for (block in query) {
                        val entity = block.blockTextureEntity ?: continue
                        if (!entity.hasViewer(uuid)) {
                            entity.addViewer(uuid)
                            visible.add(block)
                        }
                    }
                    delay(20.ticks)
                    continue
                }

                val query = octree.query(BoundingBox.of(eye, preset.cullRadius.toDouble(), preset.cullRadius.toDouble(), preset.cullRadius.toDouble()))
                visible.toSet().subtract(query).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                for (block in query) {
                    val entity = block.blockTextureEntity ?: continue

                    val seen = entity.hasViewer(uuid)
                    val distanceSquared = block.block.location.distanceSquared(player.location)
                    if (distanceSquared <= preset.alwaysShowRadius * preset.alwaysShowRadius) {
                        entity.addViewer(uuid)
                        visible.add(block)
                    } else if (distanceSquared > preset.cullRadius * preset.cullRadius) {
                        entity.removeViewer(uuid)
                        visible.remove(block)
                    } else if ((seen && (tick % preset.visibleInterval) == 0) || (!seen && (tick % preset.hiddenInterval) == 0)) {
                        // TODO: If necessary, have a 3d scan using bounding boxes rather than a line
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
                            val occludes = occludingCache[chunkPos]?.isOccluding(position) ?: current.toLocation(world).block.blockData.isOccluding
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
        occludingCache[ChunkPosition(event.chunk)] = ChunkData(System.currentTimeMillis())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockPlace(event: BlockPlaceEvent) {
        occludingCache[ChunkPosition(event.block)]?.occluding?.put(BlockPosition(event.block), event.block.blockData.isOccluding)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockBreak(event: BlockBreakEvent) {
        occludingCache[ChunkPosition(event.block)]?.occluding?.put(BlockPosition(event.block), false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockBreak(event: PylonBlockBreakEvent) {
        // TODO: Delete this when pylon no longer cancels BlockBreakEvents for PylonBlocks
        occludingCache[ChunkPosition(event.block)]?.occluding?.put(BlockPosition(event.block), false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        occludingCache.remove(ChunkPosition(event.chunk))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        jobs.remove(event.player.uniqueId)?.cancel()
    }

    private data class ChunkData(
        var timestamp: Long,
        val occluding: LoadingCache<BlockPosition, Boolean> = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(1))
            .build { pos -> pos.block.blockData.isOccluding },
    ) {
        fun isOccluding(position: BlockPosition): Boolean {
            return occluding.get(position)
        }
    }
}
