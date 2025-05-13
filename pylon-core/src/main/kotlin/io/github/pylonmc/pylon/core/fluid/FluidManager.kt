package io.github.pylonmc.pylon.core.fluid

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.base.PylonFluidBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PrePylonFluidPointConnectEvent
import io.github.pylonmc.pylon.core.event.PrePylonFluidPointDisconnectEvent
import io.github.pylonmc.pylon.core.event.PylonFluidPointConnectEvent
import io.github.pylonmc.pylon.core.event.PylonFluidPointDisconnectEvent
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.*
import java.util.function.Predicate
import kotlin.math.min

/**
 * Fluid networks are organised into 'segments' which is just a set of connected points. Each
 * segment is created at runtime - they are not persisted. When a point is loaded, we initialise
 * it with its own segment, check whether it's connected to any other points, and if so, join all
 * those connected points (and their connected points, and so on) into one segment.
 *
 * Example: Imagine we have
 * A------B------C------D
 * A is loaded first, then C, then D. This means that A and C are on their own segment to start with.
 * Then, D is loaded. D sees that C is connected and loaded, so C and D join together into one
 * segment. Then, B loads, and sees that A and C are connected, so A, B, C, and D join together into
 * one segment.
 *
 * This may seem convoluted, but it's the best (and only) way that I could find to deal with the
 * utter chaos that is chunk loading/unloading. Other models usually break when you try to modify a
 * partially unloaded fluid network.
 *
 * FUTURE OPTIMISATION POINTS:
 * - Use disjoint sets data structure to reduce overhead of connecting/disconnecting and getting
 *   connected nodes
 * - We currently use round-robin for input and output, this would be more efficient to do first-come-
 *   first-serve. The current algorithm could also be improved to get better performance I think
 * - There is some indirection in getting requested fluids when ticking; we might be able to reduce
 *   the performance overhead here with a different program flow but needs testing
 * - Currently not asynchronous, I think parts of this can definitely be made asynchronous
 */
object FluidManager {

    private class Segment(
        val points: MutableSet<FluidConnectionPoint> = mutableSetOf(),
        var fluidPerTick: Long = Long.MAX_VALUE,
        var predicate: Predicate<PylonFluid>? = null,
    )

    /**
     * A point is just a connection in a fluid network, like a machine's output or the end of a pipe
     */
    private val points: MutableMap<UUID, FluidConnectionPoint> = mutableMapOf()

    /**
     * A segment is a collection of connection points
     */
    private val segments: MutableMap<UUID, Segment> = mutableMapOf()

    /**
     * Each segment has a corresponding ticker
     */
    private val tickers: MutableMap<UUID, Job> = mutableMapOf()

    /**
     * Adds the point to its stored segment, creating the segment and starting a ticker for it if it does not exist
     */
    private fun addToSegment(point: FluidConnectionPoint) {
        if (point.segment !in segments) {
            segments[point.segment] = Segment()
            startTicker(point.segment)
        }
        segments[point.segment]!!.points.add(point)
    }

    /**
     * Removes the point from its segment, deleting the segment and cancelling the ticker if the segment is
     * now empty
     */
    private fun removeFromSegment(point: FluidConnectionPoint) {
        segments[point.segment]!!.points.remove(point)
        if (segments[point.segment]!!.points.isEmpty()) {
            segments.remove(point.segment)
            tickers[point.segment]!!.cancel()
        }
    }

    /**
     * Call when creating a new connection point, or when one has been loaded
     */
    @JvmStatic
    fun add(point: FluidConnectionPoint) {
        check(point.id !in points) { "Duplicate connection point" }

        points[point.id] = point

        addToSegment(point)

        for (otherPointId in point.connectedPoints) {
            points[otherPointId]?.let {
                connect(point, it)
            }
        }
    }

    /**
     * Call when removing a connection point, or when one has been unloaded
     */
    @JvmStatic
    fun remove(point: FluidConnectionPoint) {
        check(point.id in points) { "Nonexistant connection point" }

        // Clone to prevent ConcurrentModificationException; disconnect modifies point.connectedPoints
        for (otherPointId in point.connectedPoints.toSet()) {
            points[otherPointId]?.let {
                disconnect(point, it)
            }
        }

        removeFromSegment(point)

        points.remove(point.id)
    }

    /**
     * Sets the flow rate per tick for a segment. The segment will not transfer more fluid than the
     * flow rate per tick.
     *
     * Preserved across disconnects and connects (when connecting two points, one of the two points
     * being connected is selected, and its segment's flow rate is copied to the new segment).
     */
    @JvmStatic
    fun setFluidPerTick(segment: UUID, fluidPerTick: Long) {
        check(segment in segments) { "Segment does not exist" }
        segments[segment]!!.fluidPerTick = fluidPerTick
    }

    /**
     * Sets the fluid predicate for a segment. The segment will only transfer fluids that match the
     * predicate.
     *
     * Preserved across disconnects and connects (when connecting two points, one of the two points
     * being connected is selected, and its segment's predicate is copied to the new segment).
     */
    @JvmStatic
    fun setFluidPredicate(segment: UUID, predicate: Predicate<PylonFluid>) {
        check(segment in segments) { "Segment does not exist" }
        segments[segment]!!.predicate = predicate
    }

    /**
     * Connects two points - and all their connected points - into one segment
     */
    @JvmStatic
    fun connect(point1: FluidConnectionPoint, point2: FluidConnectionPoint) {
        check(point1.segment in segments) { "Attempt to connect a nonexistant segment" }
        check(point2.segment in segments) { "Attempt to connect a nonexistant segment" }

        if (!PrePylonFluidPointConnectEvent(point1, point2).callEvent()) {
            return
        }

        point1.connectedPoints.add(point2.id)
        point2.connectedPoints.add(point1.id)

        if (point1.segment != point2.segment) {
            val newSegment = point2.segment
            for (point in getAllConnected(point1)) {
                removeFromSegment(point)
                point.segment = newSegment
                addToSegment(point)
            }
        }

        PylonFluidPointConnectEvent(point1, point2).callEvent()
    }

    /**
     * Disconnects two points, potentially splitting them into two segments if there is no
     * other link between them.
     */
    @JvmStatic
    fun disconnect(point1: FluidConnectionPoint, point2: FluidConnectionPoint) {
        check(point1.segment in segments) { "Attempt to disconnect a nonexistant segment" }
        check(point2.segment in segments) { "Attempt to disconnect a nonexistant segment" }
        check(point2.id in point1.connectedPoints) { "Attempt to disconnect two points that are not connected" }
        check(point1.id in point2.connectedPoints) { "Attempt to disconnect two points that are not connected" }

        if (!PrePylonFluidPointDisconnectEvent(point1, point2).callEvent()) {
            return
        }

        point1.connectedPoints.remove(point2.id)
        point2.connectedPoints.remove(point1.id)

        val connectedToPoint1 = getAllConnected(point1)
        if (point2 !in connectedToPoint1) {
            // points are still (indirectly) connected
            val newSegment = UUID.randomUUID()
            segments[newSegment] = Segment(
                mutableSetOf(),
                segments[point1.segment]!!.fluidPerTick,
                segments[point1.segment]!!.predicate
            )
            startTicker(newSegment)
            for (point in connectedToPoint1) {
                removeFromSegment(point)
            }
            for (point in connectedToPoint1) {
                point.segment = newSegment
                addToSegment(point)
            }
        }

        PylonFluidPointDisconnectEvent(point1, point2).callEvent()
    }

    /**
     * Recursively gets all the points connected to another point *that are loaded*
     */
    @JvmStatic
    fun getAllConnected(point: FluidConnectionPoint): Set<FluidConnectionPoint> {
        val visitedPoints: MutableSet<FluidConnectionPoint> = mutableSetOf()
        val pointsToVisit: MutableList<FluidConnectionPoint> = mutableListOf(point)
        while (pointsToVisit.isNotEmpty()) {
            val nextPoint = pointsToVisit.removeFirst()
            visitedPoints.add(nextPoint)
            for (uuid in nextPoint.connectedPoints) {
                if (points[uuid] != null && points[uuid] !in visitedPoints) {
                    pointsToVisit.add(points[uuid]!!)
                }
            }
        }
        return visitedPoints
    }

    @JvmStatic
    fun getPoints(segment: UUID, type: FluidConnectionPoint.Type): List<FluidConnectionPoint>
        = segments[segment]!!.points.filter { it.type == type }

    /**
     * A temporary representation of a block supplying a specific fluid. Exists to make ticking logic nicer.
     */
    data class FluidSupplier(val block: PylonFluidBlock, val name: String, val fluid: PylonFluid, val amount: Long)

    /**
     * A temporary representation of a block requesting a specific fluid. Exists to make ticking logic nicer.
     */
    data class FluidRequester(val block: PylonFluidBlock, val name: String, val fluid: PylonFluid, val amount: Long)

    @JvmStatic
    fun getSuppliedFluids(point: FluidConnectionPoint): Set<FluidSupplier> {
        check(point.type == FluidConnectionPoint.Type.OUTPUT) { "Can only get supplied fluids of output point" }

        val block: PylonFluidBlock
        val blockSuppliedFluids: Map<PylonFluid, Long>
        try {
            if (!point.position.chunk.isLoaded) {
                return setOf()
            }
            block = BlockStorage.getAs<PylonFluidBlock>(point.position) ?: return setOf()
            blockSuppliedFluids = block.getSuppliedFluids(point.name)
        } catch (t: Throwable) {
            t.printStackTrace()
            return setOf()
        }

        val suppliedFluids: MutableSet<FluidSupplier> = mutableSetOf()
        for ((fluid, amount) in blockSuppliedFluids) {
            if (amount != 0L) {
                suppliedFluids.add(FluidSupplier(block, point.name, fluid, amount * PylonConfig.fluidIntervalTicks))
            }
        }
        return suppliedFluids
    }

    @JvmStatic
    fun getSuppliedFluids(segment: UUID): Map<PylonFluid, Set<FluidSupplier>> {
        val suppliedFluids: MutableMap<PylonFluid, MutableSet<FluidSupplier>> = mutableMapOf()
        for (point in getPoints(segment, FluidConnectionPoint.Type.OUTPUT)) {
            for (supplier in getSuppliedFluids(point)) {
                suppliedFluids.getOrPut(supplier.fluid, ::mutableSetOf).add(supplier)
            }
        }
        return suppliedFluids
    }

    @JvmStatic
    fun getRequestedFluids(point: FluidConnectionPoint): Set<FluidRequester> {
        check(point.type == FluidConnectionPoint.Type.INPUT) { "Can only get requested fluids of input point" }

        val block: PylonFluidBlock
        val blockRequestedFluids: Map<PylonFluid, Long>
        try {
            if (!point.position.chunk.isLoaded) {
                return setOf()
            }
            block = BlockStorage.getAs<PylonFluidBlock>(point.position) ?: return setOf()
            blockRequestedFluids = block.getRequestedFluids(point.name)
        } catch (t: Throwable) {
            t.printStackTrace()
            return setOf()
        }

        // Create requester for each request
        val requestedFluids: MutableSet<FluidRequester> = mutableSetOf()
        for ((fluid, amount) in blockRequestedFluids) {
            if (amount != 0L) {
                requestedFluids.add(FluidRequester(block, point.name, fluid, amount * PylonConfig.fluidIntervalTicks))
            }
        }
        return requestedFluids
    }

    @JvmStatic
    fun getRequestedFluids(segment: UUID): Map<PylonFluid, Set<FluidRequester>> {
        val requestedFluids: MutableMap<PylonFluid, MutableSet<FluidRequester>> = mutableMapOf()
        for (point in getPoints(segment, FluidConnectionPoint.Type.INPUT)) {
            for (requester in getRequestedFluids(point)) {
                requestedFluids.getOrPut(requester.fluid, ::mutableSetOf).add(requester)
            }
        }
        return requestedFluids
    }

    private fun tick(segment: UUID) {
        val suppliedFluids = getSuppliedFluids(segment)
        val requestedFluids = getRequestedFluids(segment)

        for ((fluid, suppliers) in suppliedFluids) {
            val predicate = segments[segment]!!.predicate
            if (predicate != null && !predicate.test(fluid)) {
                continue
            }

            val requesters = requestedFluids[fluid]?.toMutableSet()
            if (requesters.isNullOrEmpty()) {
                continue
            }

            // Take fluids on a first-come-first-serve basis from suppliers
            val totalRequested = min(requesters.sumOf { it.amount }, segments[segment]!!.fluidPerTick)
            if (totalRequested == 0L) {
                continue
            }
            var totalSupplied = 0L
            for (supplier in suppliers) {
                val remaining = totalRequested - totalSupplied
                if (remaining == 0L) {
                    break
                }

                val toTake = min(remaining, supplier.amount)
                try {
                    supplier.block.removeFluid(supplier.name, supplier.fluid, toTake)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    continue
                }
                totalSupplied += toTake
            }

            // Round-robin distribute to requesters
            while (totalSupplied != 0L) {
                val maxFluidPerRequester = if (totalSupplied / requesters.size != 0L) {
                    totalSupplied / requesters.size
                } else {
                    // eg: splitting 3 mB amongst 5 requesters
                    1
                }
                val iterator = requesters.iterator()
                while (iterator.hasNext() && totalSupplied != 0L) {
                    val requester = iterator.next()
                    if (requester.amount < maxFluidPerRequester) {
                        requester.block.addFluid(requester.name, requester.fluid, requester.amount)
                        iterator.remove()
                        totalSupplied -= requester.amount
                    } else {
                        requester.block.addFluid(requester.name, requester.fluid, maxFluidPerRequester)
                        totalSupplied -= maxFluidPerRequester
                    }
                }
            }

            // Only allow one type of fluid to be distributed per tick
            break
        }
    }

    private fun startTicker(segment: UUID) {
        check(segment !in tickers) { "Ticker already active" }

        val dispatcher = pluginInstance.minecraftDispatcher
        tickers[segment] = pluginInstance.launch(dispatcher) {
            while (true) {
                delay(PylonConfig.fluidIntervalTicks.toLong())
                tick(segment)
            }
        }
    }
}