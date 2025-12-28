package com.mikka.mod.item

import com.mikka.mod.block.ModBlocks
import com.mikka.mod.entity.ModEntities
import com.mikka.mod.world.dimension.ModDimensions
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import kotlin.math.atan2
import kotlin.math.sqrt

class BackroomsCompassItem : Item(Properties().stacksTo(1)) {
    
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        
        if (!level.isClientSide && level is ServerLevel) {
            val isBackrooms = level.dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY
            
            if (!isBackrooms) {
                player.sendSystemMessage(Component.literal("The compass spins wildly... It only works in the Backrooms.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, 0.5f)
                return InteractionResultHolder.fail(stack)
            }
            
            // Search for nearest escape portal
            val escapePortalPos = findNearestEscapePortal(level, player.blockPosition())
            
            // Search for nearest boss
            val bossPos = findNearestBoss(level, player.blockPosition())
            
            // Report findings
            player.sendSystemMessage(Component.literal("=== Backrooms Compass ===").withStyle(ChatFormatting.GOLD))
            
            if (escapePortalPos != null) {
                val distance = sqrt(player.blockPosition().distSqr(escapePortalPos)).toInt()
                val direction = getDirectionString(player.blockPosition(), escapePortalPos)
                player.sendSystemMessage(Component.literal("◈ Escape Portal: ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("$distance blocks $direction").withStyle(ChatFormatting.WHITE)))
            } else {
                player.sendSystemMessage(Component.literal("◈ Escape Portal: ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Not detected nearby").withStyle(ChatFormatting.GRAY)))
            }
            
            if (bossPos != null) {
                val distance = sqrt(player.blockPosition().distSqr(bossPos)).toInt()
                val direction = getDirectionString(player.blockPosition(), bossPos)
                player.sendSystemMessage(Component.literal("◈ Boss Room: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal("$distance blocks $direction").withStyle(ChatFormatting.WHITE)))
            } else {
                // Calculate nearest boss room based on spacing
                val nearestBossRoom = findNearestBossRoomCenter(player.blockPosition())
                val distance = sqrt(player.blockPosition().distSqr(nearestBossRoom)).toInt()
                val direction = getDirectionString(player.blockPosition(), nearestBossRoom)
                player.sendSystemMessage(Component.literal("◈ Boss Room: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal("~$distance blocks $direction (estimated)").withStyle(ChatFormatting.GRAY)))
            }
            
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.5f)
            player.cooldowns.addCooldown(this, 100) // 5 second cooldown
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }
    
    private fun findNearestEscapePortal(level: ServerLevel, center: BlockPos): BlockPos? {
        val searchRadius = 100
        var nearestPos: BlockPos? = null
        var nearestDistSq = Double.MAX_VALUE
        
        // Search in a radius for escape portal blocks
        for (x in -searchRadius..searchRadius step 4) {
            for (y in -32..64 step 8) {
                for (z in -searchRadius..searchRadius step 4) {
                    val pos = center.offset(x, y, z)
                    if (level.getBlockState(pos).`is`(ModBlocks.ESCAPE_PORTAL)) {
                        val distSq = center.distSqr(pos)
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq
                            nearestPos = pos
                        }
                    }
                }
            }
        }
        
        return nearestPos
    }
    
    private fun findNearestBoss(level: ServerLevel, center: BlockPos): BlockPos? {
        val searchBox = AABB.ofSize(center.center, 200.0, 100.0, 200.0)
        val bosses = level.getEntitiesOfClass(com.mikka.mod.entity.BackroomsBossEntity::class.java, searchBox)
        
        if (bosses.isEmpty()) return null
        
        return bosses.minByOrNull { it.distanceToSqr(center.x.toDouble(), center.y.toDouble(), center.z.toDouble()) }?.blockPosition()
    }
    
    private fun findNearestBossRoomCenter(playerPos: BlockPos): BlockPos {
        val bossRoomSpacing = 500
        val bossRoomX = Math.round(playerPos.x.toFloat() / bossRoomSpacing) * bossRoomSpacing
        val bossRoomZ = Math.round(playerPos.z.toFloat() / bossRoomSpacing) * bossRoomSpacing
        return BlockPos(bossRoomX, playerPos.y, bossRoomZ)
    }
    
    private fun getDirectionString(from: BlockPos, to: BlockPos): String {
        val dx = to.x - from.x
        val dz = to.z - from.z
        
        if (dx == 0 && dz == 0) return "here"
        
        val angle = Math.toDegrees(atan2(dz.toDouble(), dx.toDouble()))
        
        return when {
            angle >= -22.5 && angle < 22.5 -> "East (→)"
            angle >= 22.5 && angle < 67.5 -> "Southeast (↘)"
            angle >= 67.5 && angle < 112.5 -> "South (↓)"
            angle >= 112.5 && angle < 157.5 -> "Southwest (↙)"
            angle >= 157.5 || angle < -157.5 -> "West (←)"
            angle >= -157.5 && angle < -112.5 -> "Northwest (↖)"
            angle >= -112.5 && angle < -67.5 -> "North (↑)"
            else -> "Northeast (↗)"
        }
    }
    
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        tooltipComponents.add(Component.literal("Right-click to locate escape portals").withStyle(ChatFormatting.GRAY))
        tooltipComponents.add(Component.literal("and boss rooms in the Backrooms.").withStyle(ChatFormatting.GRAY))
        tooltipComponents.add(Component.literal("Only works in the Backrooms dimension.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
    }
    
    override fun isFoil(stack: ItemStack): Boolean {
        return true // Enchanted glint effect
    }
}
