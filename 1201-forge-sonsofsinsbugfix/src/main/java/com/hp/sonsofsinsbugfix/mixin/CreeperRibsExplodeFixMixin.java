package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.CreeperRibsPlayerFinishesUsingItem2Procedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 食用充能苦力怕肋骨爆炸无视 mobGriefing 游戏规则的问题。
 */
@Mixin(value = CreeperRibsPlayerFinishesUsingItem2Procedure.class, remap = false)
public abstract class CreeperRibsExplodeFixMixin {

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;", remap = true)
    )
    private static Explosion sonsofsinsbugfix$respectMobGriefing(
            Level level, Entity source, double x, double y, double z, float radius, Level.ExplosionInteraction interaction) {
        Level.ExplosionInteraction safeInteraction = !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                ? Level.ExplosionInteraction.NONE
                : interaction;
        return level.explode(source, x, y, z, radius, safeInteraction);
    }
}
