package com.mikka.mod.entity.client

import com.mikka.mod.entity.BackroomsBossEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.resources.ResourceLocation

class BackroomsBossRenderer(context: EntityRendererProvider.Context) : HumanoidMobRenderer<BackroomsBossEntity, BackroomsMonsterModel<BackroomsBossEntity>>(context, BackroomsMonsterModel(context.bakeLayer(ModModelLayers.BACKROOMS_MONSTER)), 1.0f) {
    
    override fun scale(entity: BackroomsBossEntity, poseStack: PoseStack, partialTick: Float) {
        poseStack.scale(3.0f, 3.0f, 3.0f) // Huge Boss
    }

    override fun getTextureLocation(entity: BackroomsBossEntity): ResourceLocation {
        // Use the same texture for now, or a red tinted one if available. 
        // Since I can't generate a new texture file easily without user input, I'll use the monster one.
        return ResourceLocation.fromNamespaceAndPath("mikkas-mod", "textures/entity/backrooms_monster.png")
    }
}
