package com.hp.sonsofsinsbugfix.mixin.client;

import mezz.jei.api.registration.IRecipeRegistration;
import net.mcreator.sonsofsins.init.SonsOfSinsModJeiPlugin;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 JEI 插件在客户端 level 尚未就绪或断线竞态时 Objects.requireNonNull(level) 抛出 NPE。
 */
@Mixin(value = SonsOfSinsModJeiPlugin.class, remap = false)
public abstract class SonsOfSinsModJeiPluginMixin {
    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardNullLevel(IRecipeRegistration registration, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null) {
            ci.cancel();
        }
    }
}
