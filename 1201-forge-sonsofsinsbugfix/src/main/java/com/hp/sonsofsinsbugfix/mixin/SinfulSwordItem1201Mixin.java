package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.SinfulSwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 SinfulSwordItem 误引用 sinful_shovel.description_0 本地化键的问题。
 */
@Mixin(SinfulSwordItem.class)
public abstract class SinfulSwordItem1201Mixin {

    @Redirect(
            method = "appendHoverText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent sonsofsinsbugfix$useSwordDescKey(String key) {
        return Component.translatable("item.sons_of_sins.sinful_sword.description_0");
    }
}
