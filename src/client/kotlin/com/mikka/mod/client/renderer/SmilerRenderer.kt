package com.mikka.mod.client.renderer

import com.mikka.mod.entity.SmilerEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.model.EndermanModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.resources.ResourceLocation

class SmilerRenderer(context: EntityRendererProvider.Context) : MobRenderer<SmilerEntity, EndermanModel<SmilerEntity>>(context, EndermanModel(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5f) {

    private val TEXTURE = ResourceLocation.fromNamespaceAndPath("mikkas-mod", "textures/entity/smiler.png")

    override fun getTextureLocation(entity: SmilerEntity): ResourceLocation {
        return TEXTURE
    }
    
    // To make the body invisible but eyes visible, we rely on the texture transparency.
    // The texture generated has a transparent background.
    // Standard MobRenderer supports transparency if the texture has it.
}
