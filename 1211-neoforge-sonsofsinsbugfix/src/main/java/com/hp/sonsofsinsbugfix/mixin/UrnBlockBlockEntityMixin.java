package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.block.UrnBlockBlock;
import net.mcreator.sonsofsins.block.entity.UrnBlockBlockEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 UrnBlockBlockEntity（NeoForge 1.21.1）：预分配槽位数组、事件驱动同步 BLOCKSTATE 与本地化/自定义名保留。
 */
@Mixin(UrnBlockBlockEntity.class)
public abstract class UrnBlockBlockEntityMixin extends RandomizableContainerBlockEntity {
    @Unique
    private static final int[] SONSOFSINSBUGFIX_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    @Shadow(remap = false)
    private NonNullList<ItemStack> stacks;

    protected UrnBlockBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$cachedSlots(Direction side, CallbackInfoReturnable<int[]> cir) {
        cir.setReturnValue(SONSOFSINSBUGFIX_SLOTS);
    }

    @Inject(method = "getDefaultName", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$localizedDefaultName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("container.sons_of_sins.urn"));
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$preserveCustomName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(super.getDisplayName());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()
                || this.level == null
                || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.level.getBlockState(this.worldPosition);
        if (!(state.getBlock() instanceof UrnBlockBlock) || !state.hasProperty(UrnBlockBlock.BLOCKSTATE)) {
            return;
        }
        int targetState = 0;
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty() && stack.is(SonsOfSinsModItems.ETHER_ASHES.get())) {
                targetState = 1;
                break;
            }
        }
        if (state.getValue(UrnBlockBlock.BLOCKSTATE) != targetState) {
            this.level.setBlock(this.worldPosition, state.setValue(UrnBlockBlock.BLOCKSTATE, targetState), 3);
        }
    }
}
