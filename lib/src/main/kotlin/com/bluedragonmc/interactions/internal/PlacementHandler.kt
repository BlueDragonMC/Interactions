package com.bluedragonmc.interactions.internal

import com.bluedragonmc.interactions.PlacementContext
import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.event.BlockUpdateEvent
import com.bluedragonmc.interactions.util.States
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.instance.InstanceBlockUpdateEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
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
        node.addListener(PlayerBlockInteractEvent::class.java, ::handleInteract)
        node.addListener(PlayerBlockBreakEvent::class.java, ::handleBreak)
        node.addListener(InstanceBlockUpdateEvent::class.java, ::handleBlockUpdate)
    }

    private fun handlePlacement(event: PlayerBlockPlaceEvent) {
        val rule = lookup(event.block) ?: return

        val minestomPosition = event.blockPosition
        val clickedPosition = minestomPosition.relative(event.blockFace.oppositeFace).toBlockVec()
        val clickedBlock = event.instance.getBlock(clickedPosition)
        val context = buildContext(
            instance = event.instance,
            player = event.player,
            hand = event.hand,
            block = event.block,
            blockFace = event.blockFace,
            cursorPosition = event.cursorPosition,
            clickedPosition = clickedPosition,
            clickedBlock = clickedBlock,
            defaultPosition = minestomPosition,
        )

        val placementEvent = callRule(rule, context, event.doesConsumeBlock())
        if (placementEvent.isCancelled) {
            event.isCancelled = true
            return
        }

        val intendedPosition = placementEvent.blockPosition
        val extras = placementEvent.extraPlacements()
        if (intendedPosition != minestomPosition || extras.isNotEmpty()) {
            // Minestom would place at the wrong position, so take over the placement entirely.
            event.isCancelled = true
            place(
                instance = event.instance,
                player = event.player,
                hand = event.hand,
                blockFace = event.blockFace,
                cursorPosition = event.cursorPosition,
                position = intendedPosition,
                block = placementEvent.block,
                extras = extras,
                consumeBlock = placementEvent.consumeBlock,
                doBlockUpdates = event.shouldDoBlockUpdates(),
            )
        } else {
            event.block = placementEvent.block
            event.consumeBlock(placementEvent.consumeBlock)
        }
    }

    /**
     * Manually trigger placement events for scenarios when the player
     * places a block inside another, because Minestom doesn't trigger events for those.
     */
    private fun handleInteract(event: PlayerBlockInteractEvent) {
        val player = event.player
        val instance = event.instance
        val hand = event.hand
        val clickedBlock = event.block
        // Air and replaceable targets are handled by Minestom's normal placement event path.
        if (clickedBlock.air()) return
        // Spectators cannot place; adventure placement is gated by the held item's can_place_on component.
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.CREATIVE) return

        val heldBlock = player.getItemInHand(hand).material().block() ?: return
        val rule = lookup(heldBlock) ?: return

        val clickedPosition = event.blockPosition
        val targetPosition = clickedPosition.relative(event.blockFace)
        if (instance.getBlock(targetPosition).replaceable()) return

        val context = buildContext(
            instance = instance,
            player = player,
            hand = hand,
            block = heldBlock,
            blockFace = event.blockFace,
            cursorPosition = event.cursorPosition,
            clickedPosition = clickedPosition,
            clickedBlock = clickedBlock,
            defaultPosition = targetPosition,
        )

        // The engine resolves a replaceable clicked block to its own position; otherwise the rule has
        // to explicitly accept merging into the occupied target.
        val replacingClicked = context.placementPosition != targetPosition
        if (!replacingClicked && !rule.canHandleOccupiedPlacement(context)) return

        val placementEvent = callRule(rule, context, player.gameMode != GameMode.CREATIVE)
        if (placementEvent.isCancelled) return

        // Block Minestom's interaction handling so it does not reject the placement, then apply it.
        event.isBlockingItemUse = true
        place(
            instance = instance,
            player = player,
            hand = hand,
            blockFace = event.blockFace,
            cursorPosition = event.cursorPosition,
            position = placementEvent.blockPosition,
            block = placementEvent.block,
            extras = placementEvent.extraPlacements(),
            consumeBlock = placementEvent.consumeBlock,
            doBlockUpdates = true,
        )
    }

    /**
     * Builds a new placement context, modifying the placement position to account for
     * blocks that can be replaced when targeted, like short grass.
     */
    private fun buildContext(
        instance: Instance,
        player: Player,
        hand: PlayerHand,
        block: Block,
        blockFace: BlockFace,
        cursorPosition: Point,
        clickedPosition: BlockVec,
        clickedBlock: Block,
        defaultPosition: BlockVec,
    ): PlacementContext {
        val replacingClicked = !clickedBlock.air() &&
            clickedBlock.replaceable() &&
            !player.isSneaking &&
            clickedBlock.key() != block.key()
        return PlacementContext(
            instance = instance,
            player = player,
            hand = hand,
            block = block,
            blockFace = blockFace,
            cursorPosition = cursorPosition,
            clickedPosition = clickedPosition,
            clickedBlock = clickedBlock,
            placementPosition = if (replacingClicked) clickedPosition else defaultPosition,
            isSneaking = player.isSneaking,
        )
    }

    private fun callRule(
        rule: PlacementRule,
        context: PlacementContext,
        consumeBlock: Boolean,
    ): BlockPlacementEvent {
        val intendedPosition = rule.getPlacementPosition(context).toBlockVec()
        val placementEvent = BlockPlacementEvent(
            player = context.player,
            instance = context.instance,
            hand = context.hand,
            blockFace = context.blockFace,
            cursorPosition = context.cursorPosition,
            clickedPosition = context.clickedPosition,
            clickedBlock = context.clickedBlock,
            position = intendedPosition,
            block = context.block,
            consumeBlock = consumeBlock,
            context = context,
        )
        rule.eventNode.call(placementEvent)
        return placementEvent
    }

    /**
     * Breaking a waterlogged block leaves its water behind instead of air.
     */
    private fun handleBreak(event: PlayerBlockBreakEvent) {
        if (event.block.getProperty(States.WATERLOGGED) == "true") {
            event.resultBlock = Block.WATER
        }
    }

    private fun place(
        instance: Instance,
        player: Player,
        hand: PlayerHand,
        blockFace: BlockFace,
        cursorPosition: Point,
        position: BlockVec,
        block: Block,
        extras: List<Pair<BlockVec, Block>>,
        consumeBlock: Boolean,
        doBlockUpdates: Boolean,
    ) {
        instance.placeBlock(
            BlockHandler.PlayerPlacement(
                block,
                instance.getBlock(position),
                instance,
                position,
                player,
                hand,
                blockFace,
                cursorPosition.x().toFloat(),
                cursorPosition.y().toFloat(),
                cursorPosition.z().toFloat(),
            ),
            doBlockUpdates,
        )
        for ((extraPosition, extraBlock) in extras) {
            instance.placeBlock(
                BlockHandler.PlayerPlacement(
                    extraBlock, instance.getBlock(extraPosition), instance, extraPosition,
                    player, hand, blockFace, 0f, 0f, 0f,
                ),
                doBlockUpdates,
            )
        }

        if (consumeBlock) {
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
                if (neighborBlock.air()) continue

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
