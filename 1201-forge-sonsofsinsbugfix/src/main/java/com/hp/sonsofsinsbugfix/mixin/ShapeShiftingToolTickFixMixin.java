package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.concurrent.atomic.AtomicBoolean;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolItemInHandTickProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 ShapeShiftingToolItemInHandTickProcedure 每 tick 循环写入 ~40 次 NBT 导致的同步风暴与挖掘进度不断重置问题：
 * 仅在服务端扫描一次背包得出最终结果，且仅当 haveUrn 数值实际变化时才写入 NBT。
 */
@Mixin(value = ShapeShiftingToolItemInHandTickProcedure.class, remap = false)
public abstract class ShapeShiftingToolTickFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$writeHaveUrnOnlyOnChange(
            LevelAccessor world, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        ci.cancel();
        if (entity == null || world == null || world.isClientSide() || itemstack == null || itemstack.isEmpty()) {
            return;
        }
        boolean hasEther = false;
        if (entity instanceof Player player
                && SonsOfSinsHelper.inventoryHasItem(player.getInventory(), new ItemStack(SonsOfSinsModItems.ETHER_ENGINE.get()))) {
            hasEther = true;
        } else {
            AtomicBoolean foundUrn = new AtomicBoolean(false);
            entity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv -> {
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack slotStack = inv.getStackInSlot(i);
                    if (!slotStack.isEmpty() && slotStack.getItem() == SonsOfSinsModItems.URN.get()) {
                        slotStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(urnInv -> {
                            int urnSlots = Math.min(9, urnInv.getSlots());
                            for (int j = 0; j < urnSlots; j++) {
                                ItemStack ash = urnInv.getStackInSlot(j);
                                if (!ash.isEmpty() && ash.getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                                    foundUrn.set(true);
                                    return;
                                }
                            }
                        });
                        if (foundUrn.get()) {
                            return;
                        }
                    }
                }
            });
            hasEther = foundUrn.get();
        }
        double desired = hasEther ? 1.0 : 0.0;
        CompoundTag existing = itemstack.getTag();
        if (existing == null || !existing.contains("haveUrn") || existing.getDouble("haveUrn") != desired) {
            itemstack.getOrCreateTag().putDouble("haveUrn", desired);
        }
    }
}
