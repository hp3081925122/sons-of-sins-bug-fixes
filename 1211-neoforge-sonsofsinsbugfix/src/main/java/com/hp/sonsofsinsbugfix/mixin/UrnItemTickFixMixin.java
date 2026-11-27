package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.UrnItemInInventoryTickProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 UrnItemInInventoryTickProcedure 两大严重漏洞（NeoForge 1.21.1）：
 * 1. 仅在实际从本骨灰坛扣除以太骨灰后才清除 isEmptying 标记；
 * 2. 槽位剩余 count <= 0 时写入 ItemStack.EMPTY，杜绝 -1 负数物品栈。
 */
@Mixin(value = UrnItemInInventoryTickProcedure.class, remap = false)
public abstract class UrnItemTickFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeConsumeEtherAshes(Entity entity, ItemStack itemstack, CallbackInfo ci) {
        ci.cancel();
        if (entity == null || entity.level().isClientSide() || itemstack == null || itemstack.isEmpty()) {
            return;
        }
        if (!entity.getPersistentData().getBoolean("isEmptying")) {
            return;
        }
        IItemHandler handler = itemstack.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (!(handler instanceof IItemHandlerModifiable modifiable)) {
            return;
        }
        int slots = Math.min(9, modifiable.getSlots());
        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = modifiable.getStackInSlot(i);
            if (!slotStack.isEmpty() && slotStack.getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                int remaining = slotStack.getCount() - 2;
                if (remaining <= 0) {
                    modifiable.setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    ItemStack updated = slotStack.copy();
                    updated.setCount(remaining);
                    modifiable.setStackInSlot(i, updated);
                }
                entity.getPersistentData().putBoolean("isEmptying", false);
                return;
            }
        }
    }
}
