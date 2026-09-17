package com.bluedragonmc.interactions.rules

import com.bluedragonmc.interactions.PlacementRule
import com.bluedragonmc.interactions.PlacementRuleRegistry
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
import com.bluedragonmc.interactions.rules.redstone.RepeaterRule
import com.bluedragonmc.interactions.rules.structural.SlabPlacementRule
import com.bluedragonmc.interactions.rules.structural.StairsPlacementRule
import com.bluedragonmc.interactions.rules.structural.TrapdoorRule
import com.bluedragonmc.interactions.rules.wall.BannerRule
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
 * val registry = PlacementRuleRegistry()
 * RuleCatalog.registerAll(registry)
 * MinecraftServer.getGlobalEventHandler().addChild(registry.eventNode())
 * ```
 */
object RuleCatalog {

    /** Creates a fresh list of every rule shipped by the library. */
    fun createRules(): List<PlacementRule> = listOf(
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
    )

    /** Creates and registers every rule in this catalog into [registry]. */
    fun registerAll(registry: PlacementRuleRegistry) {
        createRules().forEach(registry::register)
    }
}
