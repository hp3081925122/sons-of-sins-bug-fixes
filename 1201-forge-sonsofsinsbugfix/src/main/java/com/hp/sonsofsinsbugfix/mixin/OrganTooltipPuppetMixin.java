package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.concurrent.atomic.AtomicReference;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeSpecialInformationProcedure;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.20.1 PuppetOfStrifeSpecialInformationProcedure（槽位 0）的越界崩溃与英文硬编码。
 */
@Mixin(value = PuppetOfStrifeSpecialInformationProcedure.class, remap = false)
public abstract class OrganTooltipPuppetMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeSlot0(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        AtomicReference<ItemStack> ref = new AtomicReference<>(ItemStack.EMPTY);
        if (itemstack != null && !itemstack.isEmpty()) {
            itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                    .ifPresent(cap -> {
                        if (0 < cap.getSlots()) {
                            ref.set(cap.getStackInSlot(0));
                        }
                    });
        }
        cir.setReturnValue(SonsOfSinsHelper.formatOrganTooltip(ref.get()));
    }
}
