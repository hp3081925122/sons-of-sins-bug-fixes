package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复全部 30 个血肉造物及器官展示渲染层的两处客户端崩溃与渲染异常（Forge 1.20.1）：
 * 1. 字符串 split(":", 2) 在空值或缺少冒号时导致 ArrayIndexOutOfBoundsException；
 * 2. 附属模组器官缺贴图或 1.20.1 != ItemStack.EMPTY 误判空槽位时跳过缺失贴图（回退透明贴图），避免紫黑缺失块。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.client.renderer.DevourerOrgansRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.DevourerOrgansRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.DevourerOrgansRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GrubOrgansRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GrubOrgansRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GrubOrgansRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GulberOrgansRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GulberOrgansRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GulberOrgansRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerOrgansRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerOrgansRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerOrgansRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.NibblerOrgansRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.NibblerOrgansRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.NibblerOrgansRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.DevourerRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.DevourerRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.DevourerRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GrubRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GrubRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GrubRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GulberRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GulberRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GulberRenderer$4",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerRenderer$4",
        "net.mcreator.sonsofsins.client.renderer.NibblerRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.NibblerRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.NibblerRenderer$3"
}, remap = false)
public abstract class OrgansRendererSafeTextureMixin {

    @Unique
    private static final ResourceLocation SONSOFSINSBUGFIX$INVISIBLE =
            new ResourceLocation("sons_of_sins", "textures/entities/invisible.png");

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;split(Ljava/lang/String;I)[Ljava/lang/String;")
    )
    private String[] sonsofsinsbugfix$safeSplitOrganId(String raw, String regex, int limit) {
        if (raw == null || raw.isEmpty()) {
            return new String[]{"sons_of_sins", "missing"};
        }
        String[] parts = raw.split(regex, limit);
        if (parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return new String[]{"sons_of_sins", "missing"};
        }
        return parts;
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
                    remap = true
            )
    )
    private RenderType sonsofsinsbugfix$skipMissingTexture(ResourceLocation texture) {
        if (SonsOfSinsBugFixConfig.CLIENT.skipMissingAddonOrganTextures.get()
                && !SonsOfSinsClientCache.textureExists(texture)) {
            return RenderType.entityCutoutNoCull(SONSOFSINSBUGFIX$INVISIBLE);
        }
        return RenderType.entityCutoutNoCull(texture);
    }
}
