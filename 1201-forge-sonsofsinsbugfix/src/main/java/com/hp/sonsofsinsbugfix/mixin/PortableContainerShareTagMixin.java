package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.FleshCarcassItem;
import net.mcreator.sonsofsins.item.PuppetOfStrifeItem;
import net.mcreator.sonsofsins.item.UrnItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-06① 与 MCR-22（Forge 1.20.1）：
 * 1. 三个便携容器物品在铁砧重命名/附魔后无 "Inventory" 键时，readShareTag 不再强转 null 触发 NPE 崩端；
 * 2. getShareTag 仅在栈已有 NBT 或内部容器非空时才写入，避免污染干净物品空 NBT。
 */
@Mixin(value = {
        FleshCarcassItem.class,
        PuppetOfStrifeItem.class,
        UrnItem.class
}, remap = false)
public abstract class PortableContainerShareTagMixin {
    @Inject(method = "readShareTag", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeReadShareTag(ItemStack stack, CompoundTag nbt, CallbackInfo ci) {
        ci.cancel();
        if (nbt == null) {
            return;
        }
        stack.setTag(nbt);
        if (nbt.contains("Inventory", Tag.TAG_COMPOUND)) {
            CompoundTag invTag = nbt.getCompound("Inventory");
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                if (cap instanceof ItemStackHandler handler) {
                    handler.deserializeNBT(invTag);
                }
            });
        }
    }

    @Inject(method = "getShareTag", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeGetShareTag(ItemStack stack, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag existing = stack.getTag();
        final boolean[] hasItems = {false};
        final CompoundTag[] invData = {null};
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
            if (cap instanceof ItemStackHandler handler) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) {
                        hasItems[0] = true;
                        break;
                    }
                }
                if (hasItems[0] || (existing != null && existing.contains("Inventory", Tag.TAG_COMPOUND))) {
                    invData[0] = handler.serializeNBT();
                }
            }
        });
        if (!hasItems[0] && existing == null) {
            cir.setReturnValue(null);
            return;
        }
        CompoundTag result = existing != null ? existing.copy() : new CompoundTag();
        if (invData[0] != null) {
            result.put("Inventory", invData[0]);
        }
        cir.setReturnValue(result);
    }
}
