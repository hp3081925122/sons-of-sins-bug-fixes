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
 * 让 1.20.1 遗骸胸甲的烘焙模型接入资源重载缓存中心，避免资源重载后持有失效的 ModelPart。
 */
@Mixin(targets = "net.mcreator.sonsofsins.item.RemnantItem$Chestplate$1", remap = false)
public abstract class RemnantChestplateModelCache1201Mixin {

    @Redirect(
            method = "getHumanoidArmorModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/EntityModelSet;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;",
                    remap = true
            )
    )
    private ModelPart sonsofsinsbugfix$cachedBakeChest(EntityModelSet modelSet, ModelLayerLocation layer) {
        if (SonsOfSinsBugFixConfig.CLIENT.cacheBakedLayerModels.get()) {
            return SonsOfSinsClientCache.getOrBakeLayer(modelSet, layer);
        }
        return modelSet.bakeLayer(layer);
    }
}
