package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.WistiversJawsLivingEntityIsHitWithItemProcedure;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 威斯蒂弗之颚回复饥饿度与饱和度越界（超过 20 或小于 0）的问题。
 */
@Mixin(value = WistiversJawsLivingEntityIsHitWithItemProcedure.class, remap = false)
public abstract class WistiversJawsFoodClampMixin {

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;setFoodLevel(I)V")
    )
    private static void sonsofsinsbugfix$clampFoodLevel(FoodData food, int level) {
        food.setFoodLevel(Math.max(0, Math.min(20, level)));
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;setSaturation(F)V")
    )
    private static void sonsofsinsbugfix$clampSaturation(FoodData food, float saturation) {
        float maxSat = Math.max(0.0F, Math.min(20.0F, (float) food.getFoodLevel() + 2.0F));
        food.setSaturation(Math.max(0.0F, Math.min(maxSat, saturation)));
    }
}
