package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-03（Forge 1.20.1）：替换全部 9 个 CREATIVE 模式匿名类，移除服务端公共类对 Minecraft.getInstance() 的直接引用并防客户端断线 NPE。
 */
@Mixin(targets = {
        "net.mcreator.sonsofsins.procedures.AllureOnEffectActiveTickProcedure$1",
        "net.mcreator.sonsofsins.procedures.DamagesHurtProcedure$1",
        "net.mcreator.sonsofsins.procedures.InsanityOnEffectActiveTickProcedure$2",
        "net.mcreator.sonsofsins.procedures.InsanityThrillProcedure$2",
        "net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure$1",
        "net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure$2",
        "net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure$1",
        "net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure$2",
        "net.mcreator.sonsofsins.procedures.UrnRightclickedOnBlockProcedure$4"
}, remap = false)
public abstract class CheckCreativeGamemodeMixin {
    @Inject(method = "checkGamemode", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$safeCheckCreative(Entity _ent, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SafeGameModeHelper.isGameMode(_ent, GameType.CREATIVE));
    }
}
