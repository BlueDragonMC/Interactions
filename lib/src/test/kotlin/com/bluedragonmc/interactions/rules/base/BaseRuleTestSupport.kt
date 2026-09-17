package com.bluedragonmc.interactions.rules.base

import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.PlacementRuleRegistry
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.item.ItemStack
import net.minestom.server.listener.BlockPlacementListener
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket
import net.minestom.testing.Env

internal fun setupInstance(env: Env, vararg rules: PlacementRule): Instance {
    val instance = env.createFlatInstance()
    instance.loadChunk(0, 0).join()
    val registry = PlacementRuleRegistry()
    rules.forEach(registry::register)
    instance.eventNode().addChild(registry.eventNode())
    return instance
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
    clickedBlock: Block,
    face: BlockFace,
    position: BlockVec,
    cursor: Vec = Vec(0.5, 0.5, 0.5),
): PlayerBlockInteractEvent {
    val event = PlayerBlockInteractEvent(player, PlayerHand.MAIN, instance, clickedBlock, position, cursor, face)
    EventDispatcher.call(event)
    return event
}

internal fun holdBlock(player: Player, block: Block) {
    player.setItemInHand(PlayerHand.MAIN, ItemStack.of(block.material()!!))
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
