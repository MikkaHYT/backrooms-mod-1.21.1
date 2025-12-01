package com.mikka.mod.mixin.client

import com.mikka.mod.client.ClientSoundSilencer
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundEngine
import net.minecraft.sounds.SoundEvents
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(SoundEngine::class)
class SoundEngineMixin {

    @Inject(method = ["play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"], at = [At("HEAD")], cancellable = true)
    fun onPlaySound(sound: SoundInstance, ci: CallbackInfo) {
        if (ClientSoundSilencer.isSilenced) {
            // Allow footsteps of monsters (Backrooms Monster, Smiler, Boss)
            // And maybe player footsteps?
            // "stop all noises except the footsteps of the backrooms monsters"
            
            val id = sound.location
            // Check if it's a monster step sound
            // Our monsters use specific step sounds or we can check the sound event.
            // But SoundInstance only gives us the location (ResourceLocation).
            
            // Backrooms Monster uses RAVAGER_STEP (minecraft:entity.ravager.step)
            // Smiler uses ?? (Placeholder was Enderman)
            
            // Let's allow specific sounds.
            val allowed = id.path.contains("step") && (id.path.contains("ravager") || id.path.contains("enderman") || id.path.contains("wither"))
            
            if (!allowed) {
                ci.cancel()
            }
        }
    }
}
