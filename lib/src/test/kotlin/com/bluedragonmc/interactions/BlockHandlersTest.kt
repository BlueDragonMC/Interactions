package com.bluedragonmc.interactions

import net.minestom.server.MinecraftServer
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

class BlockHandlersTest {
    @Test
    fun `all block handlers can be registered`() {
        MinecraftServer.init()
        for (handler in Interactions.getBlockHandlers()) {
            assertDoesNotThrow {
                MinecraftServer.getBlockManager().registerHandler(handler.key) { handler }
            }
        }
    }
}
