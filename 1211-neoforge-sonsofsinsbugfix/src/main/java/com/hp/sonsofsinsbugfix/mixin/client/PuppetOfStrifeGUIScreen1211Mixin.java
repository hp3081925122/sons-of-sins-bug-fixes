package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import net.mcreator.sonsofsins.client.gui.PuppetOfStrifeGUIScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 缓存 1.21.1 纷争人偶界面的叠加层贴图 ResourceLocation，避免每帧重复 parse 字符串。
 */
@Mixin(PuppetOfStrifeGUIScreen.class)
public abstract class PuppetOfStrifeGUIScreen1211Mixin {

    @Unique
    private static final ResourceLocation SONSOFSINSBUGFIX$OVERLAY_TEX =
            ResourceLocation.fromNamespaceAndPath("sons_of_sins", "textures/screens/puppet_of_strife_gui_overlay.png");

    @Redirect(
            method = "renderBg",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;")
    )
    private ResourceLocation sonsofsinsbugfix$cachedOverlayTexture(String path) {
        if ("sons_of_sins:textures/screens/puppet_of_strife_gui_overlay.png".equals(path)) {
            return SONSOFSINSBUGFIX$OVERLAY_TEX;
        }
        return SonsOfSinsClientCache.cachedLocation(path);
    }
}
