package com.mikka.mod.logic

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

object AmbientSoundSystem {
    private val playerCooldowns = mutableMapOf<java.util.UUID, Int>()
    
    // Ambient sounds that play regularly
    private val AMBIENT_SOUNDS = listOf(
        SoundEvents.AMBIENT_CAVE to 0.3f,           // Distant cave sounds
        SoundEvents.AMBIENT_BASALT_DELTAS_MOOD to 0.2f,  // Unsettling mood
    )
    
    // Random creepy sounds that play occasionally
    private val CREEPY_SOUNDS = listOf(
        SoundEvents.WOODEN_DOOR_CLOSE to 0.4f,      // Distant door
        SoundEvents.CHEST_CLOSE to 0.3f,            // Something closing
        SoundEvents.GRAVEL_STEP to 0.5f,            // Footsteps
        SoundEvents.WOOL_STEP to 0.4f,              // Carpet footsteps
        SoundEvents.ANVIL_LAND to 0.1f,             // Distant metallic clang
        SoundEvents.IRON_DOOR_CLOSE to 0.2f,        // Metal door
        SoundEvents.AMBIENT_UNDERWATER_LOOP to 0.1f, // Droning hum
    )
    
    fun register() {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            if (world is ServerLevel) {
                val isBackrooms = world.dimension().location().toString() == "mikkas-mod:backrooms"
                
                if (isBackrooms) {
                    world.players().forEach { player ->
                        handleAmbientSounds(player, world)
                    }
                }
            }
        }
    }
    
    private fun handleAmbientSounds(player: ServerPlayer, world: ServerLevel) {
        val uuid = player.uuid
        val currentCooldown = playerCooldowns.getOrDefault(uuid, 0)
        
        if (currentCooldown > 0) {
            playerCooldowns[uuid] = currentCooldown - 1
            return
        }
        
        val random = world.random
        
        // Constant low fluorescent hum (every 100 ticks / 5 seconds)
        if (world.gameTime % 100L == 0L) {
            // Play a quiet buzzing/humming sound
            player.playNotifySound(SoundEvents.BEACON_AMBIENT, SoundSource.AMBIENT, 0.15f, 2.0f)
        }
        
        // Random ambient sounds (roughly every 10-30 seconds per player)
        if (random.nextFloat() < 0.002f) { // ~0.2% chance per tick
            val (sound, volume) = CREEPY_SOUNDS[random.nextInt(CREEPY_SOUNDS.size)]
            
            // Play from a random offset position to make it feel distant
            val offsetX = (random.nextDouble() - 0.5) * 30
            val offsetY = (random.nextDouble() - 0.5) * 10
            val offsetZ = (random.nextDouble() - 0.5) * 30
            
            world.playSound(
                null,
                player.x + offsetX,
                player.y + offsetY,
                player.z + offsetZ,
                sound,
                SoundSource.AMBIENT,
                volume,
                0.7f + random.nextFloat() * 0.3f // Slight pitch variation
            )
            
            // Set cooldown to prevent sound spam (3-8 seconds)
            playerCooldowns[uuid] = 60 + random.nextInt(100)
        }
        
        // Occasional unsettling whisper (very rare)
        if (random.nextFloat() < 0.0002f) { // ~0.02% chance per tick
            player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.AMBIENT, 0.2f, 0.5f)
            playerCooldowns[uuid] = 200 // 10 second cooldown after whisper
        }
    }
}
