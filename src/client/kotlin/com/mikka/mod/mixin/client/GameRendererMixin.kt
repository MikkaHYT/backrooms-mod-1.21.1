package com.mikka.mod.mixin.client

import com.mikka.mod.entity.BackroomsMonsterEntity
import com.mikka.mod.world.dimension.ModDimensions
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(GameRenderer::class)
class GameRendererMixin {

    @Inject(method = ["getFov"], at = [At("RETURN")], cancellable = true)
    private fun onGetFov(camera: net.minecraft.client.Camera, partialTicks: Float, useFOVSetting: Boolean, cir: CallbackInfoReturnable<Double>) {
        val mc = Minecraft.getInstance()
        if (mc.player != null && mc.level != null) {
            if (mc.player!!.level().dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY) {
                val box = mc.player!!.boundingBox.inflate(15.0)
                val monsters = mc.level!!.getEntitiesOfClass(BackroomsMonsterEntity::class.java, box)
                
                for (monster in monsters) {
                    if (monster.isAggressive) {
                        // Calculate FOV increase
                        val dist = monster.distanceTo(mc.player!!)
                        var factor = 1.0 - (dist / 15.0) // 0 at 15 blocks, 1 at 0 blocks
                        if (factor < 0) factor = 0.0
                        
                        val currentFov = cir.returnValue
                        val newFov = currentFov * (1.0 + (factor * 0.3)) // Up to 30% increase
                        cir.returnValue = newFov
                        return // Only apply for the closest one
                    }
                }
            }
        }
    }
}
