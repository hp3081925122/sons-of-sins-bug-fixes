package com.hp.sonsofsinsbugfix.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 血池配方匹配未判断 ingredients.isEmpty() 直接调用 get(0) 可能越界崩溃的问题。
 */
@Mixin(targets = "net.mcreator.sonsofsins.procedures.BloodUpdateTickProcedure$2", remap = false)
public abstract class BloodUpdateTickRecipeGuard1201Mixin {

    @Redirect(
            method = "getResult()Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;")
    )
    private Object sonsofsinsbugfix$safeGetFirstIngredient(NonNullList<?> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Ingredient.EMPTY;
        }
        return list.get(index);
    }
}
