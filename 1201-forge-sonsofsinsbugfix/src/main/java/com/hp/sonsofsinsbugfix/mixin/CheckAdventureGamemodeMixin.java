package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-03（Forge 1.20.1）：替换全部 15 个 ADVENTURE 模式匿名类。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.procedures.BludAggressivePlayerCollidesWithThisEntityProcedure$2",
        "net.mcreator.sonsofsins.procedures.BludOnEntityTickUpdateProcedure$10",
        "net.mcreator.sonsofsins.procedures.BludOnEntityTickUpdateProcedure$5",
        "net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure$10",
        "net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure$5",
        "net.mcreator.sonsofsins.procedures.EtherGhostOnEntityTickUpdateProcedure$2",
        "net.mcreator.sonsofsins.procedures.FogBlendSinMonstersProcedure$2",
        "net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure$10",
        "net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure$5",
        "net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure$10",
        "net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure$5",
        "net.mcreator.sonsofsins.procedures.WistiverEntityIsHurtProcedure$1",
        "net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure$4",
        "net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure$6",
        "net.mcreator.sonsofsins.procedures.WistiverPlayerCollidesWithThisEntityProcedure$2"
}, remap = false)
public abstract class CheckAdventureGamemodeMixin {
    @Inject(method = "checkGamemode", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$safeCheckAdventure(Entity _ent, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SafeGameModeHelper.isGameMode(_ent, GameType.ADVENTURE));
    }
}
