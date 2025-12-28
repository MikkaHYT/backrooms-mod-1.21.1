package com.mikka.mod.client

import com.mikka.mod.world.dimension.ModDimensions
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.FogRenderer
import com.mojang.blaze3d.systems.RenderSystem

object BackroomsFogRenderer {
    
    private const val FOG_START = 5.0f    // Fog starts very close
    private const val FOG_END = 60.0f     // Fog fully opaque at this distance
    
    // Yellowish fog color to match the backrooms aesthetic
    private const val FOG_RED = 0.85f
    private const val FOG_GREEN = 0.82f
    private const val FOG_BLUE = 0.65f
    
    fun register() {
        WorldRenderEvents.START.register { context ->
            val client = Minecraft.getInstance()
            val player = client.player
            
            if (player != null) {
                val isBackrooms = player.level().dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY
                
                if (isBackrooms) {
                    // Apply custom fog settings
                    RenderSystem.setShaderFogStart(FOG_START)
                    RenderSystem.setShaderFogEnd(FOG_END)
                    RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE, 1.0f)
                }
            }
        }
        
        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            val client = Minecraft.getInstance()
            val player = client.player
            
            if (player != null) {
                val isBackrooms = player.level().dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY
                
                if (isBackrooms) {
                    // Maintain fog during translucent rendering
                    RenderSystem.setShaderFogStart(FOG_START)
                    RenderSystem.setShaderFogEnd(FOG_END)
                    RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE, 1.0f)
                }
            }
        }
    }
}
