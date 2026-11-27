package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.BludAggressiveOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.BludOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.EtherGhostOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复全部 7 个抓人处决过程在造成致命伤害后立刻调用 entity.getFirstPassenger().stopRiding() 导致的空指针崩服问题（NeoForge 1.21.1）。
 */
@Mixin(value = {
        BludAggressiveOnEntityTickUpdateProcedure.class,
        BludOnEntityTickUpdateProcedure.class,
        ButcherOnEntityTickUpdateProcedure.class,
        EtherGhostOnEntityTickUpdateProcedure.class,
        SinOnEntityTickUpdateProcedure.class,
        WalkingBedOnEntityTickUpdateProcedure.class,
        WistiverOnEntityTickUpdateProcedure.class
}, remap = false)
public abstract class GrabExecutionNpeMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"
            )
    )
    private static void sonsofsinsbugfix$safeStopRiding(Entity passenger) {
        if (passenger != null) {
            passenger.stopRiding();
        }
    }
}
