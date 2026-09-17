package com.bluedragonmc.interactions

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class BlockUpdateTest {

    private class DestroyOnSupportRemoved(
        blocks: BlockSet,
        private val maxDistance: Int,
    ) : PlacementRule(blocks) {
        override fun maxUpdateDistance(): Int = maxDistance

        init {
            onUpdate {
                if (fromFace == BlockFace.BOTTOM) destroy()
            }
        }
    }

    @Test
    fun `block is destroyed when its support is removed`(env: Env) {
        val registry = RuleRegistry()
        registry.register(DestroyOnSupportRemoved(BlockSet.from(Block.TORCH), maxDistance = 10))
        MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())

        val instance = env.createFlatInstance()
        instance.loadChunk(0, 0).join()

        instance.setBlock(0, 40, 0, Block.TORCH)
        assertEquals(Block.TORCH, instance.getBlock(0, 40, 0))

        instance.setBlock(0, 39, 0, Block.AIR)
        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `updates do not propagate beyond the configured distance`(env: Env) {
        val registry = RuleRegistry()
        registry.register(DestroyOnSupportRemoved(BlockSet.from(Block.TORCH), maxDistance = 1))
        MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())

        val instance = env.createFlatInstance()
        instance.loadChunk(0, 0).join()

        instance.setBlock(0, 40, 0, Block.TORCH)
        instance.setBlock(1, 40, 0, Block.TORCH)
        assertEquals(Block.TORCH, instance.getBlock(0, 40, 0))
        assertEquals(Block.TORCH, instance.getBlock(1, 40, 0))

        instance.setBlock(0, 39, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
        assertEquals(Block.TORCH, instance.getBlock(1, 40, 0))
    }
}
