package com.bluedragonmc.interactions.container

import net.kyori.adventure.key.Key
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals

@EnvTest
class ContainerPersistenceTest {

    @Test
    fun `container contents survive an anvil save and load`(env: Env, @TempDir worldDir: Path) {
        val dimension = Key.key("minecraft", "overworld")
        val chest = Block.CHEST.withContainerItems(
            listOf(ItemStack.of(Material.DIAMOND, 5), ItemStack.of(Material.STICK, 2)),
        )

        val writing = env.createEmptyInstance(AnvilLoader(worldDir, dimension))
        writing.loadChunk(0, 0).join()
        writing.setBlock(0, 40, 0, chest)
        writing.saveChunkToStorage(writing.getChunk(0, 0)!!).join()
        env.destroyInstance(writing)

        val reading = env.createEmptyInstance(AnvilLoader(worldDir, dimension))
        reading.loadChunk(0, 0).join()

        val loaded = reading.getBlock(0, 40, 0)
        assertEquals(Block.CHEST.key(), loaded.key())
        val items = readContainerItems(loaded, 27)
        assertEquals(Material.DIAMOND, items[0].material())
        assertEquals(5, items[0].amount())
        assertEquals(Material.STICK, items[1].material())
        assertEquals(2, items[1].amount())
        env.destroyInstance(reading)
    }
}
