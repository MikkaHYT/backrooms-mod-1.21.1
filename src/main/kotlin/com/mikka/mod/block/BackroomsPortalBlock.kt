package com.mikka.mod.block

import com.mikka.mod.world.dimension.ModDimensions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

import net.minecraft.world.level.portal.DimensionTransition

class BackroomsPortalBlock(properties: Properties) : Block(properties) {
    companion object {
        val AXIS: EnumProperty<Direction.Axis> = BlockStateProperties.HORIZONTAL_AXIS
        val X_AABB: VoxelShape = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0)
        val Z_AABB: VoxelShape = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0)

        fun trySpawnPortal(level: net.minecraft.world.level.LevelAccessor, pos: BlockPos) {
             // 1. Determine Axis
             // Check if we have Quartz below
             if (!level.getBlockState(pos.below()).`is`(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK)) return
             
             // Try X Axis (Frame is in X-Y plane)
             if (isPortalFrame(level, pos, Direction.Axis.X)) {
                 fillPortal(level, pos, Direction.Axis.X)
                 return
             }
             
             // Try Z Axis
             if (isPortalFrame(level, pos, Direction.Axis.Z)) {
                 fillPortal(level, pos, Direction.Axis.Z)
                 return
             }
        }
        
        private fun isEmpty(level: net.minecraft.world.level.LevelAccessor, pos: BlockPos, startPos: BlockPos): Boolean {
            val state = level.getBlockState(pos)
            return state.isAir || pos == startPos || state.`is`(net.minecraft.world.level.block.Blocks.FIRE)
        }

        private fun isPortalFrame(level: net.minecraft.world.level.LevelAccessor, startPos: BlockPos, axis: Direction.Axis): Boolean {
            val right = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH
            val left = right.opposite
            
            // Find width
            var minSide = 0
            var maxSide = 0
            
            // Scan Left
            var p = startPos
            while (isEmpty(level, p, startPos) && level.getBlockState(p.below()).`is`(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK) && Math.abs(p.x - startPos.x) < 21 && Math.abs(p.z - startPos.z) < 21) {
                minSide++
                p = p.relative(left)
            }
            if (!level.getBlockState(p).`is`(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK)) return false
            
            // Scan Right
            p = startPos
            while (isEmpty(level, p, startPos) && level.getBlockState(p.below()).`is`(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK) && Math.abs(p.x - startPos.x) < 21 && Math.abs(p.z - startPos.z) < 21) {
                maxSide++
                p = p.relative(right)
            }
            if (!level.getBlockState(p).`is`(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK)) return false
            
            return true 
        }

        private fun fillPortal(level: net.minecraft.world.level.LevelAccessor, startPos: BlockPos, axis: Direction.Axis) {
            val right = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH
            val left = right.opposite
            
            // Find bounds again (inefficient but safe)
            var p = startPos
            while (isEmpty(level, p.relative(left), startPos)) {
                p = p.relative(left)
            }
            val leftBound = p
            
            p = startPos
            while (isEmpty(level, p.relative(right), startPos)) {
                p = p.relative(right)
            }
            val rightBound = p
            
            // Fill upwards
            // Iterate from leftBound to rightBound
            // Iterate up until hit non-air
            
            // We need to iterate the range
            val count = if (axis == Direction.Axis.X) (rightBound.x - leftBound.x) else (rightBound.z - leftBound.z)
            
            for (i in 0..Math.abs(count)) {
                var currentPos = leftBound.relative(right, i)
                var y = 0
                while (isEmpty(level, currentPos.above(y), startPos) && y < 21) {
                    level.setBlock(currentPos.above(y), ModBlocks.BACKROOMS_PORTAL.defaultBlockState().setValue(AXIS, axis), 3)
                    y++
                }
            }
        }
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X))
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return if (state.getValue(AXIS) == Direction.Axis.Z) Z_AABB else X_AABB
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AXIS)
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity.canUsePortal(false)) {
            entity.setPortalCooldown()
            if (!level.isClientSide && level is ServerLevel) {
                val resourceKey = if (level.dimension() == ModDimensions.BACKROOMS_DIMENSION_KEY) {
                    Level.OVERWORLD
                } else {
                    ModDimensions.BACKROOMS_DIMENSION_KEY
                }
                
                val serverLevel = level.server.getLevel(resourceKey)
                if (serverLevel != null) {
                    var targetPos = entity.blockPosition()
                    if (resourceKey == ModDimensions.BACKROOMS_DIMENSION_KEY) {
                        // Find safe spot in Backrooms
                        // Randomize X/Z to support multiplayer spawning
                        val random = net.minecraft.util.RandomSource.create()
                        val randomX = (random.nextInt(4000) - 2000) + targetPos.x
                        val randomZ = (random.nextInt(4000) - 2000) + targetPos.z
                        
                        // Snap to cell center (assuming 4x4 grid)
                        val cellSize = 4
                        val cellX = Math.floorDiv(randomX, cellSize)
                        val cellZ = Math.floorDiv(randomZ, cellSize)
                        
                        // Center of cell
                        val centerX = cellX * cellSize + cellSize / 2
                        val centerZ = cellZ * cellSize + cellSize / 2
                        
                        // Pick a safe Y level. 
                        // Levels are at levelIndex * 8.
                        // Let's pick level 4 (Y=32). Floor is at 32. Player at 33.
                        val targetY = 33
                        
                        targetPos = BlockPos(centerX, targetY, centerZ)
                    } else {
                        // Going back to Overworld - find surface?
                        // For now, just keep coordinates but ensure Y is safe
                        targetPos = serverLevel.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, targetPos)
                    }

                    val transition = DimensionTransition(
                        serverLevel,
                        Vec3.atBottomCenterOf(targetPos),
                        entity.deltaMovement,
                        entity.yRot,
                        entity.xRot,
                        DimensionTransition.DO_NOTHING
                    )
                    entity.changeDimension(transition)
                }
            }
        }
    }
}
