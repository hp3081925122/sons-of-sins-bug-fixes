package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.DamageCancelPuppetProcedure;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 DamageCancelPuppetProcedure 遗漏 CHARGED_CREEPER_RIBS（高压苦力怕肋骨）抵挡爆炸伤害的问题（与 1.21.1 对齐）。
 */
@Mixin(value = DamageCancelPuppetProcedure.class, remap = false)
public abstract class DamageCancelPuppetChargedRibsMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/Entity;D)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", remap = true)
    )
    private static Item sonsofsinsbugfix$treatChargedRibsAsCreeperRibs(ItemStack stack) {
        Item item = stack.getItem();
        if (item == SonsOfSinsModItems.CHARGED_CREEPER_RIBS.get()) {
            return SonsOfSinsModItems.CREEPER_RIBS.get();
        }
        return item;
    }
}
