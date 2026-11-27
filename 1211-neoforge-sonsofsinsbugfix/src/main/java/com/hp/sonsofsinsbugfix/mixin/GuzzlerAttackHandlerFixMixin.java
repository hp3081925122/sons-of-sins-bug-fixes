package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.GuzzlerAttackHandlerProcedure;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 GuzzlerAttackHandlerProcedure 重复叠加主手武器攻击力的问题。
 */
@Mixin(value = GuzzlerAttackHandlerProcedure.class, remap = false)
public abstract class GuzzlerAttackHandlerFixMixin {
    @Redirect(
            method = "hurtEntity",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/GuzzlerAttackHandlerProcedure;getDamageForItem(Lnet/minecraft/world/item/ItemStack;)D")
    )
    private static double sonsofsinsbugfix$avoidDoubleWeaponDamage(ItemStack itemStack) {
        return 0.0;
    }
}
