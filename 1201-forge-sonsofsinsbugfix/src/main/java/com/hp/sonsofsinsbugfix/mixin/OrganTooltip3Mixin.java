package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.concurrent.atomic.AtomicReference;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation3Procedure;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.20.1 FleshCarcassSpecialInformation3Procedure（槽位 5）的越界崩溃与英文硬编码。
 */
@Mixin(value = FleshCarcassSpecialInformation3Procedure.class, remap = false)
public abstract class OrganTooltip3Mixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeSlot5(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        AtomicReference<ItemStack> ref = new AtomicReference<>(ItemStack.EMPTY);
        if (itemstack != null && !itemstack.isEmpty()) {
            itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                    .ifPresent(cap -> {
                        if (5 < cap.getSlots()) {
                            ref.set(cap.getStackInSlot(5));
                        }
                    });
        }
        cir.setReturnValue(SonsOfSinsHelper.formatOrganTooltip(ref.get()));
    }
}
