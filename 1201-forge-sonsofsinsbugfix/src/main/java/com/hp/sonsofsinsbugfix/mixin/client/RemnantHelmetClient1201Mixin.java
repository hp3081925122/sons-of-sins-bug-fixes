package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import java.util.Collections;
import java.util.Map;
import net.mcreator.sonsofsins.client.model.Modelsoul_steel_helmet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 缓存 1.20.1 遗骸头盔（RemnantItem.Helmet）的 3D 盔甲模型，消除每帧 bakeLayer() 造成的掉帧与对象分配（MCR-37）。
 */
@Mixin(targets = "net.mcreator.sonsofsins.item.RemnantItem$Helmet$1", remap = false)
public abstract class RemnantHelmetClient1201Mixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getHumanoidArmorModel", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$cachedHelmetModel(
            LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel,
            CallbackInfoReturnable<HumanoidModel> cir) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheBakedLayerModels.get()) {
            return;
        }
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        HumanoidModel<?> armorModel = SonsOfSinsClientCache.getCachedRemnantHelmet(modelSet);
        if (armorModel == null) {
            ModelPart baked = SonsOfSinsClientCache.getOrBakeLayer(modelSet, Modelsoul_steel_helmet.LAYER_LOCATION);
            Modelsoul_steel_helmet helmet = new Modelsoul_steel_helmet(baked);
            ModelPart empty = new ModelPart(Collections.emptyList(), Collections.emptyMap());
            armorModel = new HumanoidModel(new ModelPart(
                    Collections.emptyList(),
                    Map.of(
                            "head", helmet.head,
                            "hat", empty,
                            "body", empty,
                            "right_arm", empty,
                            "left_arm", empty,
                            "right_leg", empty,
                            "left_leg", empty
                    )
            ));
            SonsOfSinsClientCache.setCachedRemnantHelmet(modelSet, armorModel);
        }
        armorModel.crouching = living.isShiftKeyDown();
        armorModel.riding = defaultModel.riding;
        armorModel.young = living.isBaby();
        cir.setReturnValue(armorModel);
    }
}
