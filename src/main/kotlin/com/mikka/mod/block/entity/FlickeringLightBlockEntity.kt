package com.mikka.mod.block.entity

import com.mikka.mod.block.FlickeringLightBlock
import com.mikka.mod.block.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class FlickeringLightBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlocks.FLICKERING_LIGHT_ENTITY, pos, state) {

    private var flickerTimer = 0
    private var nextFlickerAt = 200 // Default 10 seconds

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, entity: FlickeringLightBlockEntity) {
            if (level.isClientSide) return

            entity.flickerTimer++
            
            val isLit = state.getValue(FlickeringLightBlock.LIT)
            
            if (entity.flickerTimer >= entity.nextFlickerAt) {
                // Toggle the light
                level.setBlock(pos, state.setValue(FlickeringLightBlock.LIT, !isLit), 3)
                entity.flickerTimer = 0
                
                if (isLit) {
                    // Was lit, now turning off - stay off for a short time (1-3 seconds)
                    entity.nextFlickerAt = 20 + level.random.nextInt(40)
                } else {
                    // Was off, now turning on - stay on for longer (10-30 seconds)
                    entity.nextFlickerAt = 200 + level.random.nextInt(400)
                }
            }
        }
    }
}
