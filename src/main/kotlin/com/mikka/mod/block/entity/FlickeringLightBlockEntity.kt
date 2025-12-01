package com.mikka.mod.block.entity

import com.mikka.mod.block.FlickeringLightBlock
import com.mikka.mod.block.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class FlickeringLightBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlocks.FLICKERING_LIGHT_ENTITY, pos, state) {

    private var flickerTimer = 0

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, entity: FlickeringLightBlockEntity) {
            if (level.isClientSide) {
                val isLit = state.getValue(FlickeringLightBlock.LIT)
                if (!isLit) {
                    // Check distance to player
                    val player = net.minecraft.client.Minecraft.getInstance().player
                    if (player != null && player.distanceToSqr(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) < 256.0) { // 16 blocks
                        com.mikka.mod.client.ClientSoundSilencer.requestSilence()
                    }
                }
                return
            }

            entity.flickerTimer++
            if (entity.flickerTimer > 20 * 10 + level.random.nextInt(20 * 20)) { // Random interval 10-30s
                // Toggle
                val isLit = state.getValue(FlickeringLightBlock.LIT)
                level.setBlock(pos, state.setValue(FlickeringLightBlock.LIT, !isLit), 3)
                entity.flickerTimer = 0
                
                // If turning off, maybe stay off for a shorter time?
                if (isLit) {
                     // Was lit, now off. 
                     // Next tick will be short to turn it back on? 
                     // The requirement says "go dark at intervals". 
                     // So mostly ON, sometimes OFF.
                     entity.flickerTimer = 20 * 25 // Set timer high so it triggers sooner? No, logic is: if timer > threshold.
                     // Let's reset timer to a value that makes the "OFF" period shorter.
                     // If we want OFF to last 5 seconds:
                     // threshold is ~400. We want next trigger in 100 ticks.
                     // So set timer to 300.
                     entity.flickerTimer = 20 * 10 + level.random.nextInt(20 * 10) - 100 // Hacky.
                     // Let's make it explicit.
                }
            }
            
            // Better logic:
            // Mostly ON.
            // Randomly turn OFF.
            // When OFF, turn ON after short duration.
        }
    }
}
