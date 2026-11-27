package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.FleshPickaxeLivingEntityIsHitWithToolProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 血肉工具自愈在客户端独立掷骰与伤害值可能减为负数的问题。
 */
@Mixin(value = FleshPickaxeLivingEntityIsHitWithToolProcedure.class, remap = false)
public abstract class FleshPickaxeTickFixMixin {

    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$fleshPickaxeServerOnly(
            LevelAccessor world, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        if (world == null || world.isClientSide()) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", remap = true),
            index = 0
    )
    private static int sonsofsinsbugfix$clampMinDamage(int dmg) {
        return Math.max(0, dmg);
    }
}
