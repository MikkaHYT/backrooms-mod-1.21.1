package com.mikka.mod.block

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.SoundType

import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.entity.BlockEntityType

import com.mikka.mod.block.entity.FlickeringLightBlockEntity

object ModBlocks {
    val BACKROOMS_PORTAL = register("backrooms_portal", BackroomsPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noCollission().strength(-1.0f).sound(SoundType.GLASS).lightLevel { 15 }))
    val ESCAPE_PORTAL = register("escape_portal", EscapePortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0f).noLootTable()))

    val FLICKERING_LIGHT = register("flickering_light", FlickeringLightBlock(BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.GLASS).lightLevel { state -> if (state.getValue(FlickeringLightBlock.LIT)) 15 else 0 }))
    val MOIST_CARPET = register("moist_carpet", MoistCarpetBlock(BlockBehaviour.Properties.of().strength(0.1f).sound(SoundType.MOSS_CARPET).mapColor(MapColor.COLOR_YELLOW)))
    val YELLOW_WALLPAPER_VAR1 = register("yellow_wallpaper_var1", WallpaperBlock(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.WOOD).mapColor(MapColor.COLOR_YELLOW)))
    val YELLOW_WALLPAPER_VAR2 = register("yellow_wallpaper_var2", WallpaperBlock(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.WOOD).mapColor(MapColor.COLOR_YELLOW)))

    val FLICKERING_LIGHT_ENTITY: BlockEntityType<FlickeringLightBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("mikkas-mod", "flickering_light"),
        net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(::FlickeringLightBlockEntity, FLICKERING_LIGHT).build()
    )

    private fun register(name: String, block: Block): Block {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("mikkas-mod", name), block)
    }

    fun registerBlocks() {
        // Just to initialize the class
    }
}
