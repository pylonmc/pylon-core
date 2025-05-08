package io.github.pylonmc.pylon.core.fluid

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.base.PylonFluidBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import java.util.*
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

    /**
     * A point is just a connection in a fluid network, like a machine's output or the end of a pipe
     */
    private val points: MutableMap<UUID, FluidConnectionPoint> = mutableMapOf()

    /**
     * A segment is a collection of connection points
     */
    private val segments: MutableMap<UUID, MutableSet<FluidConnectionPoint>> = mutableMapOf()

    /**
     * Each segment has a corresponding ticker
     */
    private val tickers: MutableMap<UUID, Job> = mutableMapOf()

    /**
     * Adds the point to its stored segment, creating the segment and starting a ticker for it if it does not exist
     */
    private fun addToSegment(point: FluidConnectionPoint) {
        synchronized(segments) {
            if (!segments.contains(point.segment)) {
                segments[point.segment] = mutableSetOf()
                startTicker(point.segment)
            }
            segments[point.segment]!!.add(point)
        }
    }

    /**
     * Removes the point from its segment, deleting the segment and cancelling the ticker if the segment is
     * now empty
     */
    private fun removeFromSegment(point: FluidConnectionPoint) {
        synchronized(segments) {
            segments[point.segment]!!.remove(point)
            if (segments[point.segment]!!.isEmpty()) {
                segments.remove(point.segment)
                tickers[point.segment]!!.cancel()
            }
        }
    }

    /**
     * Call when creating a new connection point, or when one has been loaded
     */
    @JvmStatic
    fun add(point: FluidConnectionPoint) {
        synchronized(points) {
            check(!points.contains(point.id)) { "Duplicate connection point" }

            points[point.id] = point

            addToSegment(point)

            for (otherPointId in point.connectedPoints) {
                points[otherPointId]?.let {
                    connect(point, it)
                }
            }
        }
    }

    /**
     * Call when removing a connection point, or when one has been unloaded
     */
    @JvmStatic
    fun remove(point: FluidConnectionPoint) {
        synchronized(points) {
            check(points.contains(point.id)) { "Nonexistant connection point" }

            for (otherPointId in point.connectedPoints) {
                points[otherPointId]?.let {
                    disconnect(point, it)
                }
            }

            removeFromSegment(point)

            points.remove(point.id)
        }
    }

    /**
     * Connects two points - and all their connected points - into one segment
     */
    @JvmStatic
    fun connect(point1: FluidConnectionPoint, point2: FluidConnectionPoint) {
        check(segments.contains(point1.segment)) { "Attempt to connect a nonexistant segment" }
        check(segments.contains(point2.segment)) { "Attempt to connect a nonexistant segment" }

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
    }

    /**
     * Disconnects two points, potentially splitting them into two segments if there is no
     * other link between them.
     */
    @JvmStatic
    fun disconnect(point1: FluidConnectionPoint, point2: FluidConnectionPoint) {
        check(segments.contains(point1.segment)) { "Attempt to disconnect a nonexistant segment" }
        check(segments.contains(point2.segment)) { "Attempt to disconnect a nonexistant segment" }
        check(point1.connectedPoints.contains(point2.id)) { "Attempt to disconnect two points that are not connected" }
        check(point2.connectedPoints.contains(point1.id)) { "Attempt to disconnect two points that are not connected" }

        point1.connectedPoints.remove(point2.id)
        point2.connectedPoints.remove(point1.id)

        val connectedToPoint1 = getAllConnected(point1)
        if (!connectedToPoint1.contains(point2)) { // points are still (indirectly) connected
            val newSegment = UUID.randomUUID()
            for (point in connectedToPoint1) {
                point.segment = newSegment
                addToSegment(point)
            }
        }
    }

    /**
     * Recursively gets all the points connected to another point *that are loaded*
     */
    @JvmStatic
    fun getAllConnected(point: FluidConnectionPoint): Set<FluidConnectionPoint> {
        val visitedPoints: MutableSet<FluidConnectionPoint> = mutableSetOf()
        val pointsToVisit: MutableSet<FluidConnectionPoint> = mutableSetOf(point)
        while (pointsToVisit.isNotEmpty()) {
            // why does java/kotlin not have a Set.pop method???
            val nextPoint = pointsToVisit.iterator().next()
            pointsToVisit.remove(nextPoint)
            visitedPoints.add(nextPoint)
            for (uuid in nextPoint.connectedPoints) {
                if (points[uuid] != null && !visitedPoints.contains(points[uuid])) {
                    pointsToVisit.add(points[uuid]!!)
                }
            }
        }
        return visitedPoints
    }

    fun getPoints(segment: UUID, type: FluidConnectionPoint.Type): List<FluidConnectionPoint>
        = segments[segment]!!.filter { it.type == type }

    data class FluidSupplier(val block: PylonFluidBlock, val name: String, val fluid: PylonFluid, val amount: Int)

    data class FluidRequester(val block: PylonFluidBlock, val name: String, val fluid: PylonFluid, val amount: Int)

    fun getSuppliedFluids(point: FluidConnectionPoint): Set<FluidSupplier> {
        check(point.type == FluidConnectionPoint.Type.OUTPUT) { "Can only get supplied fluids of output point" }

        val block: PylonFluidBlock
        val blockSuppliedFluids: Map<PylonFluid, Int>
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
            if (amount != 0) {
                suppliedFluids.add(FluidSupplier(block, point.name, fluid, amount))
            }
        }
        return suppliedFluids
    }

    fun getSuppliedFluids(segment: UUID): Map<PylonFluid, Set<FluidSupplier>> {
        val suppliedFluids: MutableMap<PylonFluid, MutableSet<FluidSupplier>> = mutableMapOf()
        for (point in getPoints(segment, FluidConnectionPoint.Type.OUTPUT)) {
            for (supplier in getSuppliedFluids(point)) {
                suppliedFluids.putIfAbsent(supplier.fluid, mutableSetOf())
                suppliedFluids[supplier.fluid]!!.add(supplier)
            }
        }
        return suppliedFluids
    }

    fun getRequestedFluids(point: FluidConnectionPoint): Set<FluidRequester> {
        check(point.type == FluidConnectionPoint.Type.INPUT) { "Can only get requested fluids of input point" }

        val block: PylonFluidBlock
        val blockRequestedFluids: Map<PylonFluid, Int>
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
            if (amount != 0) {
                requestedFluids.add(FluidRequester(block, point.name, fluid, amount))
            }
        }
        return requestedFluids
    }

    fun getRequestedFluids(segment: UUID): Map<PylonFluid, Set<FluidRequester>> {
        val requestedFluids: MutableMap<PylonFluid, MutableSet<FluidRequester>> = mutableMapOf()
        for (point in getPoints(segment, FluidConnectionPoint.Type.INPUT)) {
            for (requester in getRequestedFluids(point)) {
                requestedFluids.putIfAbsent(requester.fluid, mutableSetOf())
                requestedFluids[requester.fluid]!!.add(requester)
            }
        }
        return requestedFluids
    }

    private fun tick(segment: UUID) {
        val suppliedFluids = getSuppliedFluids(segment)
        val requestedFluids = getRequestedFluids(segment)

        for ((fluid, suppliers) in suppliedFluids) {
            val requesters = requestedFluids[fluid]?.toMutableSet()
            if (requesters.isNullOrEmpty()) {
                continue
            }

            // Take fluids on a first-come-first-serve basis from suppliers
            val totalRequested = requesters.sumOf { it.amount }
            var totalSupplied = 0
            for (supplier in suppliers) {
                val remaining = totalRequested - totalSupplied
                if (remaining == 0) {
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
            while (totalSupplied != 0) {
                val maxFluidPerRequester = totalSupplied / requesters.size
                val iterator = requesters.iterator()
                while (iterator.hasNext()) {
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
        check(!tickers.contains(segment)) { "Ticker already active" }

        val dispatcher = pluginInstance.minecraftDispatcher
        tickers[segment] = pluginInstance.launch(dispatcher) {
            while (true) {
                delay(PylonConfig.fluidIntervalTicks.toLong())
                tick(segment)
            }
        }
    }
}