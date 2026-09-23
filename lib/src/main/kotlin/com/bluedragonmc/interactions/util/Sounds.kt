package com.bluedragonmc.interactions.util

import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.sound.SoundEvent

/** Plays a block-sourced sound at [position] to everyone in the instance. */
fun Instance.playBlockSound(
    sound: SoundEvent,
    position: Point,
    volume: Float = 1f,
    pitch: Float = 1f,
) = playSound(Sound.sound(sound, Sound.Source.BLOCK, volume, pitch), position)

/** Plays a block-sourced sound at [position] to everyone except [player]. */
fun Instance.playBlockSoundExcept(
    player: Player,
    sound: SoundEvent,
    position: Point,
    volume: Float = 1f,
    pitch: Float = 1f,
) = playSoundExcept(
    player,
    Sound.sound(sound, Sound.Source.BLOCK, volume, pitch),
    position.x(),
    position.y(),
    position.z(),
)

/** The centre of [position], the usual origin for block sounds and effects. */
fun Point.blockCenter() = add(0.5, 0.5, 0.5)
