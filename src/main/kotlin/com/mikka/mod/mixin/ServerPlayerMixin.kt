package com.mikka.mod.mixin

import com.mikka.mod.world.dimension.ModDimensions
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayer::class)
abstract class ServerPlayerMixin : Player(null, null, 0f, null) {

    @Inject(method = ["die"], at = [At("HEAD")])
    fun onDie(damageSource: net.minecraft.world.damagesource.DamageSource, ci: CallbackInfo) {
        if (this.level().dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY) {
            // Set respawn point to Backrooms
            val serverPlayer = this as ServerPlayer
            serverPlayer.setRespawnPosition(ModDimensions.BACKROOMS_DIMENSION_KEY, this.blockPosition(), 0f, true, false)
        }
    }
    
    @Inject(method = ["restoreFrom"], at = [At("TAIL")])
    fun onRestoreFrom(oldPlayer: ServerPlayer, alive: Boolean, ci: CallbackInfo) {
        if (this.level().dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY) {
            // Clear inventory and give starter items
            this.inventory.clearContent()
            this.inventory.add(ItemStack(Items.WOODEN_SWORD))
            this.inventory.add(ItemStack(Items.WOODEN_PICKAXE))
        }
    }
}
