package com.bluedragonmc.interactions.rules.connecting

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.rules.base.AllAxisRule
import com.bluedragonmc.interactions.rules.base.ConnectingPlacementRule
import com.bluedragonmc.interactions.rules.base.WallPlacementRule
import com.bluedragonmc.interactions.util.*
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

/** Wooden fences connect to solid blocks, other wooden fences and fence gates. */
class WoodenFenceRule : ConnectingPlacementRule(BlockSet.fromTag("minecraft:wooden_fences")) {
    override fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
        (!Support.isExceptionForConnection(neighbor) && faceSolid) ||
            neighbor.key() in keys ||
            Connections.isFenceGate(neighbor)
}

/** Nether brick fences connect to solid blocks, other nether brick fences and fence gates. */
class NetherBrickFenceRule : ConnectingPlacementRule(BlockSet.from(Block.NETHER_BRICK_FENCE)) {
    override fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
        (!Support.isExceptionForConnection(neighbor) && faceSolid) ||
            neighbor.key() == Block.NETHER_BRICK_FENCE.key() ||
            Connections.isFenceGate(neighbor)
}

/** Iron/copper bars and glass panes connect to solid blocks, each other and walls. */
class IronBarsRule : ConnectingPlacementRule(
    BlockSet.matching {
        it == "minecraft:iron_bars" || it == "minecraft:glass_pane" || it.endsWith("_glass_pane") ||
            it.endsWith("copper_bars")
    },
) {
    override fun canConnect(neighbor: Block, faceSolid: Boolean, direction: Direction): Boolean =
        (!Support.isExceptionForConnection(neighbor) && faceSolid) ||
            neighbor.key() in keys ||
            Connections.isWall(neighbor)
}

/** Walls connect to solid blocks, other walls, panes/bars and fence gates. */
class WallRule : WallPlacementRule(BlockSet.fromTag("minecraft:walls"))

/** Chains are pillars that also carry `waterlogged`. */
class ChainRule : AllAxisRule(BlockSet.fromTag("minecraft:chains"))

/** Fence gates face the player and follow redstone signals. */
class FenceGateRule : PlacementRule(BlockSet.fromTag("minecraft:fence_gates")) {

    init {
        onPlace {
            val facing = player.position.horizontalDirection()
            val powered = Signals.hasNeighborSignal(instance, blockPosition)
            val verticalAxis = facing.axisName == "z"
            val inWall = if (verticalAxis) {
                Connections.isWall(instance.getBlockOrAir(blockPosition.relative(BlockFace.WEST))) ||
                    Connections.isWall(instance.getBlockOrAir(blockPosition.relative(BlockFace.EAST)))
            } else {
                Connections.isWall(instance.getBlockOrAir(blockPosition.relative(BlockFace.NORTH))) ||
                    Connections.isWall(instance.getBlockOrAir(blockPosition.relative(BlockFace.SOUTH)))
            }
            block = block
                .withProperty(States.FACING, facing.name.lowercase())
                .withProperty(States.OPEN, powered.toString())
                .withProperty(States.POWERED, powered.toString())
                .withProperty(States.IN_WALL, inWall.toString())
        }

        onUpdate {
            val powered = Signals.hasNeighborSignal(instance, blockPosition)
            if (powered != (block.getProperty(States.POWERED) == "true")) {
                resultBlock = block
                    .withProperty(States.OPEN, powered.toString())
                    .withProperty(States.POWERED, powered.toString())
            }
        }

        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            val open = block.getProperty(States.OPEN) == "true"
            val updated = if (open) {
                block.withProperty(States.OPEN, "false")
            } else {
                val facing = player.position.horizontalDirection()
                var state = block
                if (state.getProperty(States.FACING) == facing.oppositeFace.name.lowercase()) {
                    state = state.withProperty(States.FACING, facing.name.lowercase())
                }
                state.withProperty(States.OPEN, "true")
            }
            instance.setBlock(blockPosition, updated)
            isBlockingItemUse = true
        }
    }
}
