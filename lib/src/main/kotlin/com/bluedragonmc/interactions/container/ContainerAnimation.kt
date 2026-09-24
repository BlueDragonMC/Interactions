package com.bluedragonmc.interactions.container

/** How a container tells clients that it has opened or closed. */
enum class ContainerAnimation {
    /** No client-side animation. */
    NONE,

    /** Chests and shulker boxes: signal the block entity so the client animates its lid. */
    LID,

    /** Barrels: toggle the `open` block property, which clients render directly. */
    BLOCK_STATE,
}
