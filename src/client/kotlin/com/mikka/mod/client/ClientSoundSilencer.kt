package com.mikka.mod.client

object ClientSoundSilencer {
    var isSilenced = false
    private var silenceVotes = 0

    fun tick() {
        isSilenced = false
        val player = net.minecraft.client.Minecraft.getInstance().player ?: return
        val level = player.level()
        val pos = player.blockPosition()
        
        // Scan radius 10 for unlit flickering lights
        val radius = 10
        for (x in -radius..radius) {
            for (y in -5..5) {
                for (z in -radius..radius) {
                    val checkPos = pos.offset(x, y, z)
                    val state = level.getBlockState(checkPos)
                    if (state.block == com.mikka.mod.block.ModBlocks.FLICKERING_LIGHT) {
                        if (!state.getValue(com.mikka.mod.block.FlickeringLightBlock.LIT)) {
                            isSilenced = true
                            return
                        }
                    }
                }
            }
        }
    }

    fun requestSilence() {
        // No longer used, logic moved to tick()
    }
}
