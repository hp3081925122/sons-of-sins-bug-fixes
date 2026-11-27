package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.BludTradeOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.CreeperRibsPlayerFinishesUsingItem2Procedure;
import net.mcreator.sonsofsins.procedures.CreeperRibsPlayerFinishesUsingItemProcedure;
import net.mcreator.sonsofsins.procedures.CrystallizedEtherItemIsCraftedsmeltedProcedure;
import net.mcreator.sonsofsins.procedures.EtherSwordLivingEntityIsHitWithToolProcedure;
import net.mcreator.sonsofsins.procedures.SoulSteelItemIsCraftedsmeltedProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 剩余全部 7 个进度发放过程的 _adv 未判空 NPE 隐患（修复 MCR-31）。
 */
@Mixin(value = {
        BludTradeOnEntityTickUpdateProcedure.class,
        CreeperRibsPlayerFinishesUsingItem2Procedure.class,
        CreeperRibsPlayerFinishesUsingItemProcedure.class,
        CrystallizedEtherItemIsCraftedsmeltedProcedure.class,
        EtherSwordLivingEntityIsHitWithToolProcedure.class,
        SoulSteelItemIsCraftedsmeltedProcedure.class
}, remap = false)
public abstract class AdvancementNullGuard1201Mixin {
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/AdvancementProgress;", remap = true)
    )
    private static AdvancementProgress sonsofsinsbugfix$safeGetProgress(PlayerAdvancements advancements, Advancement adv) {
        if (adv == null) {
            return new AdvancementProgress();
        }
        return advancements.getOrStartProgress(adv);
    }
}
