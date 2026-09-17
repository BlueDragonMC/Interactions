package com.bluedragonmc.interactions

import net.minestom.server.instance.block.Block
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

@EnvTest
class RuleRegistryTest {

    private class NoopRule(blocks: BlockSet) : PlacementRule(blocks)

    @Test
    fun `rule is found regardless of block state`(env: Env) {
        val registry = RuleRegistry()
        val rule = NoopRule(BlockSet.from(Block.OAK_BUTTON))
        registry.register(rule)

        assertSame(rule, registry.ruleFor(Block.OAK_BUTTON))
        assertSame(rule, registry.ruleFor(Block.OAK_BUTTON.withProperty("facing", "north")))
        assertNull(registry.ruleFor(Block.STONE))
    }

    @Test
    fun `rules covering overlapping blocks are rejected`(env: Env) {
        val registry = RuleRegistry()
        registry.register(NoopRule(BlockSet.fromTag("minecraft:buttons")))

        assertFailsWith<IllegalStateException> {
            registry.register(NoopRule(BlockSet.from(Block.STONE_BUTTON)))
        }
    }

    @Test
    fun `unregister removes the rule`(env: Env) {
        val registry = RuleRegistry()
        val rule = NoopRule(BlockSet.from(Block.OAK_BUTTON))
        registry.register(rule)
        registry.unregister(rule)

        assertNull(registry.ruleFor(Block.OAK_BUTTON))
        assertEquals(0, registry.rules().size)
    }
}
