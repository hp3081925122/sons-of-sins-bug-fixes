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
 * 优化 BloodBlock（NeoForge 1.21.1）：空血池跳过全实体扫描并降频至 20 tick，有掉落物时保持原 10 tick 响应。
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
