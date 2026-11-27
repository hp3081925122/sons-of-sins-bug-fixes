package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.DamageCancelPuppetProcedure;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 DamageCancelPuppetProcedure 私有五参 execute 中的严格 NBT 背包检查（MCR-24）。
 */
@Mixin(value = DamageCancelPuppetProcedure.class, remap = false)
public abstract class DamageCancelPuppetInventoryMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/Entity;D)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }
}
