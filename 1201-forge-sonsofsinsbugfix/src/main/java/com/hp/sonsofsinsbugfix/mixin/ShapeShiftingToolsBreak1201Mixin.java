package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolsBreakProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 ShapeShiftingToolsBreakProcedure 私有三参 execute 的进度判空（MCR-31）与只读标签防污染（MCR-22）。
 */
@Mixin(value = ShapeShiftingToolsBreakProcedure.class, remap = false)
public abstract class ShapeShiftingToolsBreak1201Mixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/AdvancementProgress;", remap = true)
    )
    private static AdvancementProgress sonsofsinsbugfix$safeGetProgress(PlayerAdvancements advancements, Advancement adv) {
        if (adv == null) {
            return new AdvancementProgress();
        }
        return advancements.getOrStartProgress(adv);
    }

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;", remap = true)
    )
    private static CompoundTag sonsofsinsbugfix$readOnlyTag(ItemStack stack) {
        return stack.hasTag() ? stack.getTag() : SonsOfSinsHelper.EMPTY_READONLY_TAG;
    }
}
