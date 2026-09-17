package com.bluedragonmc.interactions

import com.bluedragonmc.interactions.rules.RuleCatalog
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@EnvTest
class RuleCatalogTest {

    @Test
    fun `catalog rules can be registered into multiple registries`(env: Env) {
        val first = RuleRegistry()
        val second = RuleRegistry()

        RuleCatalog.registerAll(first)
        RuleCatalog.registerAll(second)

        assertEquals(first.rules().size, second.rules().size)
        assertNotSame(first.rules().first(), second.rules().first())
    }
}
