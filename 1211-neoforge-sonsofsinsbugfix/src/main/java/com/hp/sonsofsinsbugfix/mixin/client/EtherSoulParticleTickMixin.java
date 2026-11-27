package com.hp.sonsofsinsbugfix.mixin.client;

import net.mcreator.sonsofsins.client.particle.EtherSoulParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 EtherSoulParticle#tick 中 roll 在 -6.0F 处反复加减抖动卡死的旋转逻辑，改为平滑连续旋转（NeoForge 1.21.1）。
 */
@Mixin(EtherSoulParticle.class)
public abstract class EtherSoulParticleTickMixin extends TextureSheetParticle {

    @Shadow(remap = false)
    @Final
    private SpriteSet spriteSet;

    @Shadow(remap = false)
    private float angularVelocity;

    @Shadow(remap = false)
    private float angularAcceleration;

    protected EtherSoulParticleTickMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$smoothRotation(CallbackInfo ci) {
        super.tick();
        if (!this.removed) {
            this.setSprite(this.spriteSet.get(this.age / 3 % 16 + 1, 16));
        }
        this.oRoll = this.roll;
        this.roll -= this.angularVelocity;
        this.angularVelocity -= this.angularAcceleration;
        ci.cancel();
    }
}
