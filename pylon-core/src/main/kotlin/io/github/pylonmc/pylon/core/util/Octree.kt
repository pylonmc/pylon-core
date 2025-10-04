package io.github.pylonmc.pylon.core.util

import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.concurrent.CopyOnWriteArrayList

open class Octree<N>(
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,

    private val bounds: BoundingBox,
    private val depth: Int,
    private val entryStrategy: (N) -> BoundingBox,
) {
    private var entries: MutableList<N> = CopyOnWriteArrayList()
    private var children: Array<Octree<N>>? = null

    constructor(bounds: BoundingBox, depth: Int, entryStrategy: (N) -> BoundingBox) : this(
        DEFAULT_MAX_DEPTH,
        DEFAULT_MAX_ENTRIES,
        bounds,
        depth,
        entryStrategy
    )

    open fun insert(entry: N) : Boolean {
        val entryBounds = entryStrategy(entry)
        if (!bounds.overlaps(entryBounds)) return false

        if (children != null) {
            for (child in children!!) {
                if (child.bounds.overlaps(entryBounds)) {
                    return child.insert(entry)
                }
            }
        }

        if (entries.size < DEFAULT_MAX_ENTRIES || depth >= DEFAULT_MAX_DEPTH) {
            entries.add(entry)
            return true
        }

        subdivide()
        return insert(entry)
    }

    open fun remove(entry: N): Boolean {
        val entryBounds = entryStrategy(entry)
        if (!bounds.overlaps(entryBounds)) return false

        if (entries.remove(entry)) {
            return true
        }

        if (children != null) {
            for (child in children!!) {
                if (child.bounds.overlaps(entryBounds)) {
                    if (child.remove(entry)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    open fun clear() {
        entries.clear()
        children?.forEach { it.clear() }
        children = null
    }

    private fun subdivide() {
        val min = bounds.min
        val max = bounds.max
        val center = min.clone().add(max).multiply(0.5)

        children = Array(8) { i ->
            val dx = (i shr 2) and 1
            val dy = (i shr 1) and 1
            val dz = i and 1
            val childMin = Vector(
                if (dx == 0) min.getX() else center.getX(),
                if (dy == 0) min.getY() else center.getY(),
                if (dz == 0) min.getZ() else center.getZ()
            )
            val childMax = Vector(
                if (dx == 0) center.getX() else max.getX(),
                if (dy == 0) center.getY() else max.getY(),
                if (dz == 0) center.getZ() else max.getZ()
            )
            Octree(BoundingBox.of(childMin, childMax), depth + 1, entryStrategy)
        }

        for (entry in entries) {
            for (child in children!!) {
                if (child.bounds.overlaps(entryStrategy(entry))) {
                    child.insert(entry)
                    break
                }
            }
        }
        entries.clear()
    }

    fun query(range: BoundingBox): List<N> {
        val result = mutableListOf<N>()
        if (!bounds.overlaps(range)) return result

        for (entry in entries) {
            if (range.overlaps(entryStrategy(entry))) {
                result.add(entry)
            }
        }

        if (children != null) {
            for (child in children!!) {
                result.addAll(child.query(range))
            }
        }

        return result
    }

    fun maxDepth(): Int {
        return children?.maxOfOrNull { it.maxDepth() } ?: depth
    }

    companion object {
        // LISTEN IF THESE ARE INSANE FIX THEM, THIS ISN'T MY STRONG SUIT - JustAHuman
        private const val DEFAULT_MAX_DEPTH = 2048
        private const val DEFAULT_MAX_ENTRIES = 128
    }
}