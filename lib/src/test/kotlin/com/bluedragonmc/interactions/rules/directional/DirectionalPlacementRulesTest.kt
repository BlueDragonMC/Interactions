package com.bluedragonmc.interactions.rules.directional

import com.bluedragonmc.interactions.rules.base.firePlace
import com.bluedragonmc.interactions.rules.base.setupInstance
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@EnvTest
class DirectionalPlacementRulesTest {

    private fun player(env: Env, instance: Instance) = env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    @Test
    fun `logs take their axis from the clicked face`(env: Env) {
        val instance = setupInstance(env, LogAxisRule())
        val player = player(env, instance)

        val horizontal = firePlace(instance, player, Block.OAK_LOG, BlockFace.EAST, BlockVec(0, 40, 0))
        assertEquals(Block.OAK_LOG.withProperty("axis", "x"), horizontal.block)

        val vertical = firePlace(instance, player, Block.OAK_LOG, BlockFace.TOP, BlockVec(0, 41, 0))
        assertEquals(Block.OAK_LOG.withProperty("axis", "y"), vertical.block)
    }

    @Test
    fun `pillars take their axis from the clicked face`(env: Env) {
        val instance = setupInstance(env, PillarPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.BASALT, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals(Block.BASALT.withProperty("axis", "z"), event.block)
    }

    @Test
    fun `furnace faces the player`(env: Env) {
        val instance = setupInstance(env, FurnacePlacementRule())

        val event = firePlace(instance, player(env, instance), Block.FURNACE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FURNACE.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `glazed terracotta faces the player`(env: Env) {
        val instance = setupInstance(env, GlazedTerracottaPlacementRule())

        val event = firePlace(
            instance,
            env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 90f, 0f)),
            Block.WHITE_GLAZED_TERRACOTTA,
            BlockFace.TOP,
            BlockVec(0, 40, 0),
        )

        assertEquals(Block.WHITE_GLAZED_TERRACOTTA.withProperty("facing", "east"), event.block)
    }

    @Test
    fun `pumpkin faces the player`(env: Env) {
        val instance = setupInstance(env, PumpkinPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.CARVED_PUMPKIN, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.CARVED_PUMPKIN.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `beehive faces the player`(env: Env) {
        val instance = setupInstance(env, BeehivePlacementRule())

        val event = firePlace(instance, player(env, instance), Block.BEEHIVE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.BEEHIVE.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `ender chest faces the player and is not waterlogged`(env: Env) {
        val instance = setupInstance(env, EnderChestPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.ENDER_CHEST, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(
            Block.ENDER_CHEST.withProperty("facing", "north").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `end portal frame faces the player`(env: Env) {
        val instance = setupInstance(env, EndPortalFramePlacementRule())

        val event = firePlace(instance, player(env, instance), Block.END_PORTAL_FRAME, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.END_PORTAL_FRAME.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `copper golem statue faces the player`(env: Env) {
        val instance = setupInstance(env, CopperGolemStatuePlacementRule())

        val event = firePlace(instance, player(env, instance), Block.COPPER_GOLEM_STATUE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(
            Block.COPPER_GOLEM_STATUE.withProperty("facing", "north").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `barrel faces away from the player`(env: Env) {
        val instance = setupInstance(env, BarrelPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.BARREL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.BARREL.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `piston faces away from the player`(env: Env) {
        val instance = setupInstance(env, PistonPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.PISTON, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.PISTON.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `dispenser faces away from the player`(env: Env) {
        val instance = setupInstance(env, DispenserPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.DISPENSER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.DISPENSER.withProperty("facing", "north"), event.block)
    }

    @Test
    fun `observer faces the same way the player is looking`(env: Env) {
        val instance = setupInstance(env, ObserverPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.OBSERVER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.OBSERVER.withProperty("facing", "south"), event.block)
    }

    @Test
    fun `amethyst cluster faces the clicked face`(env: Env) {
        val instance = setupInstance(env, AmethystClusterPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.AMETHYST_CLUSTER, BlockFace.WEST, BlockVec(0, 40, 0))

        assertEquals(
            Block.AMETHYST_CLUSTER.withProperty("facing", "west").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `lightning rod faces the clicked face`(env: Env) {
        val instance = setupInstance(env, LightningRodPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.LIGHTNING_ROD, BlockFace.BOTTOM, BlockVec(0, 40, 0))

        assertEquals(
            Block.LIGHTNING_ROD.withProperty("facing", "down").withProperty("waterlogged", "false"),
            event.block,
        )
    }

    @Test
    fun `end rod faces the clicked face`(env: Env) {
        val instance = setupInstance(env, EndRodPlacementRule())

        val event = firePlace(instance, player(env, instance), Block.END_ROD, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.END_ROD.withProperty("facing", "up"), event.block)
    }

    @Test
    fun `end rod flips when continuing an existing end rod`(env: Env) {
        val instance = setupInstance(env, EndRodPlacementRule())
        instance.setBlock(0, 40, 0, Block.END_ROD.withProperty("facing", "up"))

        val event = firePlace(instance, player(env, instance), Block.END_ROD, BlockFace.TOP, BlockVec(0, 41, 0))

        assertEquals(Block.END_ROD.withProperty("facing", "down"), event.block)
    }
}
