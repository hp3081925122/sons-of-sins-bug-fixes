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
 * 修复 MCR-09① 与碰撞箱越界（Forge 1.20.1）：
 * 实心 16x16x16 血肉块不应透光（getLightBlock = 15, propagatesSkylightDown = false），
 * 且碰撞/轮廓体约束在 [0, 16] 标准方块格内，防止 [-3, 19] 侵入邻格引发碰撞与优化模组异常。
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
