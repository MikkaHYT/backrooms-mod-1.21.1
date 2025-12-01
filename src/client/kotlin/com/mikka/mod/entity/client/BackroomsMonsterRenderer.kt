package com.mikka.mod.entity.client

import com.mikka.mod.entity.BackroomsMonsterEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.resources.ResourceLocation

class BackroomsMonsterRenderer(context: EntityRendererProvider.Context) : HumanoidMobRenderer<BackroomsMonsterEntity, BackroomsMonsterModel<BackroomsMonsterEntity>>(context, BackroomsMonsterModel(context.bakeLayer(ModModelLayers.BACKROOMS_MONSTER)), 0.5f) {
    
    override fun scale(entity: BackroomsMonsterEntity, poseStack: PoseStack, partialTick: Float) {
        poseStack.scale(1.8f, 1.8f, 1.8f)
    }

    override fun getTextureLocation(entity: BackroomsMonsterEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath("mikkas-mod", "textures/entity/backrooms_monster.png")
    }
}
