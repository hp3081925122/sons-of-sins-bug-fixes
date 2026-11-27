package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.BludTradeOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.BottleOfBloodSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.DoesDevourerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesGrubGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesGuzzlerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesNibblerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesWightGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.FleshToolPropertyValueProviderProcedure;
import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolPropertyValueProvider2Procedure;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolPropertyValueProvider3Procedure;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolPropertyValueProviderProcedure;
import net.mcreator.sonsofsins.procedures.WistiversJawsPropertyValueProviderProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 全部 15 个纯只读路径调用 getOrCreateTag() 为空物品写入空 {} NBT 导致无法堆叠与渲染异常的问题（修复 MCR-22）。
 */
@Mixin(value = {
        BludTradeOnEntityTickUpdateProcedure.class,
        BottleOfBloodSpecialInformationProcedure.class,
        DoesDevourerGUIDisplayProcedure.class,
        DoesGrubGUIDisplayProcedure.class,
        DoesGuzzlerGUIDisplayProcedure.class,
        DoesNibblerGUIDisplayProcedure.class,
        DoesWightGUIDisplayProcedure.class,
        FleshToolPropertyValueProviderProcedure.class,
        NibblerBucketRightclickedOnBlockProcedure.class,
        NibblerLavaBucketRightclickedOnBlockProcedure.class,
        ShapeShiftingToolPropertyValueProvider2Procedure.class,
        ShapeShiftingToolPropertyValueProvider3Procedure.class,
        ShapeShiftingToolPropertyValueProviderProcedure.class,
        WistiversJawsPropertyValueProviderProcedure.class
}, remap = false)
public abstract class ReadOnlyTag1201Mixin {
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;", remap = true)
    )
    private static CompoundTag sonsofsinsbugfix$readOnlyTag(ItemStack stack) {
        return stack.hasTag() ? stack.getTag() : SonsOfSinsHelper.EMPTY_READONLY_TAG;
    }
}
