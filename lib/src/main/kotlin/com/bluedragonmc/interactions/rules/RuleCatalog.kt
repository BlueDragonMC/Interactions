package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.Rule
import com.bluedragonmc.interactions.RuleRegistry
import com.bluedragonmc.interactions.rules.connecting.*
import com.bluedragonmc.interactions.rules.directional.*
import com.bluedragonmc.interactions.rules.interaction.*
import com.bluedragonmc.interactions.rules.interaction.connecting.FenceGateInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.CandleInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.ButtonInteractionRule
import com.bluedragonmc.interactions.rules.interaction.sign.SignInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.DoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.TrapdoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.wall.LeverInteractionRule
import com.bluedragonmc.interactions.rules.misc.*
import com.bluedragonmc.interactions.rules.multiblock.*
import com.bluedragonmc.interactions.rules.plants.*
import com.bluedragonmc.interactions.rules.redstone.*
import com.bluedragonmc.interactions.rules.structural.SlabPlacementRule
import com.bluedragonmc.interactions.rules.structural.StairsPlacementRule
import com.bluedragonmc.interactions.rules.structural.TrapdoorRule
import com.bluedragonmc.interactions.rules.wall.*

/**
 * Every rule shipped by this library, and the entry point for registering all of them at once.
 *
 * ```kotlin
 * val registry = RuleRegistry()
 * RuleCatalog.registerAll(registry)
 * MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())
 * ```
 */
object RuleCatalog {

    /** Creates a fresh list of every rule shipped by the library. */
    fun createRules(): List<Rule> = listOf(
        ButtonPlacementRule(),

        // Directional / axis
        LogAxisRule(),
        PillarPlacementRule(),
        FurnacePlacementRule(),
        PumpkinPlacementRule(),
        GlazedTerracottaPlacementRule(),
        BeehivePlacementRule(),
        EnderChestPlacementRule(),
        EndPortalFramePlacementRule(),
        CopperGolemStatuePlacementRule(),
        BarrelPlacementRule(),
        PistonPlacementRule(),
        DispenserPlacementRule(),
        ObserverPlacementRule(),
        AmethystClusterPlacementRule(),
        LightningRodPlacementRule(),
        EndRodPlacementRule(),

        // Slabs / stairs / trapdoors
        SlabPlacementRule(),
        StairsPlacementRule(),
        TrapdoorRule(),

        // Wall-mounted
        TorchRule(),
        LeverRule(),
        LanternRule(),
        LadderRule(),
        SignRule(),
        HangingSignRule(),
        BannerRule(),
        SkullRule(),
        BellRule(),

        // Connecting
        WoodenFenceRule(),
        NetherBrickFenceRule(),
        IronBarsRule(),
        WallRule(),
        ChainRule(),
        FenceGateRule(),

        // Plants and growth
        CropRule(),
        StemRule(),
        NetherWartRule(),
        SaplingRule(),
        FlowerBedRule(),
        CactusRule(),
        SugarCaneRule(),
        BambooRule(),
        SweetBerryBushRule(),
        KelpRule(),
        CaveVinesRule(),
        TwistingVinesRule(),
        WeepingVinesRule(),
        CoralRule(),
        CoralFanRule(),
        PointedDripstoneRule(),
        SulfurSpikeRule(),
        VineRule(),
        MultifaceVeinRule(),
        CocoaRule(),
        ChorusPlantRule(),

        // Multi-block
        DoorRule(),
        BedRule(),
        TallPlantRule(),
        PitcherCropRule(),
        BigDripleafRule(),
        SmallDripleafRule(),

        // Redstone
        PressurePlateRule(),
        RailRule(),
        RepeaterRule(),
        ComparatorRule(),
        RedstoneWireRule(),
        TripwireRule(),
        TripwireHookRule(),

        // Containers / workstations / misc
        ChestRule(),
        ShulkerBoxRule(),
        HopperRule(),
        CandlePlacementRule(),
        SnowRule(),
        SeaPicklePlacementRule(),
        TurtleEggPlacementRule(),
        AnvilRule(),
        GrindstoneRule(),
        CampfireRule(),
        ShelfRule(),
        FacingWorkstationRule(),
        ScaffoldingRule(),
        WaterloggedRule(),
        CarpetRule(),
        CommandBlockRule(),
        CalibratedSculkSensorRule(),
        LeafLitterRule(),
        SnowyRule(),
        FarmlandRule(),
        HugeMushroomRule(),
        CreakingHeartRule(),
        PotentSulfurRule(),
        CrafterRule(),
        JigsawRule(),
        PaleMossCarpetRule(),
        PaleHangingMossRule(),
        ShelfMushroomRule(),

        // Interactions
        FlowerPotRule(),
        PumpkinCarvingRule(),
        SpawnerRule(),
        BucketRule(),
        FireStarterRule(),
        FireRule(),

        // Block interactions (right-click)
        ButtonInteractionRule(),
        LeverInteractionRule(),
        DoorInteractionRule(),
        TrapdoorInteractionRule(),
        FenceGateInteractionRule(),
        CandleInteractionRule(),
        SignInteractionRule(),
    )

    /** Creates and registers every rule in this catalog into [registry]. */
    fun registerAll(registry: RuleRegistry) {
        createRules().forEach(registry::register)
    }
}
