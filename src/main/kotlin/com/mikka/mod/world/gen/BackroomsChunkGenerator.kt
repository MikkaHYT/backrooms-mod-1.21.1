package com.mikka.mod.world.gen

import com.mikka.mod.block.ModBlocks
import com.mikka.mod.entity.ModEntities
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class BackroomsChunkGenerator(biomeSource: BiomeSource) : ChunkGenerator(biomeSource) {
    
    companion object {
        val CODEC: MapCodec<BackroomsChunkGenerator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter { it.biomeSource }
            ).apply(instance, ::BackroomsChunkGenerator)
        }
    }

    override fun codec(): MapCodec<out ChunkGenerator> {
        return CODEC
    }

    override fun applyCarvers(
        region: WorldGenRegion,
        seed: Long,
        random: RandomState,
        biomeManager: BiomeManager,
        structureManager: StructureManager,
        chunk: ChunkAccess,
        step: GenerationStep.Carving
    ) {
    }

    override fun buildSurface(
        region: WorldGenRegion,
        structureManager: StructureManager,
        randomState: RandomState,
        chunk: ChunkAccess
    ) {
    }

    override fun spawnOriginalMobs(region: WorldGenRegion) {
        val chunkPos = region.center
        val random = region.getRandom()
        
        val levelHeight = 8
        val minY = region.minBuildHeight
        val maxY = region.maxBuildHeight
        
        val startLevel = Math.floorDiv(minY, levelHeight)
        val endLevel = Math.floorDiv(maxY, levelHeight)

        // Attempt to spawn on multiple floors
        for (levelIndex in startLevel..endLevel) {
            val y = levelIndex * levelHeight + 1 // Just above floor
            
            // Boss Spawning Logic
            // Check if this chunk contains the center of a boss room
            val bossRoomSpacing = 500
            val chunkWorldX = chunkPos.minBlockX
            val chunkWorldZ = chunkPos.minBlockZ
            
            // Find the nearest boss room center
            val bossRoomX = Math.round(chunkWorldX.toFloat() / bossRoomSpacing) * bossRoomSpacing
            val bossRoomZ = Math.round(chunkWorldZ.toFloat() / bossRoomSpacing) * bossRoomSpacing
            
            // If the center of the boss room is within this chunk
            if (bossRoomX >= chunkWorldX && bossRoomX < chunkWorldX + 16 &&
                bossRoomZ >= chunkWorldZ && bossRoomZ < chunkWorldZ + 16) {
                
                // Only spawn boss on specific levels (every 3rd level) to prevent stacking
                // and ensure it matches the large room generation
                if (levelIndex % 3 == 0) {
                    if (y > minY && y < maxY) {
                        val pos = BlockPos(bossRoomX, y, bossRoomZ)
                        // Ensure space
                        if (region.getBlockState(pos).isAir && region.getBlockState(pos.below()).isSolid) {
                            val boss = ModEntities.BACKROOMS_BOSS.create(region.level)
                            if (boss != null) {
                                boss.moveTo(bossRoomX.toDouble() + 0.5, y.toDouble(), bossRoomZ.toDouble() + 0.5, 0f, 0f)
                                region.addFreshEntity(boss)
                            }
                        }
                    }
                }
            }

            if (random.nextFloat() < 0.2f) { // 20% chance per floor per chunk (Increased from 5%)
                val x = chunkPos.minBlockX + random.nextInt(16)
                val z = chunkPos.minBlockZ + random.nextInt(16)
                
                if (y > minY && y < maxY) {
                    val pos = BlockPos(x, y, z)
                    // Check if valid spawn spot (air above solid)
                    if (region.getBlockState(pos).isAir && region.getBlockState(pos.below()).isSolid) {
                        // Don't spawn monsters inside boss room radius
                        if (!isBossRoom(x, z)) {
                            val entity = ModEntities.BACKROOMS_MONSTER.create(region.level)
                            if (entity != null) {
                                entity.moveTo(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5, random.nextFloat() * 360f, 0f)
                                region.addFreshEntity(entity)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getGenDepth(): Int {
        return 384
    }

    override fun getSeaLevel(): Int {
        return -64
    }

    override fun getMinY(): Int {
        return -64
    }

    override fun getBaseHeight(x: Int, z: Int, type: Heightmap.Types, level: LevelHeightAccessor, random: RandomState): Int {
        return 10
    }

    override fun getBaseColumn(x: Int, z: Int, level: LevelHeightAccessor, random: RandomState): NoiseColumn {
        return NoiseColumn(0, arrayOf())
    }

    override fun addDebugScreenInfo(info: MutableList<String>, random: RandomState, pos: BlockPos) {
    }

    override fun fillFromNoise(
        blender: Blender,
        random: RandomState,
        structureManager: StructureManager,
        chunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> {
        val chunkPos = chunk.pos
        val seed = 12345L // Fixed seed for consistency
        
        val floorBlock = Blocks.SMOOTH_SANDSTONE.defaultBlockState()
        val wallBlock = Blocks.CUT_SANDSTONE.defaultBlockState()
        val ceilingBlock = Blocks.SMOOTH_STONE.defaultBlockState()
        val lightBlock = Blocks.OCHRE_FROGLIGHT.defaultBlockState()
        val bedrock = Blocks.BEDROCK.defaultBlockState()

        val pos = BlockPos.MutableBlockPos()
        val minX = chunkPos.minBlockX
        val minZ = chunkPos.minBlockZ
        
        val minY = chunk.minBuildHeight
        val maxY = chunk.maxBuildHeight
        
        val levelHeight = 8 // Floor to Floor
        val roomHeight = 6 // Floor to Ceiling
        
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val worldX = minX + x
                val worldZ = minZ + z
                
                // Bedrock at bottom
                chunk.setBlockState(pos.set(x, minY, z), bedrock, false)
                
                // Calculate number of levels that fit
                val startLevel = Math.floorDiv(minY, levelHeight)
                val endLevel = Math.floorDiv(maxY, levelHeight)

                for (levelIndex in startLevel..endLevel) {
                    val floorY = levelIndex * levelHeight
                    val ceilingY = floorY + roomHeight
                    
                    val isBossColumn = isBossRoom(worldX, worldZ)
                    val bossRoomFrequency = 3
                    val isBossFloor = (levelIndex % bossRoomFrequency == 0)
                    val isTopLevelOfBossRoom = (levelIndex % bossRoomFrequency == bossRoomFrequency - 1)

                    // Floor
                    // Generate floor if it's NOT a boss column OR it IS a boss column AND it's the bottom floor of the atrium
                    if (floorY > minY && floorY < maxY) {
                        if (!isBossColumn || isBossFloor) {
                            chunk.setBlockState(pos.set(x, floorY, z), floorBlock, false)
                        }
                    }
                    
                    // Ceiling
                    // Generate ceiling if it's NOT a boss column OR it IS a boss column AND it's the top level of the atrium
                    if (ceilingY > minY && ceilingY < maxY) {
                        if (!isBossColumn || isTopLevelOfBossRoom) {
                            chunk.setBlockState(pos.set(x, ceilingY, z), ceilingBlock, false)
                        }
                    }
                    
                    // Walls
                    if (isWall(worldX, worldZ, levelIndex, seed)) {
                        for (y in (floorY + 1) until ceilingY) {
                            if (y > minY && y < maxY)
                                chunk.setBlockState(pos.set(x, y, z), wallBlock, false)
                        }
                    }
                    
                    // Occasional Lights
                    if (!isBossColumn && !isWall(worldX, worldZ, levelIndex, seed) && (worldX % 10 == 0) && (worldZ % 10 == 0)) {
                         if (ceilingY > minY && ceilingY < maxY)
                            chunk.setBlockState(pos.set(x, ceilingY, z), lightBlock, false)
                    }
                    
                    // Escape Portal - Extremely Rare
                    val portalHash = (worldX * 3123 + worldZ * 5411 + levelIndex * 1231).hashCode()
                    if (Math.abs(portalHash) % 20000 == 0) { // Rare (1 in 20000)
                         if (floorY + 1 > minY && floorY + 1 < maxY && !isWall(worldX, worldZ, levelIndex, seed) && !isBossColumn) {
                             chunk.setBlockState(pos.set(x, floorY + 1, z), ModBlocks.ESCAPE_PORTAL.defaultBlockState(), false)
                         }
                    }
                    
                    // Fill the space between this ceiling and the next floor with stone/ceiling material
                    for (y in (ceilingY + 1) until (floorY + levelHeight)) {
                        if (y > minY && y < maxY) {
                            if (!isBossColumn || isTopLevelOfBossRoom) {
                                chunk.setBlockState(pos.set(x, y, z), ceilingBlock, false)
                            }
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk)
    }
    
    private fun isBossRoom(x: Int, z: Int): Boolean {
        val bossRoomSpacing = 500
        val bossRoomRadius = 20 // 40x40 room
        
        val bossRoomX = Math.round(x.toFloat() / bossRoomSpacing) * bossRoomSpacing
        val bossRoomZ = Math.round(z.toFloat() / bossRoomSpacing) * bossRoomSpacing
        
        return Math.abs(x - bossRoomX) < bossRoomRadius && Math.abs(z - bossRoomZ) < bossRoomRadius
    }

    private fun isWall(x: Int, z: Int, level: Int, seed: Long): Boolean {
        if (isBossRoom(x, z)) return false

        val cellSize = 4
        
        val cellX = Math.floorDiv(x, cellSize)
        val cellZ = Math.floorDiv(z, cellSize)
        
        val localX = Math.floorMod(x, cellSize)
        val localZ = Math.floorMod(z, cellSize)
        
        val isVerticalBoundary = localX == 0
        val isHorizontalBoundary = localZ == 0
        
        // Open areas (Big Rooms)
        // Use a simple pseudo-noise for "rooms"
        val roomScale = 0.1
        val roomNoise = Math.sin(x * roomScale) + Math.cos(z * roomScale)
        if (roomNoise > 1.2) return false // Open space
        
        if (!isVerticalBoundary && !isHorizontalBoundary) return false
        
        if (isVerticalBoundary && isHorizontalBoundary) return true // Pillar
        
        // Randomized Walls
        if (isVerticalBoundary) {
            val rand = java.util.Random(seed + level * 100000L + cellX * 34123L + cellZ * 52123L)
            return rand.nextFloat() < 0.4 // 40% chance of wall
        }
        
        if (isHorizontalBoundary) {
            val rand = java.util.Random(seed + level * 100000L + cellX * 67890L + cellZ * 12345L)
            return rand.nextFloat() < 0.4
        }
        
        return false
    }
}
