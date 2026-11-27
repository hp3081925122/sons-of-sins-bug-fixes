package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.BetrayedGuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.BetrayedGuzzlerEntityIsHurtProcedure;
import net.mcreator.sonsofsins.procedures.BludTradeOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.DevourerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GrubEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GulberEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerEntityIsHurtProcedure;
import net.mcreator.sonsofsins.procedures.NibblerEntityDiesProcedure;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 防止 9 个过程对全局静态单例 ItemStack.EMPTY 调用 setCount(1) 污染全服所有空槽位与空物品（修复 MCR-23）。
 */
@Mixin(value = {
        BetrayedGuzzlerEntityDiesProcedure.class,
        BetrayedGuzzlerEntityIsHurtProcedure.class,
        BludTradeOnEntityTickUpdateProcedure.class,
        DevourerEntityDiesProcedure.class,
        GrubEntityDiesProcedure.class,
        GulberEntityDiesProcedure.class,
        GuzzlerEntityDiesProcedure.class,
        GuzzlerEntityIsHurtProcedure.class,
        NibblerEntityDiesProcedure.class
}, remap = false)
public abstract class EmptyStackSingletonFixMixin {
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V", remap = true)
    )
    private static void sonsofsinsbugfix$neverMutateEmptySingleton(ItemStack stack, int count) {
        if (stack == null || stack == ItemStack.EMPTY || stack.isEmpty()) {
            return;
        }
        stack.setCount(count);
    }
}
