package io.github.pylonmc.pylon.core.block.base

/**
 * TODO: Come up with a better name if possible
 *
 * A [PylonCulledBlock] that requires other [PylonCulledBlock]s to all be culled
 * for the culling to actually take effect.
 *
 * For example, [io.github.pylonmc.pylon.core.content.fluid.FluidPipe] and [io.github.pylonmc.pylon.core.content.cargo.CargoDuct] both use greedy meshing,
 * on the display entities (multiple blocks use the same entity), and therefor should
 * only cull said entities if all blocks using that entity are culled.
 *
 * TODO: allow defining multiple groups per block with entities per group
 *  likely requires not extending PylonCulledBlock because that only allows one set of culledEntityIds
 */
interface PylonGroupCulledBlock : PylonCulledBlock {
    val cullingGroup: Iterable<PylonGroupCulledBlock>
}