package com.mikka.mod.entity

import com.mikka.mod.item.ModItems
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class BackroomsBossEntity(entityType: EntityType<out Monster>, level: Level) : Monster(entityType, level) {

    companion object {
        val PHASE: EntityDataAccessor<Int> = SynchedEntityData.defineId(BackroomsBossEntity::class.java, EntityDataSerializers.INT)
        val SHIELDED: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BackroomsBossEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun createBossAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0) // Increased Health
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(PHASE, 0)
        builder.define(SHIELDED, true)
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, MeleeAttackGoal(this, 1.0, true))
        this.goalSelector.addGoal(5, RandomStrollGoal(this, 0.8))
        this.goalSelector.addGoal(6, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        this.goalSelector.addGoal(6, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun getAmbientSound() = SoundEvents.WITHER_AMBIENT
    override fun getHurtSound(damageSource: DamageSource) = SoundEvents.WITHER_HURT
    override fun getDeathSound() = SoundEvents.WITHER_DEATH

    override fun tick() {
        super.tick()
        if (!this.level().isClientSide) {
            // Phase Management
            if (this.health < this.maxHealth / 2 && this.entityData.get(PHASE) == 0) {
                this.entityData.set(PHASE, 1)
                this.getAttribute(Attributes.MOVEMENT_SPEED)?.baseValue = 0.45 // Enrage speed
                this.playSound(SoundEvents.WITHER_SPAWN, 1.0f, 0.5f)
                
                // Summon Minions
                for (i in 0..3) {
                    val minion = ModEntities.BACKROOMS_MONSTER.create(this.level())
                    if (minion != null) {
                        minion.moveTo(this.x + (random.nextDouble() - 0.5) * 5, this.y, this.z + (random.nextDouble() - 0.5) * 5, 0f, 0f)
                        this.level().addFreshEntity(minion)
                    }
                }
            }

            // Shield Regeneration Logic
            if (!this.entityData.get(SHIELDED) && this.tickCount % 300 == 0) { // 15 seconds
                 this.entityData.set(SHIELDED, true)
                 this.playSound(SoundEvents.BEACON_ACTIVATE, 1.0f, 1.0f)
            }

            // Special Power: Smite
            if (this.tickCount % 100 == 0 && this.target != null) {
                val lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(this.level())
                if (lightning != null) {
                    lightning.moveTo(this.target!!.position())
                    this.level().addFreshEntity(lightning)
                }
            }

            // Special Power: Shockwave (New)
            if (this.tickCount % 200 == 0) { // Every 10 seconds
                val players = this.level().getEntitiesOfClass(net.minecraft.server.level.ServerPlayer::class.java, this.boundingBox.inflate(10.0))
                if (players.isNotEmpty()) {
                    val explodeSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("minecraft", "entity.generic.explode"))
                    if (explodeSound != null) {
                        this.playSound(explodeSound, 1.0f, 1.0f)
                    }
                    players.forEach { player ->
                        val dir = player.position().subtract(this.position()).normalize()
                        player.knockback(2.0, -dir.x, -dir.z)
                        player.hurt(this.damageSources().mobAttack(this), 8.0f)
                    }
                }
            }
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (source.directEntity is net.minecraft.world.entity.projectile.Projectile) {
            return false // Projectile Immunity
        }

        if (this.entityData.get(SHIELDED)) {
            if (source.entity is net.minecraft.server.level.ServerPlayer) {
                val player = source.entity as net.minecraft.server.level.ServerPlayer
                // Check for Critical Hit (Falling)
                val isCrit = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater
                
                if (isCrit) {
                    this.entityData.set(SHIELDED, false)
                    this.playSound(SoundEvents.SHIELD_BREAK, 1.0f, 1.0f)
                    return super.hurt(source, amount)
                }
            }
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 1.0f)
            return false
        }

        // Special Power: Teleportation on hit
        if (random.nextFloat() < 0.2f) { // 20% chance
            for(i in 0..10) {
                val dx = (random.nextDouble() - 0.5) * 16.0
                val dy = (random.nextDouble() - 0.5) * 4.0
                val dz = (random.nextDouble() - 0.5) * 16.0
                if (this.randomTeleport(this.x + dx, this.y + dy, this.z + dz, true)) {
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f)
                    break
                }
            }
        }

        return super.hurt(source, amount)
    }

    override fun dropCustomDeathLoot(serverLevel: ServerLevel, damageSource: DamageSource, hitByPlayer: Boolean) {
        super.dropCustomDeathLoot(serverLevel, damageSource, hitByPlayer)
        if (hitByPlayer) {
            this.spawnAtLocation(ItemStack(ModItems.CORRUPTED_SWORD))
            this.spawnAtLocation(ItemStack(ModItems.LIGHTNING_STICK))
            
            // Grant Advancement to all nearby players
            val players = serverLevel.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer::class.java, this.boundingBox.inflate(100.0))
            val advancement = serverLevel.server.advancements.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms/boss_defeated"))
            if (advancement != null) {
                players.forEach { player ->
                    val progress = player.advancements.getOrStartProgress(advancement)
                    if (!progress.isDone) {
                        progress.remainingCriteria.forEach { criterion ->
                            player.advancements.award(advancement, criterion)
                        }
                    }
                }
            }
        }
    }
}
