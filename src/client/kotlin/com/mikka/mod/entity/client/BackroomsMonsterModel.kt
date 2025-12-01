package com.mikka.mod.entity.client

import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.world.entity.monster.Monster

class BackroomsMonsterModel<T : Monster>(root: ModelPart) : HumanoidModel<T>(root) {

    override fun setupAnim(entity: T, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)
        
        // Scale factors
        val headScale = 0.8f
        val bodyScaleX = 0.6f
        val bodyScaleY = 1.5f // Taller body
        val limbScaleX = 0.5f
        val limbScaleY = 2.2f // Very long limbs

        // Apply scaling
        this.head.xScale = headScale
        this.head.yScale = headScale
        this.head.zScale = headScale
        
        this.hat.xScale = headScale
        this.hat.yScale = headScale
        this.hat.zScale = headScale
        
        this.body.xScale = bodyScaleX
        this.body.zScale = bodyScaleX
        this.body.yScale = bodyScaleY
        
        this.rightArm.xScale = limbScaleX
        this.rightArm.zScale = limbScaleX
        this.rightArm.yScale = limbScaleY
        
        this.leftArm.xScale = limbScaleX
        this.leftArm.zScale = limbScaleX
        this.leftArm.yScale = limbScaleY
        
        this.rightLeg.xScale = limbScaleX
        this.rightLeg.zScale = limbScaleX
        this.rightLeg.yScale = limbScaleY
        
        this.leftLeg.xScale = limbScaleX
        this.leftLeg.zScale = limbScaleX
        this.leftLeg.yScale = limbScaleY

        // Fix positions to close gaps and align height
        // Arms: Move closer to the thinner body
        this.rightArm.x = -3.0f
        this.leftArm.x = 3.0f
        
        // Legs: Move down to match the longer body, and closer together
        // Body height is 12 * 1.5 = 18.0
        this.rightLeg.y = 18.0f
        this.leftLeg.y = 18.0f
        this.rightLeg.x = -1.5f
        this.leftLeg.x = 1.5f
    }

    companion object {
        fun createBodyLayer(): LayerDefinition {
            val meshDefinition = MeshDefinition()
            val partDefinition = meshDefinition.root

            // Standard Humanoid Definition (Steve)
            // This ensures standard skins map correctly
            
            partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f))
            partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.5f)), PartPose.offset(0.0f, 0.0f, 0.0f))
            partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f))
            partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)), PartPose.offset(-5.0f, 2.0f, 0.0f))
            partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)), PartPose.offset(5.0f, 2.0f, 0.0f))
            partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)), PartPose.offset(-1.9f, 12.0f, 0.0f))
            partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)), PartPose.offset(1.9f, 12.0f, 0.0f))

            return LayerDefinition.create(meshDefinition, 64, 64)
        }
    }
}
