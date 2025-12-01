package com.mikka.mod.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.GameEventTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
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
import net.minecraft.world.level.gameevent.DynamicGameEventListener
import net.minecraft.world.level.gameevent.EntityPositionSource
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.gameevent.GameEventListener
import net.minecraft.world.level.gameevent.PositionSource
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import java.util.EnumSet
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

import java.util.function.BiConsumer

class BackroomsMonsterEntity(entityType: EntityType<out Monster>, level: Level) : Monster(entityType, level) {

    override fun tick() {
        super.tick()
        if (!this.level().isClientSide) {
            val target = this.target
            if (target is Player && this.distanceToSqr(target) < 256.0) { // Within 16 blocks
                // Apply Blindness when chasing/near
                if (!target.hasEffect(MobEffects.BLINDNESS)) {
                    target.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true))
                }
                
                // Heartbeat Sound
                if (this.tickCount % 20 == 0) { // Every second
                    val dist = Math.sqrt(this.distanceToSqr(target))
                    val volume = (1.0 - (dist / 32.0)).coerceAtLeast(0.1)
                    target.playSound(SoundEvents.WARDEN_HEARTBEAT, volume.toFloat(), 1.0f)
                }
            }
        }
    }

    private val dynamicGameEventListener: DynamicGameEventListener<GameEventListener> = DynamicGameEventListener(
        object : GameEventListener {
            override fun getListenerSource(): PositionSource {
                return EntityPositionSource(this@BackroomsMonsterEntity, this@BackroomsMonsterEntity.eyeHeight)
            }

            override fun getListenerRadius(): Int {
                return 32
            }

            override fun handleGameEvent(serverLevel: ServerLevel, holder: Holder<GameEvent>, context: GameEvent.Context, vec3: Vec3): Boolean {
                if (this@BackroomsMonsterEntity.isRemoved || this@BackroomsMonsterEntity.isDeadOrDying) {
                    return false
                }

                val gameEvent = holder.value()

                // React to sounds
                if (gameEvent == GameEvent.BLOCK_PLACE || gameEvent == GameEvent.BLOCK_DESTROY || 
                    gameEvent == GameEvent.STEP || gameEvent == GameEvent.HIT_GROUND || 
                    gameEvent == GameEvent.SPLASH || gameEvent == GameEvent.SWIM ||
                    gameEvent == GameEvent.PROJECTILE_LAND || gameEvent == GameEvent.PRIME_FUSE) {
                    
                    val sourceEntity = context.sourceEntity
                    if (sourceEntity is Player && !sourceEntity.isCreative && !sourceEntity.isSpectator) {
                        this@BackroomsMonsterEntity.target = sourceEntity
                        // Teleport closer if far away to scare
                        if (this@BackroomsMonsterEntity.distanceToSqr(sourceEntity) > 100) {
                             this@BackroomsMonsterEntity.teleportTowards(sourceEntity)
                        }
                        return true
                    }
                }
                return false
            }
        }
    )

    init {
        this.setPathfindingMalus(PathType.WATER, -1.0f)
    }

    override fun updateDynamicGameEventListener(listenerConsumer: BiConsumer<DynamicGameEventListener<*>, ServerLevel>) {
        val level = this.level()
        if (level is ServerLevel) {
            listenerConsumer.accept(this.dynamicGameEventListener, level)
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, TeleportRandomlyGoal(this))
        this.goalSelector.addGoal(2, object : MeleeAttackGoal(this, 1.2, false) {
            override fun stop() {
                super.stop()
                // Reset speed when not attacking
                this@BackroomsMonsterEntity.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.23
            }

            override fun start() {
                super.start()
                // Speed up when attacking
                this@BackroomsMonsterEntity.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.28 // Slightly slower than sprinting player (approx 0.28-0.3)
            }
        })
        this.goalSelector.addGoal(5, RandomStrollGoal(this, 0.8))
        this.goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        this.goalSelector.addGoal(6, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun getAmbientSound() = SoundEvents.ENDERMAN_AMBIENT
    override fun getHurtSound(damageSource: DamageSource) = SoundEvents.ENDERMAN_HURT
    override fun getDeathSound() = SoundEvents.ENDERMAN_DEATH
    
    override fun playStepSound(pos: net.minecraft.core.BlockPos, state: net.minecraft.world.level.block.state.BlockState) {
        this.playSound(SoundEvents.RAVAGER_STEP, 1.0f, 0.5f) // Louder and lower pitch for "heavy" feel
    }

    fun teleportTowards(target: Entity) {
        if (!this.level().isClientSide) {
            val x = target.x + (this.random.nextDouble() - 0.5) * 10.0
            val y = target.y
            val z = target.z + (this.random.nextDouble() - 0.5) * 10.0
            this.teleport(x, y, z)
        }
    }

    private fun teleport(x: Double, y: Double, z: Double): Boolean {
        val oldX = this.x
        val oldY = this.y
        val oldZ = this.z
        val success = this.randomTeleport(x, y, z, true)
        if (success) {
            this.level().playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, this.soundSource, 1.0f, 1.0f)
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f)
        }
        return success
    }

    class TeleportRandomlyGoal(private val mob: BackroomsMonsterEntity) : Goal() {
        init {
            this.flags = EnumSet.of(Flag.MOVE)
        }

        override fun canUse(): Boolean {
            return !mob.level().isClientSide && mob.isAlive && mob.random.nextDouble() < 0.005 // Low chance to random teleport
        }

        override fun start() {
            val target = mob.target
            if (target != null) {
                mob.teleportTowards(target)
            } else {
                // Teleport around randomly
                val x = mob.x + (mob.random.nextDouble() - 0.5) * 16.0
                val y = mob.y
                val z = mob.z + (mob.random.nextDouble() - 0.5) * 16.0
                mob.teleport(x, y, z)
            }
        }
    }

    companion object {
        fun createMonsterAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
        }
    }
}
