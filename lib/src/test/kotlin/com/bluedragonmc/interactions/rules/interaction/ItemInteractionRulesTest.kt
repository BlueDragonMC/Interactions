package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.holdBlock
import com.bluedragonmc.interactions.rules.base.holdMaterial
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.interaction.misc.BeehiveInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.CakeInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.CandleCakeInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.ComposterInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.DragonEggInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.JukeboxInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.RespawnAnchorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.multiblock.BedInteractionRule
import com.bluedragonmc.interactions.rules.interaction.plants.CaveVinesInteractionRule
import com.bluedragonmc.interactions.rules.interaction.plants.SweetBerryBushInteractionRule
import com.bluedragonmc.interactions.util.States
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Explosion
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.network.packet.server.play.SwingAnimationPacket
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EnvTest
class ItemInteractionRulesTest {

    private fun actor(env: Env, instance: Instance): Player =
        env.createPlayer(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))

    // --- Beds ---------------------------------------------------------------------------------

    @Test
    fun `sleeping in a bed sets the respawn point`(env: Env) {
        val instance = setupInstance(env, BedInteractionRule())
        val bed = Block.RED_BED.withProperty("part", "head").withProperty("facing", "north")
        instance.setBlock(0, 40, 0, bed)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(player.respawnPoint.samePoint(Pos(0.5, 40.1, 0.5)))
    }

    @Test
    fun `sleeping in a bed swings the hand`(env: Env) {
        val instance = setupInstance(env, BedInteractionRule())
        val bed = Block.RED_BED.withProperty("part", "head").withProperty("facing", "north")
        instance.setBlock(0, 40, 0, bed)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        val swings = connection.trackIncoming(SwingAnimationPacket::class.java)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        swings.assertSingle()
    }

    // --- Respawn anchors ----------------------------------------------------------------------

    @Test
    fun `glowstone charges a respawn anchor`(env: Env) {
        val instance = setupInstance(env, RespawnAnchorInteractionRule())
        val anchor = Block.RESPAWN_ANCHOR.withProperty("charges", "2")
        instance.setBlock(0, 40, 0, anchor)
        val player = actor(env, instance)
        holdMaterial(player, Material.GLOWSTONE)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("3", instance.getBlock(0, 40, 0).getProperty(States.CHARGES))
        assertEquals(Material.AIR, player.itemInMainHand.material())
    }

    @Test
    fun `a charged anchor consumes the click instead of placing the held block`(env: Env) {
        val instance = setupInstance(env, RespawnAnchorInteractionRule())
        instance.setExplosionSupplier { x, y, z, strength, _ -> NoopExplosion(x, y, z, strength) }
        val anchor = Block.RESPAWN_ANCHOR.withProperty("charges", "4")
        instance.setBlock(0, 40, 0, anchor)
        val player = actor(env, instance)
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    /** An explosion that breaks nothing, so tests can exercise the anchor's overworld path. */
    private class NoopExplosion(x: Float, y: Float, z: Float, strength: Float) : Explosion(x, y, z, strength) {
        override fun prepare(instance: Instance): List<Point> = emptyList()
    }

    // --- Cake ---------------------------------------------------------------------------------

    @Test
    fun `eating cake advances bites`(env: Env) {
        val instance = setupInstance(env, CakeInteractionRule())
        val cake = Block.CAKE.withProperty("bites", "0")
        instance.setBlock(0, 40, 0, cake)
        val player = actor(env, instance)
        player.food = 10

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty(States.BITES))
        assertEquals(12, player.food)
    }

    @Test
    fun `a creative player can eat cake while full`(env: Env) {
        val instance = setupInstance(env, CakeInteractionRule())
        val cake = Block.CAKE.withProperty("bites", "0")
        instance.setBlock(0, 40, 0, cake)
        val player = actor(env, instance)
        player.gameMode = GameMode.CREATIVE

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty(States.BITES))
    }

    @Test
    fun `placing a candle on a cake makes candle cake`(env: Env) {
        val instance = setupInstance(env, CakeInteractionRule())
        val cake = Block.CAKE.withProperty("bites", "0")
        instance.setBlock(0, 40, 0, cake)
        val player = actor(env, instance)
        holdMaterial(player, Material.WHITE_CANDLE)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.WHITE_CANDLE_CAKE.key(), instance.getBlock(0, 40, 0).key())
    }

    @Test
    fun `eating candle cake ejects the candle`(env: Env) {
        val instance = setupInstance(env, CandleCakeInteractionRule())
        val candleCake = Block.CANDLE_CAKE.withProperty("lit", "false")
        instance.setBlock(0, 40, 0, candleCake)
        val player = actor(env, instance)
        player.food = 10

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        val result = instance.getBlock(0, 40, 0)
        assertEquals(Block.CAKE.key(), result.key())
        assertEquals("1", result.getProperty(States.BITES))
        assertTrue(instance.entities.any { it is ItemEntity })
    }

    // --- Composters ---------------------------------------------------------------------------

    @Test
    fun `composter always fills on the first item`(env: Env) {
        val instance = setupInstance(env, ComposterInteractionRule())
        val composter = Block.COMPOSTER.withProperty("level", "0")
        instance.setBlock(0, 40, 0, composter)
        val player = actor(env, instance)
        holdMaterial(player, Material.WHEAT_SEEDS)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty(States.LEVEL))
    }

    @Test
    fun `full composter yields bone meal`(env: Env) {
        val instance = setupInstance(env, ComposterInteractionRule())
        val composter = Block.COMPOSTER.withProperty("level", "8")
        instance.setBlock(0, 40, 0, composter)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("0", instance.getBlock(0, 40, 0).getProperty(States.LEVEL))
        assertTrue(instance.entities.any { it is ItemEntity })
    }

    @Test
    fun `full composter yields bone meal even with a full hand`(env: Env) {
        val instance = setupInstance(env, ComposterInteractionRule())
        val composter = Block.COMPOSTER.withProperty("level", "8")
        instance.setBlock(0, 40, 0, composter)
        val player = actor(env, instance)
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("0", instance.getBlock(0, 40, 0).getProperty(States.LEVEL))
        assertTrue(instance.entities.any { it is ItemEntity })
        assertTrue(event.isBlockingItemUse)
    }

    // --- Jukeboxes ----------------------------------------------------------------------------

    @Test
    fun `jukebox ejects its record`(env: Env) {
        val instance = setupInstance(env, JukeboxInteractionRule())
        val record = ItemStack.of(Material.DIAMOND).toItemNBT(MinecraftServer.getRegistries())
        val jukebox = Block.JUKEBOX
            .withProperty("has_record", "true")
            .withNbt(CompoundBinaryTag.builder().put("RecordItem", record).build())
        instance.setBlock(0, 40, 0, jukebox)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty(States.HAS_RECORD))
    }

    // --- Beehives -----------------------------------------------------------------------------

    @Test
    fun `shearing a full beehive drops honeycomb`(env: Env) {
        val instance = setupInstance(env, BeehiveInteractionRule())
        val hive = Block.BEEHIVE.withProperty("honey_level", "5")
        instance.setBlock(0, 40, 0, hive)
        val player = actor(env, instance)
        holdMaterial(player, Material.SHEARS)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("0", instance.getBlock(0, 40, 0).getProperty(States.HONEY_LEVEL))
        assertTrue(instance.entities.any { it is ItemEntity })
    }

    // --- Berries ------------------------------------------------------------------------------

    @Test
    fun `harvesting a grown sweet berry bush resets its age`(env: Env) {
        val instance = setupInstance(env, SweetBerryBushInteractionRule())
        val bush = Block.SWEET_BERRY_BUSH.withProperty("age", "3")
        instance.setBlock(0, 40, 0, bush)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("1", instance.getBlock(0, 40, 0).getProperty(States.AGE))
    }

    @Test
    fun `harvesting cave vines clears their berries`(env: Env) {
        val instance = setupInstance(env, CaveVinesInteractionRule())
        val vines = Block.CAVE_VINES.withProperty("berries", "true")
        instance.setBlock(0, 40, 0, vines)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty(States.BERRIES))
    }

    @Test
    fun `bone meal grows berries on a berryless cave vine`(env: Env) {
        val instance = setupInstance(env, CaveVinesInteractionRule())
        val vines = Block.CAVE_VINES.withProperty("berries", "false")
        instance.setBlock(0, 40, 0, vines)
        val player = actor(env, instance)
        holdMaterial(player, Material.BONE_MEAL)

        val event = fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("true", instance.getBlock(0, 40, 0).getProperty(States.BERRIES))
        assertTrue(event.isBlockingItemUse)
    }

    @Test
    fun `harvesting a cave vine body clears its berries`(env: Env) {
        val instance = setupInstance(env, CaveVinesInteractionRule())
        val vines = Block.CAVE_VINES_PLANT.withProperty("berries", "true")
        instance.setBlock(0, 40, 0, vines)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("false", instance.getBlock(0, 40, 0).getProperty(States.BERRIES))
    }

    @Test
    fun `bone meal grows an ungrown sweet berry bush instead of harvesting it`(env: Env) {
        val instance = setupInstance(env, SweetBerryBushInteractionRule())
        val bush = Block.SWEET_BERRY_BUSH.withProperty("age", "2")
        instance.setBlock(0, 40, 0, bush)
        val player = actor(env, instance)
        holdMaterial(player, Material.BONE_MEAL)

        val event = fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals("3", instance.getBlock(0, 40, 0).getProperty(States.AGE))
        assertTrue(event.isBlockingItemUse)
        assertEquals(Material.AIR, player.itemInMainHand.material())
    }

    // --- Dragon eggs --------------------------------------------------------------------------

    @Test
    fun `using a dragon egg teleports it`(env: Env) {
        val instance = setupInstance(env, DragonEggInteractionRule())
        instance.setBlock(0, 40, 0, Block.DRAGON_EGG)
        val player = actor(env, instance)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        assertEquals(Block.AIR, instance.getBlock(0, 40, 0))
    }

    @Test
    fun `using a dragon egg spawns portal particles`(env: Env) {
        val instance = setupInstance(env, DragonEggInteractionRule())
        instance.setBlock(0, 40, 0, Block.DRAGON_EGG)
        val connection = env.createConnection()
        val player = connection.connect(instance, Pos(0.0, 40.0, 0.0, 0f, 0f))
        val particles = connection.trackIncoming(ParticlePacket::class.java)

        fireInteract(instance, player, BlockFace.TOP, BlockVec(0, 40, 0))

        particles.assertAny()
    }
}
