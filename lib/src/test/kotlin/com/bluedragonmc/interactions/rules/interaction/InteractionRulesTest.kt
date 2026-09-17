package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.fireUseItem
import com.bluedragonmc.interactions.rules.base.holdMaterial
import com.bluedragonmc.interactions.rules.base.setupInstance
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.PlayerHand
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.Material
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnvTest
class InteractionRulesTest {

    private fun player(env: Env, instance: Instance, yaw: Float = 0f) =
        env.createPlayer(instance, Pos(0.5, 40.0, 0.5, yaw, 0f))

    // --- Flower pots --------------------------------------------------------------------------

    @Test
    fun `flower pot accepts a flower`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.FLOWER_POT)
        val player = player(env, instance)
        holdMaterial(player, Material.DANDELION)

        val event = fireInteract(instance, player, Block.FLOWER_POT, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.POTTED_DANDELION, instance.getBlock(0, 40, 0))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `flower pot maps azalea to its bush variant`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.FLOWER_POT)
        val player = player(env, instance)
        holdMaterial(player, Material.AZALEA)

        fireInteract(instance, player, Block.FLOWER_POT, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.POTTED_AZALEA_BUSH, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `flower pot consumes non-potable items`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.FLOWER_POT)
        val player = player(env, instance)
        holdMaterial(player, Material.STICK)

        val event = fireInteract(instance, player, Block.FLOWER_POT, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FLOWER_POT, instance.getBlock(0, 40, 0))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `empty hand removes the flower from a pot`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.POTTED_DANDELION)
        val player = player(env, instance)

        val event = fireInteract(instance, player, Block.POTTED_DANDELION, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FLOWER_POT, instance.getBlock(0, 40, 0))
        assertTrue(player.inventory.getItemStacks().any { it.material() == Material.DANDELION })
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `removing an azalea bush returns azalea`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.POTTED_AZALEA_BUSH)
        val player = player(env, instance)

        fireInteract(instance, player, Block.POTTED_AZALEA_BUSH, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FLOWER_POT, instance.getBlock(0, 40, 0))
        assertTrue(player.inventory.getItemStacks().any { it.material() == Material.AZALEA })
    }

    @Test
    fun `holding a compatible flower keeps the pot filled`(env: Env) {
        val instance = setupInstance(env, FlowerPotRule())
        instance.setBlock(0, 40, 0, Block.POTTED_POPPY)
        val player = player(env, instance)
        holdMaterial(player, Material.DANDELION)

        val event = fireInteract(instance, player, Block.POTTED_POPPY, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.POTTED_POPPY, instance.getBlock(0, 40, 0))
        assertTrue(event.isBlockingItemUse)
    }

    // --- Pumpkin carving ----------------------------------------------------------------------

    @Test
    fun `shears carve a pumpkin facing away from the player`(env: Env) {
        val instance = setupInstance(env, PumpkinCarvingRule())
        instance.setBlock(0, 40, 0, Block.PUMPKIN)
        val player = player(env, instance) // yaw 0 looks south
        holdMaterial(player, Material.SHEARS)

        val event = fireInteract(instance, player, Block.PUMPKIN, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("north", instance.getBlock(0, 40, 0).getProperty("facing"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `pumpkin carving uses the clicked horizontal face`(env: Env) {
        val instance = setupInstance(env, PumpkinCarvingRule())
        instance.setBlock(0, 40, 0, Block.PUMPKIN)
        val player = player(env, instance)
        holdMaterial(player, Material.SHEARS)

        fireInteract(instance, player, Block.PUMPKIN, BlockFace.EAST, BlockVec(0, 40, 0))

        assertEquals("east", instance.getBlock(0, 40, 0).getProperty("facing"))
    }

    // --- Buckets ------------------------------------------------------------------------------

    @Test
    fun `water bucket waterlogs a waterloggable block`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.OAK_SLAB.withProperty("type", "bottom"))
        val player = player(env, instance)
        holdMaterial(player, Material.WATER_BUCKET)

        val event = fireInteract(instance, player, Block.OAK_SLAB, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty("waterlogged"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `water bucket places water in the adjacent block`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        holdMaterial(player, Material.WATER_BUCKET)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.WATER, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `lava bucket places lava and does not waterlog`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        holdMaterial(player, Material.LAVA_BUCKET)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.LAVA, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `water bucket places beside a fire block`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.FIRE)
        val player = player(env, instance)
        holdMaterial(player, Material.WATER_BUCKET)

        fireInteract(instance, player, Block.FIRE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FIRE, instance.getBlock(0, 40, 0))
        assertEquals(Block.WATER, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `lava bucket places beside a fire block`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.FIRE)
        val player = player(env, instance)
        holdMaterial(player, Material.LAVA_BUCKET)

        fireInteract(instance, player, Block.FIRE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FIRE, instance.getBlock(0, 40, 0))
        assertEquals(Block.LAVA, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `sneaking does not waterlog`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.OAK_SLAB.withProperty("type", "bottom"))
        val player = player(env, instance)
        player.setSneaking(true)
        holdMaterial(player, Material.WATER_BUCKET)

        fireInteract(instance, player, Block.OAK_SLAB, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("waterlogged"))
        assertEquals(Block.WATER, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `survival bucket leaves an empty bucket`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        player.setGameMode(GameMode.SURVIVAL)
        holdMaterial(player, Material.WATER_BUCKET)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Material.BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `creative bucket is kept`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        player.setGameMode(GameMode.CREATIVE)
        holdMaterial(player, Material.WATER_BUCKET)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Material.WATER_BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `empty bucket collects a water source`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.WATER)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        val event = fireInteract(instance, player, Block.WATER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
        assertEquals(Material.WATER_BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `empty bucket collects water by aiming at it`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        // Eye level, three blocks south of the player (yaw 0 looks south).
        instance.setBlock(0, 41, 3, Block.WATER)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        val event = fireUseItem(player)

        assertEquals(Block.AIR, instance.getBlock(0, 41, 3))
        assertEquals(Material.WATER_BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
        assertTrue(event.isCancelled)
    }

    @Test
    fun `empty bucket aimed at a solid block does nothing`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 41, 3, Block.STONE)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        val event = fireUseItem(player)

        assertEquals(Material.BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
        assertFalse(event.isCancelled)
    }

    @Test
    fun `empty bucket collects a lava source`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.LAVA)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        fireInteract(instance, player, Block.LAVA, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
        assertEquals(Material.LAVA_BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `empty bucket ignores flowing fluids`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        val flowing = Block.WATER.withProperty("level", "1")
        instance.setBlock(0, 40, 0, flowing)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        val event = fireInteract(instance, player, flowing, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(flowing, instance.getBlock(0, 40, 0))
        assertEquals(Material.BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
        assertFalse(event.isBlockingItemUse)
    }

    @Test
    fun `empty bucket drains a waterlogged block`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        val waterlogged = Block.OAK_SLAB.withProperty("type", "bottom").withProperty("waterlogged", "true")
        instance.setBlock(0, 40, 0, waterlogged)
        val player = player(env, instance)
        holdMaterial(player, Material.BUCKET)

        fireInteract(instance, player, waterlogged, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty("waterlogged"))
        assertEquals(Material.WATER_BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `creative empty bucket is kept and the filled bucket goes to the inventory`(env: Env) {
        val instance = setupInstance(env, BucketRule())
        instance.setBlock(0, 40, 0, Block.WATER)
        val player = player(env, instance)
        player.setGameMode(GameMode.CREATIVE)
        holdMaterial(player, Material.BUCKET)

        fireInteract(instance, player, Block.WATER, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Material.BUCKET, player.getItemInHand(PlayerHand.MAIN).material())
        assertTrue(player.inventory.getItemStacks().any { it.material() == Material.WATER_BUCKET })
    }

    // --- Fire starters ------------------------------------------------------------------------

    @Test
    fun `flint and steel creates fire on a solid surface`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        val event = fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FIRE, instance.getBlock(0, 41, 0))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `fire charge is consumed in survival`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        player.setGameMode(GameMode.SURVIVAL)
        holdMaterial(player, Material.FIRE_CHARGE)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FIRE, instance.getBlock(0, 41, 0))
        assertEquals(Material.AIR, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `fire charge is not consumed in creative`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        player.setGameMode(GameMode.CREATIVE)
        holdMaterial(player, Material.FIRE_CHARGE)

        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.FIRE, instance.getBlock(0, 41, 0))
        assertEquals(Material.FIRE_CHARGE, player.getItemInHand(PlayerHand.MAIN).material())
    }

    @Test
    fun `fire is not placed without support`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(1, 39, 0, Block.AIR)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        fireInteract(instance, player, Block.STONE, BlockFace.EAST, BlockVec(0, 40, 0))

        assertEquals(Block.AIR, instance.getBlock(1, 40, 0))
    }

    @Test
    fun `flint and steel lights fire against a flammable block`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.OAK_LEAVES)
        // No floor beside the leaves, so only the flammable neighbour can support the fire.
        instance.setBlock(1, 39, 0, Block.AIR)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        fireInteract(instance, player, Block.OAK_LEAVES, BlockFace.EAST, BlockVec(0, 40, 0))

        val fire = instance.getBlock(1, 40, 0)
        // The leaves are west of the target, so the fire attaches to its west side.
        assertEquals("true", fire.getProperty("west"))
        assertEquals("false", fire.getProperty("east"))
        assertEquals("false", fire.getProperty("north"))
        assertEquals("false", fire.getProperty("south"))
        assertEquals("false", fire.getProperty("up"))
    }

    @Test
    fun `fire attaches upward to a flammable block above`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(1, 39, 0, Block.AIR)
        instance.setBlock(1, 41, 0, Block.OAK_LEAVES)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        fireInteract(instance, player, Block.STONE, BlockFace.EAST, BlockVec(0, 40, 0))

        val fire = instance.getBlock(1, 40, 0)
        assertEquals("true", fire.getProperty("up"))
        assertEquals("false", fire.getProperty("west"))
        assertEquals("false", fire.getProperty("east"))
    }

    @Test
    fun `flint and steel lights soul fire on soul soil`(env: Env) {
        val instance = setupInstance(env, FireStarterRule())
        instance.setBlock(0, 40, 0, Block.SOUL_SOIL)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)

        fireInteract(instance, player, Block.SOUL_SOIL, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.SOUL_FIRE, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `fire recomputes its connections when a support is removed`(env: Env) {
        val instance = setupInstance(env, FireStarterRule(), FireRule())
        // Fire stands on a sturdy block, with a flammable block above it.
        instance.setBlock(0, 40, 0, Block.STONE)
        instance.setBlock(0, 42, 0, Block.OAK_PLANKS)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)
        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))
        assertEquals(Block.FIRE, instance.getBlock(0, 41, 0))

        // Removing the support makes the fire attach upward to the planks.
        instance.setBlock(0, 40, 0, Block.AIR)

        val fire = instance.getBlock(0, 41, 0)
        assertEquals("true", fire.getProperty("up"))
    }

    @Test
    fun `fire is destroyed when its last support is removed`(env: Env) {
        val instance = setupInstance(env, FireStarterRule(), FireRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val player = player(env, instance)
        holdMaterial(player, Material.FLINT_AND_STEEL)
        fireInteract(instance, player, Block.STONE, BlockFace.TOP, BlockVec(0, 40, 0))
        assertEquals(Block.FIRE, instance.getBlock(0, 41, 0))

        instance.setBlock(0, 40, 0, Block.AIR)

        assertEquals(Block.AIR, instance.getBlock(0, 41, 0))
    }

    @Test
    fun `fire keeps its age when it updates`(env: Env) {
        val instance = setupInstance(env, FireRule())
        instance.setBlock(0, 40, 0, Block.STONE)
        val agedFire = Block.FIRE.withProperty("age", "5")
        instance.setBlock(0, 41, 0, agedFire)

        // A neighbour change triggers the update.
        instance.setBlock(0, 42, 0, Block.OAK_PLANKS)

        assertEquals("5", instance.getBlock(0, 41, 0).getProperty("age"))
    }

    // --- Spawners -----------------------------------------------------------------------------

    @Test
    fun `spawn egg changes a monster spawner`(env: Env) {
        val instance = setupInstance(env, SpawnerRule())
        instance.setBlock(0, 40, 0, Block.SPAWNER)
        val player = player(env, instance)
        holdMaterial(player, Material.ZOMBIE_SPAWN_EGG)

        val event = fireInteract(instance, player, Block.SPAWNER, BlockFace.TOP, BlockVec(0, 40, 0))

        val nbt = instance.getBlock(0, 40, 0).nbt()!!
        assertEquals("minecraft:zombie", nbt.getCompound("SpawnData").getCompound("entity").getString("id"))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `spawn egg changes a trial spawner`(env: Env) {
        val instance = setupInstance(env, SpawnerRule())
        instance.setBlock(0, 40, 0, Block.TRIAL_SPAWNER)
        val player = player(env, instance)
        holdMaterial(player, Material.ZOMBIE_SPAWN_EGG)

        fireInteract(instance, player, Block.TRIAL_SPAWNER, BlockFace.TOP, BlockVec(0, 40, 0))

        val block = instance.getBlock(0, 40, 0)
        val normal = block.nbt()!!.getCompound("normal_config").getList("spawn_potentials").getCompound(0)
        assertEquals("minecraft:zombie", normal.getCompound("data").getCompound("entity").getString("id"))
        assertEquals("inactive", block.getProperty("trial_spawner_state"))
    }
}
