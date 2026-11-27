package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeSpecialInformationProcedure;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.21.1 PuppetOfStrifeSpecialInformationProcedure（槽位 0）的越界崩溃与英文硬编码。
 */
@Mixin(value = PuppetOfStrifeSpecialInformationProcedure.class, remap = false)
public abstract class OrganTooltipPuppetMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeSlot0(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        ItemStack organ = ItemStack.EMPTY;
        if (itemstack != null && !itemstack.isEmpty()) {
            IItemHandler cap = itemstack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (cap != null && 0 < cap.getSlots()) {
                organ = cap.getStackInSlot(0);
            }
        }
        cir.setReturnValue(SonsOfSinsHelper.formatOrganTooltip(organ));
    }
}
