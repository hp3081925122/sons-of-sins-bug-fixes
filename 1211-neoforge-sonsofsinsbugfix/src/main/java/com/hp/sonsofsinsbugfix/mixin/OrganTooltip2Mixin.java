package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation2Procedure;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.21.1 FleshCarcassSpecialInformation2Procedure（槽位 4）的越界崩溃与英文硬编码。
 */
@Mixin(value = FleshCarcassSpecialInformation2Procedure.class, remap = false)
public abstract class OrganTooltip2Mixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeSlot4(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        ItemStack organ = ItemStack.EMPTY;
        if (itemstack != null && !itemstack.isEmpty()) {
            IItemHandler cap = itemstack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (cap != null && 4 < cap.getSlots()) {
                organ = cap.getStackInSlot(4);
            }
        }
        cir.setReturnValue(SonsOfSinsHelper.formatOrganTooltip(organ));
    }
}
