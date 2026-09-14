package com.bluedragonmc.interactions.internal

import com.bluedragonmc.interactions.PlacementContext
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.event.BlockUpdateEvent
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.instance.InstanceBlockUpdateEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.instance.block.BlockHandler

/**
 * Translates Minestom's placement system into this library's event-based one.
 *
 * This is the only place that touches Minestom's block placement internals.
 */
internal class PlacementHandler(
    private val lookup: (Block) -> PlacementRule?,
) {
    val node: EventNode<InstanceEvent> = EventNode.type("interactions:placement-handler", EventFilter.INSTANCE)

    /** Guards against re-entering the update drain from block writes performed by the drain itself. */
    private val draining = ThreadLocal.withInitial { false }

    init {
        node.addListener(PlayerBlockPlaceEvent::class.java, ::handlePlacement)
        node.addListener(InstanceBlockUpdateEvent::class.java, ::handleBlockUpdate)
    }

    private fun handlePlacement(event: PlayerBlockPlaceEvent) {
        val rule = lookup(event.block) ?: return

        val minestomPosition = event.blockPosition
        val clickedPosition = minestomPosition.relative(event.blockFace.oppositeFace).toBlockVec()
        val clickedBlock = event.instance.getBlock(clickedPosition)
        val context = PlacementContext(
            instance = event.instance,
            player = event.player,
            hand = event.hand,
            block = event.block,
            blockFace = event.blockFace,
            cursorPosition = event.cursorPosition,
            clickedPosition = clickedPosition,
            clickedBlock = clickedBlock,
            placementPosition = minestomPosition,
            isSneaking = event.player.isSneaking,
        )

        val intendedPosition = rule.getPlacementPosition(context).toBlockVec()
        val placementEvent = BlockPlacementEvent(
            player = event.player,
            instance = event.instance,
            hand = event.hand,
            blockFace = event.blockFace,
            cursorPosition = event.cursorPosition,
            clickedPosition = clickedPosition,
            clickedBlock = clickedBlock,
            position = intendedPosition,
            block = event.block,
            consumeBlock = event.doesConsumeBlock(),
            context = context,
        )

        rule.eventNode.call(placementEvent)

        if (placementEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        val extras = placementEvent.extraPlacements()
        if (intendedPosition != minestomPosition || extras.isNotEmpty()) {
            // Minestom would place at the wrong position, so take over the placement entirely.
            event.isCancelled = true
            placeManually(event, placementEvent, intendedPosition, extras)
        } else {
            event.block = placementEvent.block
            event.consumeBlock(placementEvent.consumeBlock)
        }
    }

    private fun placeManually(
        minestomEvent: PlayerBlockPlaceEvent,
        placementEvent: BlockPlacementEvent,
        intendedPosition: BlockVec,
        extras: List<Pair<BlockVec, Block>>,
    ) {
        val instance = placementEvent.instance
        val player = placementEvent.player
        val hand = placementEvent.hand
        val cursor = placementEvent.cursorPosition
        val doBlockUpdates = minestomEvent.shouldDoBlockUpdates()

        instance.placeBlock(
            BlockHandler.PlayerPlacement(
                placementEvent.block,
                instance.getBlock(intendedPosition),
                instance,
                intendedPosition,
                player,
                hand,
                placementEvent.blockFace,
                cursor.x().toFloat(),
                cursor.y().toFloat(),
                cursor.z().toFloat(),
            ),
            doBlockUpdates,
        )
        for ((position, block) in extras) {
            instance.placeBlock(
                BlockHandler.PlayerPlacement(
                    block, instance.getBlock(position), instance, position,
                    player, hand, placementEvent.blockFace, 0f, 0f, 0f,
                ),
                doBlockUpdates,
            )
        }

        if (placementEvent.consumeBlock) {
            player.setItemInHand(hand, player.getItemInHand(hand).consume(1))
        }
    }

    private fun handleBlockUpdate(event: InstanceBlockUpdateEvent) {
        if (draining.get()) return
        draining.set(true)
        try {
            drain(event.instance, event.blockPosition)
        } finally {
            draining.set(false)
        }
    }

    /**
     * Breadth-first propagation of neighbor updates starting from [origin].
     *
     * Propagation is bounded by each rule's [PlacementRule.maxUpdateDistance].
     */
    private fun drain(instance: Instance, origin: BlockVec) {
        val queue = ArrayDeque<Pair<BlockVec, Int>>()
        queue.addLast(origin to 0)
        var budget = MAX_UPDATES_PER_DRAIN

        while (queue.isNotEmpty() && budget-- > 0) {
            val (position, distance) = queue.removeFirst()
            for (face in BlockFace.entries) {
                val neighbor = position.relative(face)
                if (!instance.isChunkLoaded(neighbor)) continue
                val neighborBlock = instance.getBlock(neighbor) ?: continue
                if (neighborBlock.isAir) continue

                val rule = lookup(neighborBlock) ?: continue
                if (distance >= rule.maxUpdateDistance()) continue

                val updateEvent = BlockUpdateEvent(
                    instance = instance,
                    position = neighbor.toBlockVec(),
                    block = neighborBlock,
                    fromFace = face.oppositeFace,
                )
                rule.eventNode.call(updateEvent)

                if (updateEvent.resultBlock != neighborBlock) {
                    instance.setBlock(neighbor, updateEvent.resultBlock)
                    queue.addLast(neighbor.toBlockVec() to distance + 1)
                }
            }
        }
    }

    private fun Point.toBlockVec(): BlockVec = BlockVec(blockX(), blockY(), blockZ())

    private companion object {
        const val MAX_UPDATES_PER_DRAIN = 4096
    }
}
