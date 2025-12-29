package io.github.pylonmc.pylon.core.resourcepack.block

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.util.Octree
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.time.Duration
import java.util.*
import kotlin.collections.Map.Entry
import kotlin.math.ceil

object BlockTextureEngine : Listener {
    const val DISABLED_PRESET = "disabled"

    val customBlockTexturesKey = pylonKey("custom_block_textures")
    val presetKey = pylonKey("culling_preset")

    private val occludingCache = mutableMapOf<UUID, MutableMap<Long, ChunkData>>()

    private val octrees = mutableMapOf<UUID, Octree<PylonBlock>>()
    private val jobs = mutableMapOf<UUID, Job>()

    /**
     * Periodically invalidates a share of the occluding cache, to ensure stale data isn't perpetuated.
     * Every [PylonConfig.BlockTextureConfig.occludingCacheRefreshInterval] ticks, it will invalidate [PylonConfig.BlockTextureConfig.occludingCacheRefreshShare]
     * percent of the cache, starting with the oldest entries.
     *
     * Normally, blocks occluding state is cached the first time its requested, and is only updated when placed or broken.
     * If a block changes its occluding state in any other way the cache will no longer be accurate. This job corrects that.
     */
    @JvmSynthetic
    internal val updateOccludingCacheJob = PylonCore.launch(start = CoroutineStart.LAZY) {
        while (true) {
            delay(PylonConfig.BlockTextureConfig.occludingCacheRefreshInterval.ticks)
            val now = System.currentTimeMillis()
            for ((worldId, chunkMap) in occludingCache) {
                var refreshed = 0
                var toRefresh = ceil(chunkMap.size * PylonConfig.BlockTextureConfig.occludingCacheRefreshShare)
                var entries = mutableListOf<Entry<Long, ChunkData>>()
                entries.addAll(chunkMap.entries)
                entries.sortBy { it.value.timestamp }

                for ((chunkKey, data) in entries) {
                    if (now - data.timestamp <= PylonConfig.BlockTextureConfig.occludingCacheRefreshInterval) continue

                    val world = Bukkit.getWorld(worldId) ?: continue
                    if (world.isChunkLoaded(chunkKey.toInt(), (chunkKey shr 32).toInt())) {
                        data.timestamp = now
                        data.occluding.cleanUp()
                        data.occluding.invalidateAll()
                        if (++refreshed >= toRefresh) break
                    } else {
                        chunkMap.remove(chunkKey)
                    }
                }
            }
        }
    }

    @JvmStatic
    var Player.hasCustomBlockTextures: Boolean
        get() = (this.persistentDataContainer.getOrDefault(customBlockTexturesKey, PersistentDataType.BOOLEAN, PylonConfig.BlockTextureConfig.default) || PylonConfig.BlockTextureConfig.forced)
        set(value) = this.persistentDataContainer.set(customBlockTexturesKey, PersistentDataType.BOOLEAN, value || PylonConfig.BlockTextureConfig.forced)

    @JvmStatic
    var Player.cullingPreset: CullingPreset
        get() = PylonConfig.BlockTextureConfig.cullingPresets.getOrElse(this.persistentDataContainer.getOrDefault(presetKey, PersistentDataType.STRING, PylonConfig.BlockTextureConfig.defaultCullingPreset.id)) {
            PylonConfig.BlockTextureConfig.defaultCullingPreset
        }
        set(value) = this.persistentDataContainer.set(presetKey, PersistentDataType.STRING, value.id)

    @JvmSynthetic
    internal fun insert(block: PylonBlock) {
        if (!PylonConfig.BlockTextureConfig.enabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).insert(block)
    }

    @JvmSynthetic
    internal fun remove(block: PylonBlock) {
        if (!PylonConfig.BlockTextureConfig.enabled || block.disableBlockTextureEntity) return
        getOctree(block.block.world).remove(block)
        block.blockTextureEntity?.let {
            for (viewer in it.viewers.toSet()) {
                it.removeViewer(viewer)
            }
        }
    }

    @JvmSynthetic
    internal fun getOctree(world: World): Octree<PylonBlock> {
        check(PylonConfig.BlockTextureConfig.enabled) { "Tried to access BlockTextureEngine octree while custom block textures are disabled" }

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

    @JvmSynthetic
    internal fun launchBlockTextureJob(player: Player) {
        val uuid = player.uniqueId
        if (!PylonConfig.BlockTextureConfig.enabled || !player.hasCustomBlockTextures || jobs.containsKey(uuid)) return

        jobs[uuid] = PylonCore.launch(PylonCore.asyncDispatcher) {
            val visible = mutableSetOf<PylonBlock>()
            var tick = 0

            while (true) {
                val player = Bukkit.getPlayer(uuid)
                if (player == null || !player.hasCustomBlockTextures) {
                    octrees.values.forEach { it.forEach { b -> b.blockTextureEntity?.removeViewer(uuid) } }
                    jobs.remove(uuid)
                    break
                }

                // When showing/hiding entities, we will always add/remove the viewer and add/remove the block from the visible set
                // because in some edge cases, the visible set and the actual viewers can get out of sync

                val world = player.world
                val occludingCache = occludingCache.getOrPut(world.uid) { mutableMapOf() }

                val location = player.location
                val eye = player.eyeLocation.toVector()
                val preset = player.cullingPreset
                val octree = getOctree(world)
                if (preset.id == DISABLED_PRESET) {
                    // Send all block entities within view distance and hide all others
                    val radius = player.sendViewDistance * 16 / 2.0
                    val query = octree.query(BoundingBox.of(eye, radius, radius, radius))
                    visible.toSet().subtract(query.toSet()).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                    for (block in query) {
                        val entity = block.blockTextureEntity ?: continue
                        val distanceSquared = block.block.location.distanceSquared(location)
                        entity.addOrRefreshViewer(uuid, distanceSquared)
                        visible.add(block)
                    }
                    delay(preset.updateInterval.ticks)
                    continue
                }

                // Query all possibly visible blocks within cull radius, and hide all others
                val query = octree.query(BoundingBox.of(eye, preset.cullRadius.toDouble(), preset.cullRadius.toDouble(), preset.cullRadius.toDouble()))
                visible.toSet().subtract(query.toSet()).forEach { it.blockTextureEntity?.removeViewer(uuid) }

                for (block in query) {
                    val entity = block.blockTextureEntity ?: continue

                    // If we are within the always show radius, show, if we are outside cull radius, hide
                    // (our query is a cube not a sphere, so blocks in the corners can still be outside the cull radius)
                    val seen = entity.hasViewer(uuid)
                    val distanceSquared = block.block.location.distanceSquared(location)
                    if (distanceSquared <= preset.alwaysShowRadius * preset.alwaysShowRadius) {
                        entity.addOrRefreshViewer(uuid, distanceSquared)
                        visible.add(block)
                        continue
                    } else if (distanceSquared > preset.cullRadius * preset.cullRadius) {
                        entity.removeViewer(uuid)
                        visible.remove(block)
                        continue
                    }

                    // If its visible & we are on a visibleInterval tick, or if its hidden & we are on a hiddenInterval tick, do a culling check
                    if ((seen && (tick % preset.visibleInterval) == 0) || (!seen && (tick % preset.hiddenInterval) == 0)) {
                        // TODO: Later if necessary, have a 3d scan using bounding boxes rather than a line
                        // Ray traces from the players eye to the center of the block, counting occluding blocks in between
                        // if its greater than the maxOccludingCount, hide the entity, otherwise show it
                        var occluding = 0
                        val end = Vector(block.block.x + 0.5, block.block.y + 0.5, block.block.z + 0.5)
                        val totalDistance = eye.distanceSquared(end)
                        val current = eye.clone()
                        val direction = end.clone().subtract(eye).normalize()
                        while (current.distanceSquared(eye) < totalDistance) {
                            current.add(direction)
                            if (current.distanceSquared(eye) > totalDistance) {
                                current.copy(end)
                            }

                            val x = current.blockX
                            val y = current.blockY
                            val z = current.blockZ
                            val chunkPos = Chunk.getChunkKey(x shr 4, z shr 4)
                            val occludes = occludingCache.getOrPut(chunkPos) { ChunkData() }.isOccluding(world, x, y, z)
                            if (occludes && ++occluding > preset.maxOccludingCount) {
                                break
                            }
                        }

                        val shouldSee = occluding <= preset.maxOccludingCount
                        if (shouldSee) {
                            entity.addOrRefreshViewer(uuid, distanceSquared)
                            visible.add(block)
                        } else {
                            entity.removeViewer(uuid)
                            visible.remove(block)
                        }
                    }
                }

                delay(preset.updateInterval.ticks)
                tick++
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        launchBlockTextureJob(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onWorldLoad(event: WorldLoadEvent) {
        occludingCache[event.world.uid] = mutableMapOf()
        getOctree(event.world)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkLoad(event: ChunkLoadEvent) {
        occludingCache[event.world.uid]?.set(event.chunk.chunkKey, ChunkData())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockPlace(event: BlockPlaceEvent) {
        occludingCache[event.block.world.uid]?.get(event.block.chunk.chunkKey)?.insert(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockBreak(event: BlockBreakEvent) {
        occludingCache[event.block.world.uid]?.get(event.block.chunk.chunkKey)?.insert(event.block, false)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockDestroy(event: BlockDestroyEvent) {
        occludingCache[event.block.world.uid]?.get(event.block.chunk.chunkKey)?.insert(event.block, false)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockExplode(event: BlockExplodeEvent) {
        val cache = occludingCache[event.block.world.uid] ?: return
        for (block in event.blockList()) {
            cache[block.chunk.chunkKey]?.insert(block, false)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityExplode(event: EntityExplodeEvent) {
        val cache = occludingCache[event.entity.world.uid] ?: return
        for (block in event.blockList()) {
            cache[block.chunk.chunkKey]?.insert(block, false)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        occludingCache[event.world.uid]?.remove(event.chunk.chunkKey)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onWorldUnload(event: WorldUnloadEvent) {
        occludingCache.remove(event.world.uid)
        octrees.remove(event.world.uid)
    }

    private data class ChunkData(
        var timestamp: Long = System.currentTimeMillis(),
        val occluding: Cache<Long, Boolean> = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(1))
            .build()
    ) {
        fun insert(block: Block, isOccluding: Boolean = block.blockData.isOccluding) {
            val key = BlockPosition.asLong(block.x, block.y, block.z)
            occluding.put(key, isOccluding)
        }

        fun isOccluding(world: World, blockX: Int, blockY: Int, blockZ: Int): Boolean {
            val key = BlockPosition.asLong(blockX, blockY, blockZ)
            return occluding.get(key) {
                world.getBlockAt(blockX, blockY, blockZ).blockData.isOccluding
            }
        }
    }
}
