package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.RuleRegistry
import com.bluedragonmc.interactions.Rule
import com.bluedragonmc.interactions.rules.connecting.ChainRule
import com.bluedragonmc.interactions.rules.connecting.FenceGateRule
import com.bluedragonmc.interactions.rules.connecting.IronBarsRule
import com.bluedragonmc.interactions.rules.connecting.NetherBrickFenceRule
import com.bluedragonmc.interactions.rules.connecting.WallRule
import com.bluedragonmc.interactions.rules.connecting.WoodenFenceRule
import com.bluedragonmc.interactions.rules.directional.AmethystClusterPlacementRule
import com.bluedragonmc.interactions.rules.directional.BarrelPlacementRule
import com.bluedragonmc.interactions.rules.directional.BeehivePlacementRule
import com.bluedragonmc.interactions.rules.directional.CopperGolemStatuePlacementRule
import com.bluedragonmc.interactions.rules.directional.DispenserPlacementRule
import com.bluedragonmc.interactions.rules.directional.EnderChestPlacementRule
import com.bluedragonmc.interactions.rules.directional.EndPortalFramePlacementRule
import com.bluedragonmc.interactions.rules.directional.EndRodPlacementRule
import com.bluedragonmc.interactions.rules.directional.FurnacePlacementRule
import com.bluedragonmc.interactions.rules.directional.GlazedTerracottaPlacementRule
import com.bluedragonmc.interactions.rules.directional.LightningRodPlacementRule
import com.bluedragonmc.interactions.rules.directional.LogAxisRule
import com.bluedragonmc.interactions.rules.directional.ObserverPlacementRule
import com.bluedragonmc.interactions.rules.directional.PillarPlacementRule
import com.bluedragonmc.interactions.rules.directional.PistonPlacementRule
import com.bluedragonmc.interactions.rules.directional.PumpkinPlacementRule
import com.bluedragonmc.interactions.rules.interaction.BucketRule
import com.bluedragonmc.interactions.rules.interaction.FireRule
import com.bluedragonmc.interactions.rules.interaction.FireStarterRule
import com.bluedragonmc.interactions.rules.interaction.FlowerPotRule
import com.bluedragonmc.interactions.rules.interaction.PumpkinCarvingRule
import com.bluedragonmc.interactions.rules.interaction.SpawnerRule
import com.bluedragonmc.interactions.rules.interaction.connecting.FenceGateInteractionRule
import com.bluedragonmc.interactions.rules.interaction.misc.CandleInteractionRule
import com.bluedragonmc.interactions.rules.interaction.redstone.ButtonInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.DoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.structural.TrapdoorInteractionRule
import com.bluedragonmc.interactions.rules.interaction.wall.LeverInteractionRule
import com.bluedragonmc.interactions.rules.misc.AnvilRule
import com.bluedragonmc.interactions.rules.misc.CalibratedSculkSensorRule
import com.bluedragonmc.interactions.rules.misc.CampfireRule
import com.bluedragonmc.interactions.rules.misc.CandlePlacementRule
import com.bluedragonmc.interactions.rules.misc.CarpetRule
import com.bluedragonmc.interactions.rules.misc.ChestRule
import com.bluedragonmc.interactions.rules.misc.CommandBlockRule
import com.bluedragonmc.interactions.rules.misc.FacingWorkstationRule
import com.bluedragonmc.interactions.rules.misc.GrindstoneRule
import com.bluedragonmc.interactions.rules.misc.HopperRule
import com.bluedragonmc.interactions.rules.misc.LeafLitterRule
import com.bluedragonmc.interactions.rules.misc.ScaffoldingRule
import com.bluedragonmc.interactions.rules.misc.SeaPicklePlacementRule
import com.bluedragonmc.interactions.rules.misc.ShelfRule
import com.bluedragonmc.interactions.rules.misc.ShulkerBoxRule
import com.bluedragonmc.interactions.rules.misc.SnowRule
import com.bluedragonmc.interactions.rules.misc.TurtleEggPlacementRule
import com.bluedragonmc.interactions.rules.misc.CreakingHeartRule
import com.bluedragonmc.interactions.rules.misc.CrafterRule
import com.bluedragonmc.interactions.rules.misc.FarmlandRule
import com.bluedragonmc.interactions.rules.misc.HugeMushroomRule
import com.bluedragonmc.interactions.rules.misc.JigsawRule
import com.bluedragonmc.interactions.rules.misc.PaleHangingMossRule
import com.bluedragonmc.interactions.rules.misc.PaleMossCarpetRule
import com.bluedragonmc.interactions.rules.misc.PotentSulfurRule
import com.bluedragonmc.interactions.rules.misc.ShelfMushroomRule
import com.bluedragonmc.interactions.rules.misc.SnowyRule
import com.bluedragonmc.interactions.rules.misc.WaterloggedRule
import com.bluedragonmc.interactions.rules.multiblock.BedRule
import com.bluedragonmc.interactions.rules.multiblock.BigDripleafRule
import com.bluedragonmc.interactions.rules.multiblock.DoorRule
import com.bluedragonmc.interactions.rules.multiblock.PitcherCropRule
import com.bluedragonmc.interactions.rules.multiblock.SmallDripleafRule
import com.bluedragonmc.interactions.rules.multiblock.TallPlantRule
import com.bluedragonmc.interactions.rules.plants.BambooRule
import com.bluedragonmc.interactions.rules.plants.CactusRule
import com.bluedragonmc.interactions.rules.plants.CaveVinesRule
import com.bluedragonmc.interactions.rules.plants.ChorusPlantRule
import com.bluedragonmc.interactions.rules.plants.CocoaRule
import com.bluedragonmc.interactions.rules.plants.CoralFanRule
import com.bluedragonmc.interactions.rules.plants.CoralRule
import com.bluedragonmc.interactions.rules.plants.CropRule
import com.bluedragonmc.interactions.rules.plants.FlowerBedRule
import com.bluedragonmc.interactions.rules.plants.KelpRule
import com.bluedragonmc.interactions.rules.plants.MultifaceVeinRule
import com.bluedragonmc.interactions.rules.plants.NetherWartRule
import com.bluedragonmc.interactions.rules.plants.PointedDripstoneRule
import com.bluedragonmc.interactions.rules.plants.SaplingRule
import com.bluedragonmc.interactions.rules.plants.StemRule
import com.bluedragonmc.interactions.rules.plants.SugarCaneRule
import com.bluedragonmc.interactions.rules.plants.SulfurSpikeRule
import com.bluedragonmc.interactions.rules.plants.SweetBerryBushRule
import com.bluedragonmc.interactions.rules.plants.TwistingVinesRule
import com.bluedragonmc.interactions.rules.plants.VineRule
import com.bluedragonmc.interactions.rules.plants.WeepingVinesRule
import com.bluedragonmc.interactions.rules.redstone.ComparatorRule
import com.bluedragonmc.interactions.rules.redstone.PressurePlateRule
import com.bluedragonmc.interactions.rules.redstone.RailRule
import com.bluedragonmc.interactions.rules.redstone.RedstoneWireRule
import com.bluedragonmc.interactions.rules.redstone.RepeaterRule
import com.bluedragonmc.interactions.rules.redstone.TripwireHookRule
import com.bluedragonmc.interactions.rules.redstone.TripwireRule
import com.bluedragonmc.interactions.rules.structural.SlabPlacementRule
import com.bluedragonmc.interactions.rules.structural.StairsPlacementRule
import com.bluedragonmc.interactions.rules.structural.TrapdoorRule
import com.bluedragonmc.interactions.rules.wall.BannerRule
import com.bluedragonmc.interactions.rules.wall.BellRule
import com.bluedragonmc.interactions.rules.wall.HangingSignRule
import com.bluedragonmc.interactions.rules.wall.LadderRule
import com.bluedragonmc.interactions.rules.wall.LanternRule
import com.bluedragonmc.interactions.rules.wall.LeverRule
import com.bluedragonmc.interactions.rules.wall.SignRule
import com.bluedragonmc.interactions.rules.wall.SkullRule
import com.bluedragonmc.interactions.rules.wall.TorchRule

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
    )

    /** Creates and registers every rule in this catalog into [registry]. */
    fun registerAll(registry: RuleRegistry) {
        createRules().forEach(registry::register)
    }
}
