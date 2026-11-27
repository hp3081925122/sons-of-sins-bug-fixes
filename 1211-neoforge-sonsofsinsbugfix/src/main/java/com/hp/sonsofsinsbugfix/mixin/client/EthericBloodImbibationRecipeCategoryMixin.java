package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipeCategory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-43（NeoForge 1.21.1）：JEI 配方分类标题可翻译化。
 */
@Mixin(value = EthericBloodImbibationRecipeCategory.class, remap = false)
public abstract class EthericBloodImbibationRecipeCategoryMixin {
    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$localizedTitle(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("jei.sons_of_sins.etheric_imbibation"));
    }
}
