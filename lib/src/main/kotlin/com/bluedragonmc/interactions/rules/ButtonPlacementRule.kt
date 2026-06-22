package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.BlockBehavior
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.blockFace
import com.bluedragonmc.interactions.util.closestDirections
import com.bluedragonmc.interactions.util.closestHorizontalFace
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.utils.Direction

class ButtonPlacementRule : BlockBehavior(BlockSet.fromTag("minecraft:buttons")) {
    init {
        onPlace {
            isCancelled = true
            for (dir in player.position.direction().closestDirections()) {
                val (face, facing) = if (dir.vertical()) {
                    val face = if (dir == Direction.UP) "ceiling" else "floor"
                    val facing = player.position.direction().closestHorizontalFace().blockFace
                    Pair(face, facing)
                } else {
                    val face = "wall"
                    val facing = dir.opposite().blockFace
                    Pair(face, facing)
                }

                val supporting = getSupportingBlockPosition(face, facing, blockPosition)
                val supportFace = dir.opposite().blockFace

                if (instance.getBlock(supporting).registry()?.collisionShape()?.isFaceFull(supportFace) != true) {
                    continue
                }

                isCancelled = false
                block = block
                    .withProperty("facing", facing.name.lowercase())
                    .withProperty("face", face)

                return@onPlace
            }
        }
    }

    private fun getSupportingBlockPosition(face: String, facing: BlockFace, blockPosition: Point): Point {
        return when (face) {
            "ceiling" -> blockPosition.add(0.0, 1.0, 0.0)
            "floor" -> blockPosition.sub(0.0, 1.0, 0.0)
            else -> blockPosition.add(facing.oppositeFace.toDirection().vec())
        }
    }
}
