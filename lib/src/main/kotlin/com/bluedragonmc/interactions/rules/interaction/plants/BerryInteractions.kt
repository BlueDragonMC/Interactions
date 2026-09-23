package com.bluedragonmc.interactions.rules.interaction.plants

import com.bluedragonmc.interactions.util.blockCenter
import com.bluedragonmc.interactions.util.dropItemStack
import com.bluedragonmc.interactions.util.playBlockSound
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.WorldEventPacket
import net.minestom.server.sound.SoundEvent
import net.minestom.server.worldevent.WorldEvent
import java.util.concurrent.ThreadLocalRandom

/** The block action used to show the plant growth particles when bone mealing. */
internal const val BONE_MEAL_PARTICLES = 15

/** Plays the bone meal growth particles and sound at the interacted block. */
internal fun sendBoneMealParticles(event: PlayerBlockInteractEvent) {
    event.instance.sendGroupedPacket(
        WorldEventPacket(
            WorldEvent.PARTICLES_AND_SOUND_PLANT_GROWTH.id(),
            event.blockPosition,
            BONE_MEAL_PARTICLES,
            false,
        ),
    )
}

/** Drops [amount] of [material] and plays the harvest [sound] with a randomized pitch. */
internal fun harvest(event: PlayerBlockInteractEvent, material: Material, amount: Int, sound: SoundEvent) {
    event.instance.dropItemStack(event.blockPosition, ItemStack.of(material, amount))
    event.instance.playBlockSound(
        sound,
        event.blockPosition.blockCenter(),
        pitch = ThreadLocalRandom.current().nextFloat(0.8f, 1.2f),
    )
}
