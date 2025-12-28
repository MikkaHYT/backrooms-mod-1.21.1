package com.mikka.mod.client.renderer

import com.mikka.mod.entity.SkinStealerEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.resources.ResourceLocation
import java.util.UUID

class SkinStealerRenderer(context: EntityRendererProvider.Context) : 
    LivingEntityRenderer<SkinStealerEntity, PlayerModel<SkinStealerEntity>>(
        context,
        PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false),
        0.5f
    ) {
    
    private val skinCache = mutableMapOf<UUID, ResourceLocation>()
    
    override fun render(
        entity: SkinStealerEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        // Make it slightly transparent when stalking
        if (entity.isStalking()) {
            // Add subtle flickering effect during stalking
            val flicker = if ((entity.tickCount / 5) % 2 == 0) 0.7f else 0.9f
            // Note: transparency would require custom rendering, for now just render normally
        }
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }
    
    override fun getTextureLocation(entity: SkinStealerEntity): ResourceLocation {
        val mimickedUUID = entity.getMimickedPlayerUUID()
        
        if (mimickedUUID != null) {
            // Check cache first
            skinCache[mimickedUUID]?.let { return it }
            
            // Try to get the skin from the player if they're online
            val client = Minecraft.getInstance()
            val level = client.level
            
            if (level != null) {
                val player = level.getPlayerByUUID(mimickedUUID)
                if (player != null) {
                    // Get the player's skin
                    val skinManager = client.skinManager
                    val profile = player.gameProfile
                    
                    try {
                        val skin = skinManager.getInsecureSkin(profile)
                        val texture = skin.texture
                        skinCache[mimickedUUID] = texture
                        return texture
                    } catch (e: Exception) {
                        // Fall through to default
                    }
                }
            }
            
            // Return default skin based on UUID
            return DefaultPlayerSkin.get(mimickedUUID).texture
        }
        
        // Default skin for entities that haven't mimicked anyone yet
        return DefaultPlayerSkin.getDefaultTexture()
    }
    
    override fun scale(entity: SkinStealerEntity, poseStack: PoseStack, partialTickTime: Float) {
        // Same size as a player
        poseStack.scale(0.9375f, 0.9375f, 0.9375f)
    }
}
