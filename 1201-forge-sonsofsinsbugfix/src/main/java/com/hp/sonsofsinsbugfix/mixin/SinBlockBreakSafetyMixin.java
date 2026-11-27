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
 * 修复全部 5 个原罪怪破块逻辑：
 * 禁止破坏基岩/命令方块/传送门框架等不可破坏方块、尊重 mobGriefing 游戏规则、并在目标为容器方块时保留掉落防止吞箱子。
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
                    target = "Lnet/minecraft/world/level/LevelAccessor;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    remap = true
            )
    )
    private static boolean sonsofsinsbugfix$safeDestroyBlock(LevelAccessor world, BlockPos pos, boolean dropBlock) {
        return SonsOfSinsHelper.safeSinBreakBlock(world, pos);
    }
}
