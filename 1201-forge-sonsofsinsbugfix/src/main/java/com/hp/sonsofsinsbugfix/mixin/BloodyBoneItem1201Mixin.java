package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.BloodyBoneItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 BloodyBoneItem 悬浮提示硬编码英文的问题，改为读取已存在的本地化键。
 */
@Mixin(BloodyBoneItem.class)
public abstract class BloodyBoneItem1201Mixin {

    @Redirect(
            method = "appendHoverText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent sonsofsinsbugfix$useTranslatableDesc(String text) {
        return Component.translatable("item.sons_of_sins.bloody_bone.description_0");
    }
}
