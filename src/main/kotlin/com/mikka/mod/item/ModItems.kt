package com.mikka.mod.item

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

object ModItems {
    val CORRUPTED_SWORD = register("corrupted_sword", CorruptedSwordItem())
    val LIGHTNING_STICK = register("lightning_stick", LightningStickItem())
    val BACKROOMS_COMPASS = register("backrooms_compass", BackroomsCompassItem())
    
    val FLICKERING_LIGHT_ITEM = register("flickering_light", net.minecraft.world.item.BlockItem(com.mikka.mod.block.ModBlocks.FLICKERING_LIGHT, net.minecraft.world.item.Item.Properties()))
    val MOIST_CARPET_ITEM = register("moist_carpet", net.minecraft.world.item.BlockItem(com.mikka.mod.block.ModBlocks.MOIST_CARPET, net.minecraft.world.item.Item.Properties()))
    val YELLOW_WALLPAPER_VAR1_ITEM = register("yellow_wallpaper_var1", net.minecraft.world.item.BlockItem(com.mikka.mod.block.ModBlocks.YELLOW_WALLPAPER_VAR1, net.minecraft.world.item.Item.Properties()))
    val YELLOW_WALLPAPER_VAR2_ITEM = register("yellow_wallpaper_var2", net.minecraft.world.item.BlockItem(com.mikka.mod.block.ModBlocks.YELLOW_WALLPAPER_VAR2, net.minecraft.world.item.Item.Properties()))

    private fun register(name: String, item: Item): Item {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("mikkas-mod", name), item)
    }

    fun registerItems() {
        // Initialize
    }
}
