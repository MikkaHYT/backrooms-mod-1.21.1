package com.mikka.mod.block

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CarpetBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class MoistCarpetBlock(properties: Properties) : CarpetBlock(properties) {
    override fun stepOn(level: Level, pos: BlockPos, state: BlockState, entity: Entity) {
        if (!level.isClientSide && entity is LivingEntity) {
            // Slow down
            val slowFactor = 0.8
            entity.deltaMovement = entity.deltaMovement.multiply(slowFactor, 1.0, slowFactor)
        }
        
        // Squish sound on client? stepOn is server side mostly.
        // Entity.playStepSound handles step sounds.
        // But we can play an extra squish here.
        if (level.random.nextFloat() < 0.1f) {
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.5f, 1.0f)
        }
        
        super.stepOn(level, pos, state, entity)
    }
}
