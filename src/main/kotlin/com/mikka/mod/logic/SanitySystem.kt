package com.mikka.mod.logic

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.Level

object SanitySystem {
    fun register() {
        ServerTickEvents.END_WORLD_TICK.register { world ->
            if (world is ServerLevel) {
                val isBackrooms = world.dimension().location().toString() == "mikkas-mod:backrooms"
                val isNightOverworld = world.dimension() == Level.OVERWORLD && world.isNight && world.dayTime % 24000 > 13000

                if (isBackrooms || isNightOverworld) {
                    world.players().forEach { player ->
                        handlePlayerSanity(player, isBackrooms)
                    }
                }
            }
        }
    }

    private fun handlePlayerSanity(player: ServerPlayer, isBackrooms: Boolean) {
        val advancement = player.server.advancements.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms/boss_defeated"))
        if (advancement != null && player.advancements.getOrStartProgress(advancement).isDone) {
            return
        }

        val random = player.random
        
        // Base chance: 1 in 1200 ticks (once a minute approx)
        // Increased in Backrooms
        val chance = if (isBackrooms) 0.002 else 0.0005 

        if (random.nextDouble() < chance) {
            val eventType = random.nextInt(4)
            when (eventType) {
                0 -> { // Auditory Hallucination
                    val sound = when (random.nextInt(3)) {
                        0 -> SoundEvents.SCULK_SHRIEKER_SHRIEK
                        1 -> SoundEvents.PHANTOM_AMBIENT
                        else -> SoundEvents.CHEST_OPEN
                    }
                    player.playNotifySound(sound, SoundSource.AMBIENT, 1.0f, 0.5f)
                }
                1 -> { // Visual Hallucination (Effect)
                    player.addEffect(MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false, false))
                    player.sendSystemMessage(Component.literal("§oIt's getting darker..."))
                }
                2 -> { // Paranoia
                    player.playNotifySound(SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 1.0f, 1.0f)
                    player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0f, 1.0f)
                }
                3 -> { // Chat Hallucination
                    val messages = listOf(
                        "Run.",
                        "Don't look back.",
                        "They are watching.",
                        "I see you.",
                        "It's behind you.",
                        ""
                    )
                    player.sendSystemMessage(Component.literal("§c§o${messages[random.nextInt(messages.size)]}"))
                }
            }
        }
    }
}
