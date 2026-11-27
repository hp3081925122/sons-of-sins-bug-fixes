package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.init.SonsOfSinsModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-02 客户端并发：将 ItemBlockRenderTypes.setRenderLayer 放入 FMLClientSetupEvent.enqueueWork 串行执行。
 */
@Mixin(value = SonsOfSinsModFluids.FluidsClientSideHandler.class, remap = false)
public abstract class SonsOfSinsModFluidsClientMixin {
    @Inject(method = "clientSetup", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$enqueueFluidRenderLayer(FMLClientSetupEvent event, CallbackInfo ci) {
        ci.cancel();
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(SonsOfSinsModFluids.BLOOD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SonsOfSinsModFluids.FLOWING_BLOOD.get(), RenderType.translucent());
        });
    }
}
