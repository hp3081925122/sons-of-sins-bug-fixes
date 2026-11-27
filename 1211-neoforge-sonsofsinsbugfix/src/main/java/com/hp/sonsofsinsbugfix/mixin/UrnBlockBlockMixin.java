package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.block.UrnBlockBlock;
import net.mcreator.sonsofsins.block.entity.UrnBlockBlockEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.UrnCloneItemProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 UrnBlockBlock 五大高危缺陷（NeoForge 1.21.1）：
 * 1. onRemove 未清空方块实体容器导致的确定性刷物漏洞 + 客户端幽灵掉落；
 * 2. 消除每 10 tick 永久自调度并无条件 setBlock 引发的暴跌至 14 FPS 卡顿（MCR-10）；
 * 3. 恢复 1.21.1 退化的 getCloneItemStack（中键拾取保留骨灰瓮内部灰烬）；
 * 4. setPlacedBy 仅服务端同步状态。
 */
@Mixin(UrnBlockBlock.class)
public abstract class UrnBlockBlockMixin extends Block {
    protected UrnBlockBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getCloneItemStack", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$restoreCloneWithContents(
            LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if (level instanceof Level world) {
            ItemStack cloned = UrnCloneItemProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
            if (cloned != null && !cloned.isEmpty()) {
                cir.setReturnValue(cloned);
            }
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeOnRemove(
            BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() == newState.getBlock()) {
            return;
        }
        ci.cancel();
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof UrnBlockBlockEntity urnBe) {
                ItemStack finalUrn = new ItemStack(SonsOfSinsModItems.URN.get());
                IItemHandler cap = finalUrn.getCapability(Capabilities.ItemHandler.ITEM, null);
                if (cap instanceof IItemHandlerModifiable mod) {
                    for (int i = 0; i < 9; i++) {
                        mod.setStackInSlot(i, urnBe.getItem(i).copy());
                    }
                }
                urnBe.clearContent();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, finalUrn);
                world.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
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
