package com.hp.sonsofsinsbugfix.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-41③（Forge 1.20.1）：
 * 1. 覆写 isSpecial() 返回 true，消除进服刷屏 11 条 Unknown recipe category 警告；
 * 2. 修复 assemble() 直接返回可变单例 output 未 .copy() 的缺陷。
 */
@Mixin(EthericBloodImbibationRecipe.class)
public abstract class EthericBloodImbibationRecipeMixin implements Recipe<SimpleContainer> {
    @Shadow(remap = false)
    @Final
    private ItemStack output;

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Inject(method = "assemble(Lnet/minecraft/world/SimpleContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$copyAssembleOutput(SimpleContainer container, RegistryAccess access, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(this.output.copy());
    }
}
