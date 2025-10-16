package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.content.fluid.FluidEndpointDisplay
import io.github.pylonmc.pylon.core.fluid.FluidPointType
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.rotateToPlayerFacing
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * A block that interacts with fluids in some way.
 *
 * This is a very flexible class which requires you to define exactly how fluid should
 * be input and output. You are responsible for keeping track of any state, like how
 * much fluid is stored.
 *
 * Most fluid blocks can use [PylonFluidBufferBlock] or [PylonFluidTank] instead.
 *
 * This interface allowed you to request fluids from input points, supply fluids to
 * output points, and specify how to add/remove fluids from your block.
 *
 * Multiple inputs/outputs are not supported. You can have at most 1 input and 1 output.
 *
 * PylonFLuidBlocks automatically implement [PylonDirectionalBlock]. If the block has
 * an input or output point, the block direction will be towards the output or input
 * point's face. Output points take precedence over input points. You can override
 * this behaviour by overriding [getFacing].
 *
 * @see PylonFluidBufferBlock
 * @see PylonFluidTank
 */
interface PylonFluidBlock : PylonEntityHolderBlock, PylonDirectionalBlock, PylonBreakHandler {

    override fun getFacing(): BlockFace? =
        getHeldPylonEntity(FluidEndpointDisplay::class.java, "fluid_point_output")?.face
            ?: getHeldPylonEntity(FluidEndpointDisplay::class.java, "fluid_point_input")?.face

    fun getFluidPointDisplay(type: FluidPointType) =
        getHeldPylonEntity(FluidEndpointDisplay::class.java, getFluidPointName(type))

    fun getFluidPointDisplayOrThrow(type: FluidPointType) =
        getHeldPylonEntityOrThrow(FluidEndpointDisplay::class.java, getFluidPointName(type))

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace, radius: Float) {
        check(getFluidPointDisplay(type) == null) { "A fluid point of type $type already exists on this block" }
        addEntity(getFluidPointName(type), FluidEndpointDisplay(block, type, face, radius))
    }

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace) = createFluidPoint(type, face, 0.5F)

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     *
     * @param player If supplied, the point will be rotated to the player's frame of reference, with NORTH
     * considered 'forward'
     * @param allowVerticalFaces Whether up/down should be considered when rotating to the player's frame
     * of reference
     *
     * @see rotateToPlayerFacing
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace, player: Player?, allowVerticalFaces: Boolean, radius: Float) {
        var finalFace = face
        if (player != null) {
            finalFace = rotateToPlayerFacing(player, face, allowVerticalFaces)
        }
        createFluidPoint(type, finalFace, radius)
    }

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     *
     * @param player If supplied, the point will be rotated to the player's frame of reference, with NORTH
     * considered 'forward'
     * @param allowVerticalFaces Whether up/down should be considered when rotating to the player's frame
     * of reference
     *
     * @see rotateToPlayerFacing
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace, player: Player?, allowVerticalFaces: Boolean) {
        createFluidPoint(type, face, player, allowVerticalFaces, 0.5F)
    }

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     *
     * @param context If a player placed the block, the point will be rotated to the player's frame of reference,
     * with NORTH considered 'forward'
     * @param allowVerticalFaces Whether up/down should be considered when rotating to the player's frame
     * of reference
     *
     * @see rotateToPlayerFacing
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace, context: BlockCreateContext, allowVerticalFaces: Boolean) {
        createFluidPoint(type, face, (context as? BlockCreateContext.PlayerPlace)?.player, allowVerticalFaces)
    }

    /**
     * Creates a fluid input point. Call in your place constructor. Should be called at most once per block.
     *
     * @param context If a player placed the block, the point will be rotated to the player's frame of reference,
     * with NORTH considered 'forward'
     * @param allowVerticalFaces Whether up/down should be considered when rotating to the player's frame
     * of reference
     *
     * @see rotateToPlayerFacing
     */
    fun createFluidPoint(type: FluidPointType, face: BlockFace, context: BlockCreateContext, allowVerticalFaces: Boolean, radius: Float) {
        createFluidPoint(type, face, (context as? BlockCreateContext.PlayerPlace)?.player, allowVerticalFaces, radius)
    }

    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        val player = (context as? BlockBreakContext.PlayerBreak)?.event?.player
        getFluidPointDisplay(FluidPointType.INPUT)?.pipeDisplay?.delete(player, drops)
    }

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * the block for this fluid tick. deltaSeconds is the time since the last fluid tick.
     *
     * If you have a machine that can supply up to 100 fluid per second, it should supply
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     *
     * Called exactly one per fluid tick.
     */
    fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * Returns the amount of the given fluid that the machine wants to receive next tick.
     *
     * If you have a machine that consumes 100 water per second, it should request
     * 100*deltaSeconds of water, and return 0 for every other fluid.
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     *
     * Called at most once for any given fluid type per tick.
     */
    fun fluidAmountRequested(fluid: PylonFluid, deltaSeconds: Double): Double = 0.0

    /**
     * `amount` is always at most `getRequestedFluids().get(fluid)` and will never
     * be zero or less.
     *
     * Called at most once per fluid tick.
     */
    fun onFluidAdded(fluid: PylonFluid, amount: Double) {
        error("Block requested fluids, but does not implement onFluidAdded")
    }

    /**
     * `amount` is always at least `getSuppliedFluids().get(fluid)` and will never
     * be zero or less.
     *
     * Called at most once per fluid tick.
     */
    fun onFluidRemoved(fluid: PylonFluid, amount: Double) {
        error("Block supplied fluids, but does not implement removeFluid")
    }

    @ApiStatus.Internal
    companion object {
        fun getFluidPointName(type: FluidPointType) = when (type) {
            FluidPointType.INPUT -> "fluid_point_input"
            FluidPointType.OUTPUT -> "fluid_point_output"
            FluidPointType.INTERSECTION -> throw IllegalStateException("You cannot create an intersection point from a fluid block")
        }
    }
}