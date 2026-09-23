package com.bluedragonmc.interactions.rules.interaction.misc

import com.bluedragonmc.interactions.BlockInteractionRule
import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.util.getBlockOrAir
import com.bluedragonmc.interactions.util.isSecondaryUseActive
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import java.util.concurrent.ThreadLocalRandom

/**
 * Right-clicking on a dragon egg teleports it to a random nearby position
 * with solid ground and spawns some particles.
 */
class DragonEggInteractionRule : BlockInteractionRule(BlockSet.from(Block.DRAGON_EGG)) {
    init {
        onInteract {
            if (isSecondaryUseActive()) return@onInteract
            isBlockingItemUse = true

            val random = ThreadLocalRandom.current()
            val dimension = instance.cachedDimensionType

            repeat(1000) {
                val candidate = BlockVec(
                    blockPosition.blockX() + random.nextInt(16) - random.nextInt(16),
                    blockPosition.blockY() + random.nextInt(8) - random.nextInt(8),
                    blockPosition.blockZ() + random.nextInt(16) - random.nextInt(16),
                )
                if (candidate.blockY() < dimension.minY() || candidate.blockY() >= dimension.maxY() ||
                    !instance.worldBorder.inBounds(candidate) ||
                    !instance.getBlockOrAir(candidate).air() ||
                    instance.getBlockOrAir(candidate.relative(BlockFace.BOTTOM)).air()
                ) {
                    return@repeat
                }

                instance.setBlock(candidate, block)
                instance.setBlock(blockPosition, Block.AIR)
                spawnTeleportParticles(instance, blockPosition, candidate)
                return@onInteract
            }
        }
    }

    /** Spawns portal particles along the line between the old and new positions when the egg teleports. */
    private fun spawnTeleportParticles(instance: Instance, origin: BlockVec, destination: BlockVec) {
        for (segment in 0 until PARTICLE_SEGMENTS) {
            val delta = segment.toDouble() / (PARTICLE_SEGMENTS - 1)
            val x = origin.blockX() + delta * (destination.blockX() - origin.blockX()) + 0.5
            val y = origin.blockY() + delta * (destination.blockY() - origin.blockY()) + 0.5
            val z = origin.blockZ() + delta * (destination.blockZ() - origin.blockZ()) + 0.5
            instance.sendGroupedPacket(
                ParticlePacket(
                    Particle.PORTAL,
                    x,
                    y,
                    z,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPEED,
                    PARTICLES_PER_SEGMENT,
                ),
            )
        }
    }

    private companion object {
        const val PARTICLE_SEGMENTS = 16
        const val PARTICLES_PER_SEGMENT = 8
        const val PARTICLE_SPREAD = 0.25f
        const val PARTICLE_SPEED = 0.2f
    }
}
