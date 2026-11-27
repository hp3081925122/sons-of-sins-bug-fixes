package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.block.UrnBlockBlock;
import net.mcreator.sonsofsins.block.entity.UrnBlockBlockEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 UrnBlockBlock 四大高危缺陷（Forge 1.20.1）：
 * 1. onRemove 拷贝 9 格灰烬掉落后未调用 be.clearContent() 导致的 100% 确定性刷物漏洞；
 * 2. onRemove 缺少 !world.isClientSide() 守卫导致客户端生成幽灵掉落物；
 * 3. onPlace/tick 每 10 tick 永久自调度并无条件 setBlock 引发的客户端暴跌至 14 FPS 卡顿（MCR-10），改为 UrnBlockBlockEntity#setChanged 事件驱动；
 * 4. setPlacedBy 仅在服务端同步方块状态。
 */
@Mixin(UrnBlockBlock.class)
public abstract class UrnBlockBlockMixin extends Block {
    protected UrnBlockBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeOnRemove(
            BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (state.getBlock() == newState.getBlock()) {
            return;
        }
        ci.cancel();
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof UrnBlockBlockEntity urnBe) {
                ItemStack finalUrn = new ItemStack(SonsOfSinsModItems.URN.get());
                finalUrn.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                    if (handler instanceof IItemHandlerModifiable mod) {
                        for (int i = 0; i < 9; i++) {
                            mod.setStackInSlot(i, urnBe.getItem(i).copy());
                        }
                    }
                });
                // 关键修复：在掉出满载骨灰瓮前清空原方块实体内容，彻底阻断并发开界面刷物漏洞
                urnBe.clearContent();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, finalUrn);
                world.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$noPermanentScheduleOnPlace(
            BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            return;
        }
        ci.cancel();
        super.onPlace(blockstate, world, pos, oldState, moving);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$noPermanentScheduleTick(
            BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            return;
        }
        ci.cancel();
        super.tick(blockstate, world, pos, random);
        if (world.getBlockEntity(pos) instanceof UrnBlockBlockEntity urnBe) {
            urnBe.setChanged();
        }
    }

    @Inject(method = "setPlacedBy", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$serverOnlyPlacedSync(
            Level world, BlockPos pos, BlockState blockstate, LivingEntity entity, ItemStack itemstack, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            return;
        }
        ci.cancel();
        super.setPlacedBy(world, pos, blockstate, entity, itemstack);
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof UrnBlockBlockEntity urnBe) {
            urnBe.setChanged();
        }
    }
}
