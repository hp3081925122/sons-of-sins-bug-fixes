package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.FleshAxeItem;
import net.mcreator.sonsofsins.item.FleshHoeItem;
import net.mcreator.sonsofsins.item.FleshShovelItem;
import net.mcreator.sonsofsins.item.FleshSwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 血肉斧/锄/铲/剑全部错引 flesh_pickaxe.description_0 提示键的问题，改为使用各自已定义的本地化键。
 */
@Mixin({FleshAxeItem.class, FleshHoeItem.class, FleshShovelItem.class, FleshSwordItem.class})
public abstract class FleshToolsTooltip1211Mixin {

    @Redirect(
            method = "appendHoverText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent sonsofsinsbugfix$useOwnDescriptionKey(String key) {
        Object self = this;
        if (self instanceof FleshAxeItem) {
            return Component.translatable("item.sons_of_sins.flesh_axe.description_0");
        }
        if (self instanceof FleshHoeItem) {
            return Component.translatable("item.sons_of_sins.flesh_hoe.description_0");
        }
        if (self instanceof FleshShovelItem) {
            return Component.translatable("item.sons_of_sins.flesh_shovel.description_0");
        }
        if (self instanceof FleshSwordItem) {
            return Component.translatable("item.sons_of_sins.flesh_sword.description_0");
        }
        return Component.translatable(key);
    }
}
