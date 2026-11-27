package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-41③（NeoForge 1.21.1）：覆写 isSpecial() = true 并修复 assemble() 返回单例未 .copy()。
 */
@Mixin(EthericBloodImbibationRecipe.class)
public abstract class EthericBloodImbibationRecipeMixin implements Recipe<RecipeInput> {
    @Shadow(remap = false)
    @Final
    private ItemStack output;

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$copyAssembleOutput(RecipeInput input, HolderLookup.Provider lookup, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(this.output.copy());
    }
}
