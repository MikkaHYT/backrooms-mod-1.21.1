package com.mikka.mod

import com.mikka.mod.client.BackroomsFogRenderer
import com.mikka.mod.client.renderer.SkinStealerRenderer
import com.mikka.mod.entity.ModEntities
import com.mikka.mod.entity.client.BackroomsBossRenderer
import com.mikka.mod.entity.client.BackroomsMonsterModel
import com.mikka.mod.entity.client.BackroomsMonsterRenderer
import com.mikka.mod.entity.client.ModModelLayers
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import com.mikka.mod.client.renderer.SmilerRenderer

object MikkasModClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        EntityRendererRegistry.register(ModEntities.BACKROOMS_MONSTER, ::BackroomsMonsterRenderer)
        EntityRendererRegistry.register(ModEntities.BACKROOMS_BOSS, ::BackroomsBossRenderer)
        EntityRendererRegistry.register(ModEntities.SMILER, ::SmilerRenderer)
        EntityRendererRegistry.register(ModEntities.SKIN_STEALER, ::SkinStealerRenderer)
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.BACKROOMS_MONSTER) { BackroomsMonsterModel.createBodyLayer() }
        
        // Register fog renderer for backrooms dimension
        BackroomsFogRenderer.register()
        
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register { 
            com.mikka.mod.client.ClientSoundSilencer.tick()
        }
    }
}