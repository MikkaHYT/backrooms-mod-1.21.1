package com.mikka.mod.mixin

import com.mikka.mod.block.BackroomsPortalBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(FireBlock::class)
class FireBlockMixin {
    @Inject(method = ["onPlace"], at = [At("HEAD")])
    private fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean, ci: CallbackInfo) {
        BackroomsPortalBlock.trySpawnPortal(level, pos)
    }
}
