package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolItemInHandTickProcedure;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 ShapeShiftingToolItemInHandTickProcedure 每 tick 循环写 ~40 次 DataComponent 导致的同步风暴与挖掘重置问题。
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
            IItemHandler inv = entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
            if (inv != null) {
                outer:
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack slotStack = inv.getStackInSlot(i);
                    if (!slotStack.isEmpty() && slotStack.getItem() == SonsOfSinsModItems.URN.get()) {
                        IItemHandler urnInv = slotStack.getCapability(Capabilities.ItemHandler.ITEM, null);
                        if (urnInv != null) {
                            int urnSlots = Math.min(9, urnInv.getSlots());
                            for (int j = 0; j < urnSlots; j++) {
                                ItemStack ash = urnInv.getStackInSlot(j);
                                if (!ash.isEmpty() && ash.getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                                    hasEther = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        }
        double desired = hasEther ? 1.0 : 0.0;
        CustomData currentData = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!currentData.contains("haveUrn") || currentData.copyTag().getDouble("haveUrn") != desired) {
            CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> tag.putDouble("haveUrn", desired));
        }
    }
}
