package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.Rule
import com.bluedragonmc.interactions.RuleRegistry
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.listener.BlockPlacementListener
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket
import net.minestom.server.network.packet.server.play.SoundEffectPacket
import net.minestom.testing.Collector
import net.minestom.testing.Env

internal fun setupInstance(env: Env, vararg rules: Rule): Instance {
    val instance = env.createFlatInstance()
    instance.loadChunk(0, 0).join()
    val registry = RuleRegistry()
    rules.forEach(registry::register)
    instance.eventNode().addChild(registry.eventNode())
    return instance
}

/**
 * Creates a player and starts collecting the [SoundEffectPacket]s sent to it, so tests can assert on
 * the sounds an interaction plays. Use this for the observer when the acting player is excluded.
 */
internal fun trackSounds(env: Env, instance: Instance, pos: Pos): Collector<SoundEffectPacket> {
    val connection = env.createConnection()
    connection.connect(instance, pos)
    return connection.trackIncoming(SoundEffectPacket::class.java)
}

internal fun firePlace(
    instance: Instance,
    player: Player,
    block: Block,
    face: BlockFace,
    position: BlockVec,
    cursor: Vec = Vec(0.5, 0.5, 0.5),
): PlayerBlockPlaceEvent {
    val event = PlayerBlockPlaceEvent(player, instance, block, face, position, cursor, PlayerHand.MAIN)
    EventDispatcher.call(event)
    return event
}

internal fun fireInteract(
    instance: Instance,
    player: Player,
    face: BlockFace,
    position: BlockVec,
    cursor: Vec = Vec(0.5, 0.5, 0.5),
): PlayerBlockInteractEvent {
    val clickedBlock = instance.getBlock(position)
    val event = PlayerBlockInteractEvent(player, PlayerHand.MAIN, instance, clickedBlock, position, cursor, face)
    EventDispatcher.call(event)
    return event
}

/** Runs an item use with no block target, as the client does for, e.g., an empty bucket on a fluid. */
internal fun fireUseItem(player: Player): PlayerUseItemEvent {
    val event = PlayerUseItemEvent(player, PlayerHand.MAIN, player.getItemInHand(PlayerHand.MAIN), 0L)
    EventDispatcher.call(event)
    return event
}

internal fun holdBlock(player: Player, block: Block) {
    player.setItemInHand(PlayerHand.MAIN, ItemStack.of(block.material()!!))
}

internal fun holdItem(player: Player, item: ItemStack) {
    player.setItemInHand(PlayerHand.MAIN, item)
}

internal fun holdMaterial(player: Player, material: Material) {
    holdItem(player, ItemStack.of(material))
}

/** Runs Minestom's real placement listener, as a client click would. */
internal fun simulatePlace(
    player: Player,
    block: Block,
    clicked: BlockVec,
    face: BlockFace,
) {
    holdBlock(player, block)
    BlockPlacementListener.listener(
        ClientPlayerBlockPlacementPacket(
            PlayerHand.MAIN, clicked, face,
            0.5f, 0.5f, 0.5f,
            false, false, 1,
        ),
        player,
    )
}
