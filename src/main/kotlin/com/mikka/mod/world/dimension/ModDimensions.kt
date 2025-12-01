package com.mikka.mod.world.dimension

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType

object ModDimensions {
    val BACKROOMS_DIMENSION_KEY: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms"))
    val BACKROOMS_DIMENSION_TYPE_KEY: ResourceKey<DimensionType> = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath("mikkas-mod", "backrooms_type"))
}
