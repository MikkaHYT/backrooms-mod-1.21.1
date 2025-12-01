package com.mikka.mod.entity

import com.mikka.mod.MikkasMod
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object ModEntities {
    val BACKROOMS_MONSTER: EntityType<BackroomsMonsterEntity> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms_monster"),
        FabricEntityTypeBuilder.create(MobCategory.MONSTER, ::BackroomsMonsterEntity)
            .dimensions(EntityDimensions.fixed(0.7f, 4.0f))
            .build()
    )

    val BACKROOMS_BOSS: EntityType<BackroomsBossEntity> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms_boss"),
        FabricEntityTypeBuilder.create(MobCategory.MONSTER, ::BackroomsBossEntity)
            .dimensions(EntityDimensions.fixed(1.5f, 6.0f)) // Bigger than normal monster
            .build()
    )

    fun registerEntities() {
        FabricDefaultAttributeRegistry.register(BACKROOMS_MONSTER, BackroomsMonsterEntity.createMonsterAttributes())
        FabricDefaultAttributeRegistry.register(BACKROOMS_BOSS, BackroomsBossEntity.createBossAttributes())
    }
}
