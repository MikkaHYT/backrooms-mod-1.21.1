package com.mikka.mod.item

import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block

class CorruptedSwordItem : SwordItem(
    object : Tier {
        override fun getUses(): Int = 2500
        override fun getSpeed(): Float = 10.0f
        override fun getAttackDamageBonus(): Float = 12.0f // Base is 4, so 4+12=16
        override fun getIncorrectBlocksForDrops(): TagKey<Block> = BlockTags.INCORRECT_FOR_NETHERITE_TOOL
        override fun getEnchantmentValue(): Int = 22
        override fun getRepairIngredient(): Ingredient = Ingredient.EMPTY
    },
    object : net.minecraft.world.item.Item.Properties() {
        init {
            attributes(SwordItem.createAttributes(object : Tier {
                 override fun getUses(): Int = 2500
                 override fun getSpeed(): Float = 10.0f
                 override fun getAttackDamageBonus(): Float = 12.0f
                 override fun getIncorrectBlocksForDrops(): TagKey<Block> = BlockTags.INCORRECT_FOR_NETHERITE_TOOL
                 override fun getEnchantmentValue(): Int = 22
                 override fun getRepairIngredient(): Ingredient = Ingredient.EMPTY
            }, 3, -2.0f)) // Attack Speed: 4.0 base + (-2.0) = 2.0
        }
    }
) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        tooltipComponents.add(Component.literal("§5§k|||§r §4§lCORRUPTED§r §5§k|||"))
        tooltipComponents.add(Component.literal("§7A blade glitching between realities."))
        tooltipComponents.add(Component.literal("§7It hums with unstable energy."))
    }
}
