package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-03（NeoForge 1.21.1）：替换全部 5 个 CREATIVE 模式匿名类。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.procedures.AllureOnEffectActiveTickProcedure$1",
        "net.mcreator.sonsofsins.procedures.DamagesHurtProcedure$4",
        "net.mcreator.sonsofsins.procedures.InsanityOnEffectActiveTickProcedure$2",
        "net.mcreator.sonsofsins.procedures.InsanityThrillProcedure$2",
        "net.mcreator.sonsofsins.procedures.UrnRightclickedOnBlockProcedure$4"
}, remap = false)
public abstract class CheckCreativeGamemodeMixin {
    @Inject(method = "checkGamemode", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$safeCheckCreative(Entity _ent, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SafeGameModeHelper.isGameMode(_ent, GameType.CREATIVE));
    }
}
