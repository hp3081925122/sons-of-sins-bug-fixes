package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeEntitySwingsItemProcedure;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeItemInInventoryTickProcedure;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复单重载过程的严格 NBT 背包检查 Inventory#contains(new ItemStack(...))（修复 MCR-24）。
 */
@Mixin(value = {
        PuppetOfStrifeEntitySwingsItemProcedure.class,
        PuppetOfStrifeItemInInventoryTickProcedure.class
}, remap = false)
public abstract class StrictInventoryContainsMixin {
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }
}
