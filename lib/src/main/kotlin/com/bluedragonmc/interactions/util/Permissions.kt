package com.bluedragonmc.interactions.util

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

/** Whether [player] is allowed to build (not adventure or spectator). */
fun mayBuild(player: Player): Boolean =
    player.gameMode != GameMode.ADVENTURE && player.gameMode != GameMode.SPECTATOR
