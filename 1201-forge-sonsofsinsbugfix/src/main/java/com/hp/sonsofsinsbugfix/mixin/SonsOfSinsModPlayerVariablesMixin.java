package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-04④（Forge 1.20.1）：PlayerVariables.readNBT 遇到 null 或非 CompoundTag 时不再抛出 NPE/ClassCastException。
 */
@Mixin(value = SonsOfSinsModVariables.PlayerVariables.class, remap = false)
public abstract class SonsOfSinsModPlayerVariablesMixin {
    @Shadow
    public ItemStack currentOpenItem;

    @Inject(method = "readNBT", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$guardReadNbt(Tag tag, CallbackInfo ci) {
        if (!(tag instanceof CompoundTag)) {
            this.currentOpenItem = ItemStack.EMPTY;
            ci.cancel();
        }
    }
}
