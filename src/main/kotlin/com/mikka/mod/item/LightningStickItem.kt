package com.mikka.mod.item

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class LightningStickItem : Item(Properties().stacksTo(1)) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!level.isClientSide) {
            // Long-range raycast (up to 100 blocks)
            val start = player.eyePosition
            val look = player.lookAngle
            val end = start.add(look.x * 100.0, look.y * 100.0, look.z * 100.0)
            val hitResult = level.clip(ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
            
            if (hitResult.type == HitResult.Type.BLOCK || hitResult.type == HitResult.Type.ENTITY) {
                val pos = hitResult.location
                val lightning = EntityType.LIGHTNING_BOLT.create(level)
                if (lightning != null) {
                    lightning.moveTo(pos)
                    level.addFreshEntity(lightning)
                }
                player.cooldowns.addCooldown(this, 20) // 1 second cooldown
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand))
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        tooltipComponents.add(Component.literal("§e§lTHUNDERLORD'S COMMAND"))
        tooltipComponents.add(Component.literal("§7Right-click to smite your enemies."))
        tooltipComponents.add(Component.literal("§7Power of the storm in your hand."))
    }
}
