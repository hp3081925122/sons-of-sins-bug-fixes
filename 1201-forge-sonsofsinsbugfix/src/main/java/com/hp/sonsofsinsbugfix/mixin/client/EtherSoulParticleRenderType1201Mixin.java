package com.hp.sonsofsinsbugfix.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 EtherSoulParticle.PARTICLE_GLOW 在 begin() 中漏绑粒子图集（TextureAtlas.LOCATION_PARTICLES）导致采样错误纹理的问题。
 */
@Mixin(targets = "net.mcreator.sonsofsins.client.particle.EtherSoulParticle$1")
public abstract class EtherSoulParticleRenderType1201Mixin {

    @Inject(method = "begin", at = @At("HEAD"))
    private void sonsofsinsbugfix$bindParticleAtlas(BufferBuilder builder, TextureManager textureManager, CallbackInfo ci) {
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
    }
}
