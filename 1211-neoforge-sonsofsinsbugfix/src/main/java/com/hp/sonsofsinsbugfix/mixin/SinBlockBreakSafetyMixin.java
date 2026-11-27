package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.BludOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.SinOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.WalkingBedOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.WistiverOnEntityTickUpdateProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复全部 5 个原罪怪破块逻辑（NeoForge 1.21.1）。
 */
@Mixin(value = {
        BludOnEntityTickUpdateProcedure.class,
        ButcherOnEntityTickUpdateProcedure.class,
        SinOnEntityTickUpdateProcedure.class,
        WalkingBedOnEntityTickUpdateProcedure.class,
        WistiverOnEntityTickUpdateProcedure.class
}, remap = false)
public abstract class SinBlockBreakSafetyMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private static boolean sonsofsinsbugfix$safeDestroyBlock(LevelAccessor world, BlockPos pos, boolean dropBlock) {
        return SonsOfSinsHelper.safeSinBreakBlock(world, pos);
    }
}
