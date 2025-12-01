package com.mikka.mod.entity

import net.minecraft.core.BlockPos
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.Vec3
import java.util.EnumSet

class SmilerEntity(entityType: EntityType<out Monster>, level: Level) : Monster(entityType, level) {

    companion object {
        val AGGRO: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(SmilerEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun createSmilerAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
        }

        fun checkSmilerSpawnRules(type: EntityType<SmilerEntity>, level: ServerLevelAccessor, spawnType: MobSpawnType, pos: BlockPos, random: RandomSource): Boolean {
            if (level is ServerLevel) {
                val isBackrooms = level.dimension().location().toString() == "mikkas-mod:backrooms"
                if (isBackrooms) return true // Always spawn in Backrooms

                // Overworld logic
                if (level.dimension() == Level.OVERWORLD) {
                    // Check if boss defeated
                    // We can't easily check advancement for "any player" here without a specific player context.
                    // But we can check if it's night.
                    // For the "boss defeated" check, strictly speaking, we need a player.
                    // However, standard spawning happens around players.
                    // We can check the nearest player's advancement?
                    val player = level.getNearestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 128.0, false)
                    if (player is net.minecraft.server.level.ServerPlayer) {
                         val advancement = player.server.advancements.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms/boss_defeated"))
                         if (advancement != null && player.advancements.getOrStartProgress(advancement).isDone) {
                             return false // Boss defeated, don't spawn
                         }
                    }
                    return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                }
            }
            return false
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(AGGRO, false)
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, SmilerAttackGoal(this))
        this.goalSelector.addGoal(5, RandomStrollGoal(this, 0.8))
        this.goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        this.goalSelector.addGoal(6, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun tick() {
        super.tick()
        if (!this.level().isClientSide) {
            val target = this.target
            if (target != null) {
                // Check if target is looking at me
                val vec3 = target.getViewVector(1.0f).normalize()
                var vec32 = Vec3(this.x - target.x, this.eyeY - target.eyeY, this.z - target.z)
                val d0 = vec32.length()
                vec32 = vec32.normalize()
                val d1 = vec3.dot(vec32)
                
                if (d1 > 0.1 && target.hasLineOfSight(this)) { // Looking roughly at smiler
                    this.entityData.set(AGGRO, true)
                } else {
                    // If aggro, stay aggro for a bit? Or stop?
                    // Requirement: "rush the player if stared at"
                    // If not stared at, maybe stop rushing?
                    // Let's keep it simple: Once aggro, stay aggro until target lost or dead.
                }
            } else {
                this.entityData.set(AGGRO, false)
            }
        }
    }

    class SmilerAttackGoal(private val mob: SmilerEntity) : MeleeAttackGoal(mob, 1.0, false) {
        override fun canUse(): Boolean {
            return super.canUse() && mob.entityData.get(AGGRO)
        }

        override fun start() {
            super.start()
            mob.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.45 // Rush speed
        }

        override fun stop() {
            super.stop()
            mob.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.25 // Reset speed
        }
    }
    
    override fun getAmbientSound() = SoundEvents.ENDERMAN_AMBIENT // Placeholder
    override fun getHurtSound(damageSource: DamageSource) = SoundEvents.ENDERMAN_HURT
    override fun getDeathSound() = SoundEvents.ENDERMAN_DEATH
}
