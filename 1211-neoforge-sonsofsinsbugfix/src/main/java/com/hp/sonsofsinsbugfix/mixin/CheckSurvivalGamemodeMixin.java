package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-03（NeoForge 1.21.1）：替换全部 14 个 SURVIVAL 模式匿名类。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.procedures.BludAggressivePlayerCollidesWithThisEntityProcedure$1",
        "net.mcreator.sonsofsins.procedures.BludOnEntityTickUpdateProcedure$3",
        "net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure$3",
        "net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure$9",
        "net.mcreator.sonsofsins.procedures.EtherGhostOnEntityTickUpdateProcedure$1",
        "net.mcreator.sonsofsins.procedures.FogBlendSinMonstersProcedure$1",
        "net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure$3",
        "net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure$9",
        "net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure$3",
        "net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure$9",
        "net.mcreator.sonsofsins.procedures.WistiverEntityIsHurtProcedure$2",
        "net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure$3",
        "net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure$5",
        "net.mcreator.sonsofsins.procedures.WistiverPlayerCollidesWithThisEntityProcedure$1"
}, remap = false)
public abstract class CheckSurvivalGamemodeMixin {
    @Inject(method = "checkGamemode", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$safeCheckSurvival(Entity _ent, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SafeGameModeHelper.isGameMode(_ent, GameType.SURVIVAL));
    }
}
