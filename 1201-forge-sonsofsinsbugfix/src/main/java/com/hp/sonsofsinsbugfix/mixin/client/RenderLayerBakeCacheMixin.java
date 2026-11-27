package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 缓存全部 24 个实体渲染层在 render() 中每帧调用的 EntityModelSet#bakeLayer，消除严重的客户端掉帧与内存抖动（MCR-37）。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.client.renderer.BetrayedGuzzlerRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GulberRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.GuzzlerRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$1",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$2",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$3",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$4",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$5",
        "net.mcreator.sonsofsins.client.renderer.EtherGhostRenderer$6",
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
        "net.mcreator.sonsofsins.client.renderer.NibblerOrgansRenderer$3"
}, remap = false)
public abstract class RenderLayerBakeCacheMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/EntityModelSet;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;",
                    remap = true
            )
    )
    private ModelPart sonsofsinsbugfix$cachedBakeLayer(EntityModelSet modelSet, ModelLayerLocation layer) {
        if (SonsOfSinsBugFixConfig.CLIENT.cacheBakedLayerModels.get()) {
            return SonsOfSinsClientCache.getOrBakeLayer(modelSet, layer);
        }
        return modelSet.bakeLayer(layer);
    }
}
