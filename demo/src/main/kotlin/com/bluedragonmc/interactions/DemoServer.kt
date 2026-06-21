package com.bluedragonmc.interactions

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block

private const val HOSTNAME = "127.0.0.1"
private const val PORT = 25565

// Start this server with "./gradlew :demo:run"
@Suppress("unused")
class DemoServer {
    fun main() {
        val server = MinecraftServer.init()

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        instance.setChunkSupplier(::LightingChunk)
        instance.setGenerator { unit ->
            val start: Point = unit.absoluteStart()
            val size: Point = unit.size()
            for (x in 0..<size.blockX()) {
                for (z in 0..<size.blockZ()) {
                    unit.modifier().setBlock(start.add(x.toDouble(), 63.0, z.toDouble()), Block.GLASS)
                }
            }
        }

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            event.spawningInstance = instance
            event.player.gameMode = GameMode.CREATIVE
        }

        for (handler in Interactions.getBlockHandlers()) {
            MinecraftServer.getBlockManager().registerHandler(handler.key) { handler }
        }

        MinecraftServer.getGlobalEventHandler().addChild(Interactions.events())

        server.start(HOSTNAME, PORT)
        println("Minecraft ${MinecraftServer.VERSION_NAME} server started on $HOSTNAME:$PORT!")
    }
}
