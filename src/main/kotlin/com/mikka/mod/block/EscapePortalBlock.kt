package com.mikka.mod.block

import com.mikka.mod.world.dimension.ModDimensions
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.level.portal.DimensionTransition
import net.minecraft.world.phys.Vec3

class EscapePortalBlock(properties: Properties) : Block(properties) {
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (!level.isClientSide && level is ServerLevel && player is ServerPlayer) {
            val serverLevel = level.server.getLevel(Level.OVERWORLD)
            if (serverLevel != null) {
                // Grant the escaped advancement
                val advancement = level.server.advancements.get(ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms/escaped"))
                if (advancement != null) {
                    val progress = player.advancements.getOrStartProgress(advancement)
                    if (!progress.isDone) {
                        progress.remainingCriteria.forEach { criterion ->
                            player.advancements.award(advancement, criterion)
                        }
                    }
                }
                
                val spawnPos = serverLevel.sharedSpawnPos
                val transition = DimensionTransition(
                    serverLevel,
                    Vec3.atBottomCenterOf(spawnPos),
                    Vec3.ZERO,
                    0f,
                    0f,
                    DimensionTransition.DO_NOTHING
                )
                player.changeDimension(transition)
                return InteractionResult.SUCCESS
            }
        }
        return InteractionResult.CONSUME
    }
}
