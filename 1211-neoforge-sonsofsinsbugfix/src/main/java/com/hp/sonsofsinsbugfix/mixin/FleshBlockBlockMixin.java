package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.block.FleshBlockBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-09① 与碰撞箱越界（NeoForge 1.21.1）：实心血肉块恢复不透光与 [0, 16] 标准碰撞箱。
 */
@Mixin(FleshBlockBlock.class)
public abstract class FleshBlockBlockMixin extends Block {
    protected FleshBlockBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "propagatesSkylightDown", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$solidNoSkylight(BlockState state, BlockGetter reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getLightBlock", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$solidFullLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(15);
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$clampedFullCubeShape(
            BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(Shapes.block());
    }
}
