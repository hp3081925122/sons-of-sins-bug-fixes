package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.block.BloodBlock;
import net.mcreator.sonsofsins.procedures.BloodUpdateTickProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化 BloodBlock（Forge 1.20.1）：
 * 仅在血池上方存在掉落物时才调用昂贵的 BloodUpdateTickProcedure（含 3 次全实体扫描与配方遍历），
 * 空血池拉长复检间隔至 20 tick，大幅降低血池区块的空转开销。
 */
@Mixin(BloodBlock.class)
public abstract class BloodBlockMixin extends LiquidBlock {
    protected BloodBlockMixin(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$throttledBloodTick(
            BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            return;
        }
        ci.cancel();
        super.tick(blockstate, world, pos, random);
        AABB box = new AABB(pos).inflate(1.5);
        boolean hasItem = !world.getEntitiesOfClass(ItemEntity.class, box).isEmpty();
        if (hasItem) {
            BloodUpdateTickProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
            world.scheduleTick(pos, this, 10);
        } else {
            world.scheduleTick(pos, this, 20);
        }
    }
}
