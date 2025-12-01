package com.mikka.mod.block

import com.mikka.mod.block.entity.FlickeringLightBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty

class FlickeringLightBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val LIT: BooleanProperty = BooleanProperty.create("lit")
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(LIT, true))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return FlickeringLightBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun <T : BlockEntity?> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return createTickerHelper(type, ModBlocks.FLICKERING_LIGHT_ENTITY, FlickeringLightBlockEntity::tick)
    }
}
