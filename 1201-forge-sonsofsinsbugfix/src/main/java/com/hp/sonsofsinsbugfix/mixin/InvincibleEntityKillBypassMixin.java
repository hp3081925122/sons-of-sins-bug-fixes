package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.BludAggressiveEntity;
import net.mcreator.sonsofsins.entity.BludTradeEntity;
import net.mcreator.sonsofsins.entity.WistiverJawEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复无敌演出实体（BludAggressive、BludTrade、WistiverJaw）覆写 hurt() 恒返回 false 阻断 /kill 与虚空的问题（Forge 1.20.1）。
 */
@Mixin({
        BludAggressiveEntity.class,
        BludTradeEntity.class,
        WistiverJawEntity.class
})
public abstract class InvincibleEntityKillBypassMixin extends Monster {
    protected InvincibleEntityKillBypassMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$allowKillAndVoid(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(super.hurt(source, amount));
        }
    }
}
