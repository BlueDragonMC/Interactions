package com.bluedragonmc.interactions.rules.interaction

import com.bluedragonmc.interactions.BlockHandlers
import com.bluedragonmc.interactions.container.ITEMS_TAG
import com.bluedragonmc.interactions.container.withContainerItems
import com.bluedragonmc.interactions.rules.base.fireInteract
import com.bluedragonmc.interactions.rules.base.holdBlock
import com.bluedragonmc.interactions.rules.base.setupInstance
import com.bluedragonmc.interactions.rules.interaction.misc.ContainerInteractionRule
import net.kyori.adventure.nbt.BinaryTagTypes
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnvTest
class ContainerInteractionRuleTest {

    private fun openChest(env: Env, instance: Instance): Pair<Player, Inventory> {
        instance.setBlock(0, 40, 0, Block.CHEST)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        val event = fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))
        assertTrue(event.isBlockingItemUse)
        val inventory = player.openInventory
        assertNotNull(inventory)
        return player to inventory as Inventory
    }

    @Test
    fun `right clicking a chest opens a 27 slot menu`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())

        val (_, inventory) = openChest(env, instance)

        assertEquals(InventoryType.CHEST_3_ROW, inventory.inventoryType)
        assertEquals(27, inventory.size)
    }

    @Test
    fun `sneaking with an empty hand still opens a container`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.CHEST)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        player.isSneaking = true

        val event = fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertTrue(event.isBlockingItemUse)
        assertNotNull(player.openInventory)
    }

    @Test
    fun `sneaking with an item bypasses the container`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.CHEST)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        player.isSneaking = true
        holdBlock(player, Block.STONE)

        val event = fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertFalse(event.isBlockingItemUse)
        assertNull(player.openInventory)
    }

    @Test
    fun `items already on the block are shown in the menu`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val chest = Block.CHEST.withContainerItems(listOf(ItemStack.of(Material.GOLD_INGOT, 2)))
        instance.setBlock(0, 40, 0, chest)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        val inventory = player.openInventory as Inventory
        assertEquals(Material.GOLD_INGOT, inventory.getItemStack(0).material())
        assertEquals(2, inventory.getItemStack(0).amount())
    }

    @Test
    fun `items placed in the menu are written back to the block`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val (_, inventory) = openChest(env, instance)

        inventory.setItemStack(3, ItemStack.of(Material.DIAMOND, 5))

        val items = instance.getBlock(0, 40, 0).nbtOrEmpty().getList(ITEMS_TAG, BinaryTagTypes.COMPOUND)
        assertEquals(1, items.size())
        val entry = items.getCompound(0)
        assertEquals(3, entry.getInt("Slot"))
        assertEquals("minecraft:diamond", entry.getString("id"))
        assertEquals(5, entry.getInt("count"))
    }

    @Test
    fun `reopening a container reloads the stored items`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val (player, inventory) = openChest(env, instance)
        inventory.setItemStack(0, ItemStack.of(Material.REDSTONE, 7))
        player.closeInventory()

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        val reopened = player.openInventory as Inventory
        assertEquals(Material.REDSTONE, reopened.getItemStack(0).material())
        assertEquals(7, reopened.getItemStack(0).amount())
    }

    @Test
    fun `barrel uses a 27 slot menu`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.BARREL)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        assertEquals(InventoryType.CHEST_3_ROW, (player.openInventory as Inventory).inventoryType)
    }

    @Test
    fun `shulker box uses the shulker menu`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.SHULKER_BOX)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))

        val inventory = player.openInventory as Inventory
        assertEquals(InventoryType.SHULKER_BOX, inventory.inventoryType)
        assertEquals(27, inventory.size)
    }

    @Test
    fun `dispenser and hopper map to their own menus`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.DISPENSER)
        instance.setBlock(1, 40, 0, Block.HOPPER)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(0, 40, 0))
        assertEquals(InventoryType.WINDOW_3X3, (player.openInventory as Inventory).inventoryType)

        fireInteract(instance, player, BlockFace.NORTH, BlockVec(1, 40, 0))
        assertEquals(InventoryType.HOPPER, (player.openInventory as Inventory).inventoryType)
    }

    @Test
    fun `breaking a container drops its contents`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val chest = Block.CHEST.withContainerItems(listOf(ItemStack.of(Material.EMERALD, 4)))
        instance.setBlock(0, 40, 0, chest)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        EventDispatcher.call(
            PlayerBlockBreakEvent(player, instance, chest, Block.AIR, BlockVec(0, 40, 0), BlockFace.NORTH),
        )

        val drops = instance.entities.filterIsInstance<ItemEntity>()
        assertEquals(1, drops.size)
        assertEquals(Material.EMERALD, drops.first().itemStack.material())
        assertEquals(4, drops.first().itemStack.amount())
    }

    @Test
    fun `breaking an empty container drops nothing`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        instance.setBlock(0, 40, 0, Block.CHEST)
        val player = env.createPlayer(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))

        EventDispatcher.call(
            PlayerBlockBreakEvent(player, instance, Block.CHEST, Block.AIR, BlockVec(0, 40, 0), BlockFace.NORTH),
        )

        assertTrue(instance.entities.filterIsInstance<ItemEntity>().isEmpty())
    }

    @Test
    fun `the client is sent the container contents as block entity data`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val handler = BlockHandlers.getAll().first { it.key == Block.CHEST.key() }
        MinecraftServer.getBlockManager().registerHandler(handler.key) { handler }

        val connection = env.createConnection()
        connection.connect(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        val packets = connection.trackIncoming(BlockEntityDataPacket::class.java)

        val chest = Block.CHEST
            .withContainerItems(listOf(ItemStack.of(Material.DIAMOND, 5)))
            .withHandler(handler)
        instance.setBlock(0, 40, 0, chest)

        packets.assertAnyMatch { packet ->
            packet.blockPosition.blockX() == 0 && packet.blockPosition.blockY() == 40 && packet.blockPosition.blockZ() == 0 &&
                packet.data?.getList(ITEMS_TAG, BinaryTagTypes.COMPOUND)?.size() == 1
        }
    }

    @Test
    fun `the client is sent the container contents even without a block handler`(env: Env) {
        val instance = setupInstance(env, ContainerInteractionRule())
        val connection = env.createConnection()
        connection.connect(instance, Pos(0.5, 40.0, 0.5, 0f, 0f))
        val packets = connection.trackIncoming(BlockEntityDataPacket::class.java)

        val chest = Block.CHEST.withContainerItems(listOf(ItemStack.of(Material.DIAMOND, 5)))
        instance.setBlock(0, 40, 0, chest)

        packets.assertAnyMatch { packet ->
            packet.blockPosition.blockX() == 0 && packet.blockPosition.blockY() == 40 && packet.blockPosition.blockZ() == 0 &&
                packet.data?.getList(ITEMS_TAG, BinaryTagTypes.COMPOUND)?.size() == 1
        }
    }
}
