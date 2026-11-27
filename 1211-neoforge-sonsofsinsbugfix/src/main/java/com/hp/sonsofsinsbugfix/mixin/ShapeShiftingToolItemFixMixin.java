package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.ShapeShiftingToolItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.21.1 ShapeShiftingToolItem#canPerformAction 遗漏 PICKAXE_DIG / SHOVEL_DIG / AXE_DIG 能力声明的问题。
 */
@Mixin(value = ShapeShiftingToolItem.class, remap = false)
public abstract class ShapeShiftingToolItemFixMixin {
    @Inject(method = "canPerformAction", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$allowDigAbilities(ItemStack stack, ItemAbility action, CallbackInfoReturnable<Boolean> cir) {
        if (action == ItemAbilities.PICKAXE_DIG
                || action == ItemAbilities.SHOVEL_DIG
                || action == ItemAbilities.AXE_DIG) {
            cir.setReturnValue(true);
        }
    }
}
