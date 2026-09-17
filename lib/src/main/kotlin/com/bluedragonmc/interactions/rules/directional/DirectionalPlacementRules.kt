package com.bluedragonmc.interactions.rules.directional

import com.bluedragonmc.interactions.BlockSet
import com.bluedragonmc.interactions.event.BlockPlacementEvent
import com.bluedragonmc.interactions.rules.base.AllAxisRule
import com.bluedragonmc.interactions.rules.base.ClickFacingRule
import com.bluedragonmc.interactions.rules.base.DirectionalPlacementRule
import com.bluedragonmc.interactions.rules.base.HorizontalFacingRule
import com.bluedragonmc.interactions.rules.base.NearestFacingRule
import com.bluedragonmc.interactions.util.States
import net.minestom.server.instance.block.Block

// --- Axis / pillar ----------------------------------------------------------------------------

/** Logs, wood, stems and hyphae: `axis` follows the clicked face. */
class LogAxisRule : AllAxisRule(BlockSet.fromTag("minecraft:logs"))

/** Non-wood pillars (basalt, deepslate, froglights, ...): `axis` follows the clicked face. */
class PillarPlacementRule : AllAxisRule(
    BlockSet.from(
        Block.BAMBOO_BLOCK,
        Block.STRIPPED_BAMBOO_BLOCK,
        Block.BASALT,
        Block.POLISHED_BASALT,
        Block.BONE_BLOCK,
        Block.DEEPSLATE,
        Block.INFESTED_DEEPSLATE,
        Block.HAY_BLOCK,
        Block.MUDDY_MANGROVE_ROOTS,
        Block.OCHRE_FROGLIGHT,
        Block.PEARLESCENT_FROGLIGHT,
        Block.VERDANT_FROGLIGHT,
        Block.PURPUR_PILLAR,
        Block.QUARTZ_PILLAR,
    ),
)

// --- Horizontal facing (player yaw) -----------------------------------------------------------

/** Furnaces, blast furnaces and smokers face the player. */
class FurnacePlacementRule : HorizontalFacingRule(BlockSet.from(Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER))

/** Carved pumpkins and jack o'lanterns face the player. */
class PumpkinPlacementRule : HorizontalFacingRule(BlockSet.from(Block.CARVED_PUMPKIN, Block.JACK_O_LANTERN))

/** Glazed terracotta faces the player. */
class GlazedTerracottaPlacementRule : HorizontalFacingRule(
    BlockSet.from(
        Block.WHITE_GLAZED_TERRACOTTA,
        Block.ORANGE_GLAZED_TERRACOTTA,
        Block.MAGENTA_GLAZED_TERRACOTTA,
        Block.LIGHT_BLUE_GLAZED_TERRACOTTA,
        Block.YELLOW_GLAZED_TERRACOTTA,
        Block.LIME_GLAZED_TERRACOTTA,
        Block.PINK_GLAZED_TERRACOTTA,
        Block.GRAY_GLAZED_TERRACOTTA,
        Block.LIGHT_GRAY_GLAZED_TERRACOTTA,
        Block.CYAN_GLAZED_TERRACOTTA,
        Block.PURPLE_GLAZED_TERRACOTTA,
        Block.BLUE_GLAZED_TERRACOTTA,
        Block.BROWN_GLAZED_TERRACOTTA,
        Block.GREEN_GLAZED_TERRACOTTA,
        Block.RED_GLAZED_TERRACOTTA,
        Block.BLACK_GLAZED_TERRACOTTA,
    ),
)

/** Beehives and bee nests face the player. */
class BeehivePlacementRule : HorizontalFacingRule(BlockSet.from(Block.BEEHIVE, Block.BEE_NEST))

/** Ender chests face the player (and carry `waterlogged`). */
class EnderChestPlacementRule : HorizontalFacingRule(BlockSet.from(Block.ENDER_CHEST))

/** End portal frames face the player. */
class EndPortalFramePlacementRule : HorizontalFacingRule(BlockSet.from(Block.END_PORTAL_FRAME))

/** Copper golem statues face the player (and carry `waterlogged`). */
class CopperGolemStatuePlacementRule : HorizontalFacingRule(
    BlockSet.from(
        Block.COPPER_GOLEM_STATUE,
        Block.EXPOSED_COPPER_GOLEM_STATUE,
        Block.WEATHERED_COPPER_GOLEM_STATUE,
        Block.OXIDIZED_COPPER_GOLEM_STATUE,
        Block.WAXED_COPPER_GOLEM_STATUE,
        Block.WAXED_EXPOSED_COPPER_GOLEM_STATUE,
        Block.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
        Block.WAXED_OXIDIZED_COPPER_GOLEM_STATUE,
    ),
)

// --- Facing the player's nearest looking direction --------------------------------------------

/** Barrels face away from the player's nearest looking direction. */
class BarrelPlacementRule : NearestFacingRule(BlockSet.from(Block.BARREL))

/** Pistons and sticky pistons face away from the player's nearest looking direction. */
class PistonPlacementRule : NearestFacingRule(BlockSet.from(Block.PISTON, Block.STICKY_PISTON))

/** Dispensers and droppers face away from the player's nearest looking direction. */
class DispenserPlacementRule : NearestFacingRule(BlockSet.from(Block.DISPENSER, Block.DROPPER))

/** Observers face the same direction the player is looking. */
class ObserverPlacementRule : NearestFacingRule(BlockSet.from(Block.OBSERVER), facesPlayer = false)

// --- Clicked-face facing ----------------------------------------------------------------------

/** Amethyst clusters and buds face the clicked face. */
class AmethystClusterPlacementRule : ClickFacingRule(
    BlockSet.from(
        Block.AMETHYST_CLUSTER,
        Block.SMALL_AMETHYST_BUD,
        Block.MEDIUM_AMETHYST_BUD,
        Block.LARGE_AMETHYST_BUD,
    ),
)

/** Lightning rods face the clicked face (and carry `waterlogged`). */
class LightningRodPlacementRule : ClickFacingRule(
    BlockSet.from(
        Block.LIGHTNING_ROD,
        Block.EXPOSED_LIGHTNING_ROD,
        Block.WEATHERED_LIGHTNING_ROD,
        Block.OXIDIZED_LIGHTNING_ROD,
        Block.WAXED_LIGHTNING_ROD,
        Block.WAXED_EXPOSED_LIGHTNING_ROD,
        Block.WAXED_WEATHERED_LIGHTNING_ROD,
        Block.WAXED_OXIDIZED_LIGHTNING_ROD,
    ),
)

/**
 * End rods face the clicked face, but flip to the opposite face when the clicked block is an end
 * rod already facing the same direction.
 */
class EndRodPlacementRule : DirectionalPlacementRule(BlockSet.from(Block.END_ROD)) {

    override fun placementState(event: BlockPlacementEvent): Block {
        val clickedDirection = event.blockFace.toDirection()
        val behind = event.clickedBlock
        val facing = if (
            behind.key() == Block.END_ROD.key() &&
            behind.getProperty(States.FACING) == clickedDirection.name.lowercase()
        ) {
            clickedDirection.opposite()
        } else {
            clickedDirection
        }
        return event.block.withProperty(States.FACING, facing.name.lowercase())
    }
}
