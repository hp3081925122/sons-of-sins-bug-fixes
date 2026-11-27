package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.util.SonsOfSinsMapColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-07（Forge 1.20.1）：为原罪之子 22 个默认返回 MapColor.NONE 的可见方块补齐对应地图色，
 * 修复 Xaero 大小地图（全取色模式）与原版地图全部穿透跳过方块的问题；保留原模组已显式声明的 MapColor（如 BloodBlock）。
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockMapColorMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getMapColor", at = @At("RETURN"), cancellable = true)
    private void sonsofsinsbugfix$provideMissingMapColor(BlockGetter level, BlockPos pos, CallbackInfoReturnable<MapColor> cir) {
        if (cir.getReturnValue() != MapColor.NONE || !SonsOfSinsBugFixConfig.COMMON.fixMapColors.get()) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(this.getBlock());
        if (id != null && "sons_of_sins".equals(id.getNamespace())) {
            MapColor resolved = SonsOfSinsMapColors.resolve(id.getPath());
            if (resolved != null) {
                cir.setReturnValue(resolved);
            }
        }
    }
}
