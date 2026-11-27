package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 遗骸胸甲 renderToBuffer 绕过传入 VertexConsumer 直接调用全局 bufferSource 导致悬浮重复模型与丢失附魔光泽的问题。
 */
@Mixin(targets = "net.mcreator.sonsofsins.item.RemnantItem$Chestplate$1$1")
public abstract class RemnantChestplateRender1201Mixin extends HumanoidModel<LivingEntity> {

    private RemnantChestplateRender1201Mixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$renderToPassedBufferOnly(
            PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float r, float g, float b, float a, CallbackInfo ci) {
        if (SonsOfSinsBugFixConfig.CLIENT.fixRemnantChestplateRender.get()) {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
            ci.cancel();
        }
    }
}
