package io.github.pylonmc.pylon.core.fluid

import java.util.*

object FluidManager {

    /**
     * A point is just a connection in a fluid network, like a machine's output or the end of a pipe
     */
    private val points: MutableMap<UUID, FluidConnectionPoint> = mutableMapOf()

    /**
     * A segment is a collection of connection points
     */
    private val segments: MutableMap<UUID, MutableSet<FluidConnectionPoint>> = mutableMapOf()

    @JvmStatic
    fun add(point: FluidConnectionPoint) {
        check(!points.contains(point.id)) { "Duplicate connection point" }
        points[point.id] = point
        segments.getOrPut(point.segment) { mutableSetOf() }.add(point)
    }

    @JvmStatic
    fun remove(point: FluidConnectionPoint) {
        // TODO
    }

    @JvmStatic
    fun connect(point1: FluidConnectionPoint, point2: FluidConnectionPoint) {
        check(segments.contains(point1.segment)) { "Attempt to connect a nonexistant segment" }
        check(segments.contains(point2.segment)) { "Attempt to connect a nonexistant segment" }

        point1.connectedPoints.add(point2.id)
        point2.connectedPoints.add(point1.id)

        if (point1.segment != point2.segment) {
            val newSegment = point2.segment
            for (point in getAllConnected(point1)) {
                segments[point.segment]!!.remove(point)
                point.segment = newSegment
                segments[point.segment]!!.add(point)
            }
        }
    }

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
            }
        }
    }

    @JvmStatic
    fun getAllConnected(point: FluidConnectionPoint): Set<FluidConnectionPoint> {
        val visitedPoints: MutableSet<FluidConnectionPoint> = mutableSetOf()
        val pointsToVisit: MutableSet<FluidConnectionPoint> = mutableSetOf(point)
        while (pointsToVisit.isNotEmpty()) {
            for (uuid in pointsToVisit.drop(1)[0].connectedPoints) {
                if (points[uuid] != null) {
                    pointsToVisit.add(points[uuid]!!)
                }
            }
        }
        return visitedPoints
    }
}