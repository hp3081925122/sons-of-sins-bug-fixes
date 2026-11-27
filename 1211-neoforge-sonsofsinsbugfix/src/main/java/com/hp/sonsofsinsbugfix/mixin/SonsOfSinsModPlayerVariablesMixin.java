package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-04④（NeoForge 1.21.1）：PlayerVariables.deserializeNBT 空 NBT 防护。
 */
@Mixin(value = SonsOfSinsModVariables.PlayerVariables.class, remap = false)
public abstract class SonsOfSinsModPlayerVariablesMixin {
    @Shadow
    public ItemStack currentOpenItem;

    @Inject(method = "deserializeNBT", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardDeserializeNbt(HolderLookup.Provider lookupProvider, CompoundTag nbt, CallbackInfo ci) {
        if (nbt == null) {
            this.currentOpenItem = ItemStack.EMPTY;
            ci.cancel();
        }
    }
}
