package com.mikka.mod.entity

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
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
import net.minecraft.world.level.Level
import java.util.Optional
import java.util.UUID

class SkinStealerEntity(entityType: EntityType<out Monster>, level: Level) : Monster(entityType, level) {

    companion object {
        // Store the UUID of the player whose skin we're mimicking
        val MIMICKED_PLAYER_UUID: EntityDataAccessor<Optional<UUID>> = SynchedEntityData.defineId(
            SkinStealerEntity::class.java, 
            EntityDataSerializers.OPTIONAL_UUID
        )
        // Store the player name for display
        val MIMICKED_PLAYER_NAME: EntityDataAccessor<String> = SynchedEntityData.defineId(
            SkinStealerEntity::class.java,
            EntityDataSerializers.STRING
        )

        fun createSkinStealerAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 2.0)
        }
    }

    private var hasStolenSkin = false
    private var stalkingTicks = 0
    private val STALKING_DURATION = 200 // 10 seconds of stalking before attacking

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(MIMICKED_PLAYER_UUID, Optional.empty())
        builder.define(MIMICKED_PLAYER_NAME, "")
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, SkinStealerAttackGoal(this))
        this.goalSelector.addGoal(4, RandomStrollGoal(this, 0.6))
        this.goalSelector.addGoal(5, LookAtPlayerGoal(this, Player::class.java, 16.0f))
        this.goalSelector.addGoal(6, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun tick() {
        super.tick()
        
        if (!this.level().isClientSide && this.level() is ServerLevel) {
            val serverLevel = this.level() as ServerLevel
            
            // Find nearest player to mimic if we haven't yet
            if (!hasStolenSkin) {
                val nearestPlayer = serverLevel.getNearestPlayer(this, 32.0)
                if (nearestPlayer != null) {
                    stealSkin(nearestPlayer)
                }
            }
            
            // Stalking behavior - follow at a distance before attacking
            val target = this.target
            if (target is Player && hasStolenSkin) {
                val distance = this.distanceTo(target)
                
                if (distance > 8.0 && distance < 24.0) {
                    // Stalking range - increment timer
                    stalkingTicks++
                    
                    // Occasionally make creepy sounds
                    if (random.nextFloat() < 0.01f) {
                        this.playSound(SoundEvents.ENDERMAN_STARE, 0.3f, 0.5f)
                    }
                }
            }
        }
    }
    
    private fun stealSkin(player: Player) {
        this.entityData.set(MIMICKED_PLAYER_UUID, Optional.of(player.uuid))
        this.entityData.set(MIMICKED_PLAYER_NAME, player.gameProfile.name)
        this.customName = net.minecraft.network.chat.Component.literal(player.gameProfile.name)
        this.isCustomNameVisible = true
        hasStolenSkin = true
        
        // Play a creepy sound when skin is stolen
        this.playSound(SoundEvents.WARDEN_NEARBY_CLOSER, 0.5f, 1.5f)
    }
    
    fun getMimickedPlayerUUID(): UUID? {
        return this.entityData.get(MIMICKED_PLAYER_UUID).orElse(null)
    }
    
    fun getMimickedPlayerName(): String {
        return this.entityData.get(MIMICKED_PLAYER_NAME)
    }
    
    fun isStalking(): Boolean {
        return stalkingTicks < STALKING_DURATION && hasStolenSkin
    }

    override fun getAmbientSound() = null // Silent when stalking
    override fun getHurtSound(damageSource: DamageSource) = SoundEvents.PLAYER_HURT
    override fun getDeathSound() = SoundEvents.PLAYER_DEATH
    
    override fun addAdditionalSaveData(compound: net.minecraft.nbt.CompoundTag) {
        super.addAdditionalSaveData(compound)
        val uuid = getMimickedPlayerUUID()
        if (uuid != null) {
            compound.putUUID("MimickedPlayer", uuid)
            compound.putString("MimickedName", getMimickedPlayerName())
            compound.putBoolean("HasStolenSkin", hasStolenSkin)
            compound.putInt("StalkingTicks", stalkingTicks)
        }
    }
    
    override fun readAdditionalSaveData(compound: net.minecraft.nbt.CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains("MimickedPlayer")) {
            this.entityData.set(MIMICKED_PLAYER_UUID, Optional.of(compound.getUUID("MimickedPlayer")))
            this.entityData.set(MIMICKED_PLAYER_NAME, compound.getString("MimickedName"))
            hasStolenSkin = compound.getBoolean("HasStolenSkin")
            stalkingTicks = compound.getInt("StalkingTicks")
            
            val name = getMimickedPlayerName()
            if (name.isNotEmpty()) {
                this.customName = net.minecraft.network.chat.Component.literal(name)
                this.isCustomNameVisible = true
            }
        }
    }
    
    // Custom attack goal that waits during stalking phase
    class SkinStealerAttackGoal(private val skinStealer: SkinStealerEntity) : MeleeAttackGoal(skinStealer, 1.0, true) {
        override fun canUse(): Boolean {
            // Only attack after stalking phase is complete
            return !skinStealer.isStalking() && super.canUse()
        }
        
        override fun canContinueToUse(): Boolean {
            return !skinStealer.isStalking() && super.canContinueToUse()
        }
    }
}
