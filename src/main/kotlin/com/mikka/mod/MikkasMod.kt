package com.mikka.mod

import com.mikka.mod.block.ModBlocks
import com.mikka.mod.item.ModItems
import com.mikka.mod.logic.AmbientSoundSystem
import com.mikka.mod.logic.SanitySystem
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

import com.mikka.mod.entity.ModEntities
import com.mikka.mod.world.gen.BackroomsChunkGenerator
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object MikkasMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("mikkas-mod")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello from the backrooms!")
        SanitySystem.register()
        AmbientSoundSystem.register()
        ModBlocks.registerBlocks()
        ModItems.registerItems()
        ModEntities.registerEntities()
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms"), BackroomsChunkGenerator.CODEC)
	}
}