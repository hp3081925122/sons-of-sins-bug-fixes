package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolChangeProcedure;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 ShapeShiftingToolChangeProcedure 私有四参 execute 中的严格背包检查（NeoForge 1.21.1，MCR-24）。
 */
@Mixin(value = ShapeShiftingToolChangeProcedure.class, remap = false)
public abstract class ShapeShiftingToolChangeInventoryMixin {
    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }
}
