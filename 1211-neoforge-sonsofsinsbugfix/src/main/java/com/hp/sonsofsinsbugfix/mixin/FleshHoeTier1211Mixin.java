package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.21.1 血肉锄头（FleshHoeItem）修复材料被误改为原版骨头（Items.BONE）的退化问题，恢复为亡骸血肉（FLESH_OF_DEMISE）。
 */
@Mixin(targets = "net.mcreator.sonsofsins.item.FleshHoeItem$1")
public abstract class FleshHoeTier1211Mixin {

    @Inject(method = "getRepairIngredient()Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$fixRepairIngredient(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.of(new ItemStack(SonsOfSinsModItems.FLESH_OF_DEMISE.get())));
    }
}
