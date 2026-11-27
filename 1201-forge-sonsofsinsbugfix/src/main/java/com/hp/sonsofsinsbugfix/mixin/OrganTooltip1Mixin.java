package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.concurrent.atomic.AtomicReference;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation2Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation3Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeSpecialInformationProcedure;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.20.1 血肉躯壳与纷争人偶 4 个器官提示过程的 substring(3, len - 1) 越界崩溃与 “[Empty]” 英文硬编码。
 */
@Mixin(value = FleshCarcassSpecialInformationProcedure.class, remap = false)
public abstract class OrganTooltip1Mixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeSlot3(ItemStack itemstack, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(sonsofsinsbugfix$readSlot(itemstack, 3));
    }

    private static String sonsofsinsbugfix$readSlot(ItemStack container, int slot) {
        if (container == null || container.isEmpty()) {
            return SonsOfSinsHelper.formatOrganTooltip(ItemStack.EMPTY);
        }
        AtomicReference<ItemStack> ref = new AtomicReference<>(ItemStack.EMPTY);
        container.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .ifPresent(cap -> {
                    if (slot < cap.getSlots()) {
                        ref.set(cap.getStackInSlot(slot));
                    }
                });
        return SonsOfSinsHelper.formatOrganTooltip(ref.get());
    }
}
