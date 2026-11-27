package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.EtherSwordLivingEntityIsHitWithToolProcedure;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeEntitySwingsItemProcedure;
import net.mcreator.sonsofsins.procedures.ShapeShiftingToolsBreakProcedure;
import net.mcreator.sonsofsins.procedures.TouchOfGreedLivingEntityIsHitWithItemProcedure;
import net.mcreator.sonsofsins.procedures.TouchOfGreedRightclickedProcedure;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复服务端动作栏提示硬编码英文或在服务端提前 translate().getString() 导致多人联机客户端无法本地化的问题。
 */
@Mixin(value = {
        TouchOfGreedRightclickedProcedure.class,
        TouchOfGreedLivingEntityIsHitWithItemProcedure.class,
        EtherSwordLivingEntityIsHitWithToolProcedure.class,
        PuppetOfStrifeEntitySwingsItemProcedure.class,
        ShapeShiftingToolsBreakProcedure.class
}, remap = false)
public abstract class ChatLocalizationFixMixin {

    @Redirect(
            method = "execute*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", remap = true),
            require = 0
    )
    private static MutableComponent sonsofsinsbugfix$localizeActionBar(String text) {
        if ("§eYou have no Bottle of Blood".equals(text)) {
            return Component.translatable("item.sons_of_sins.touch_no_blood");
        }
        if (text != null && text.equals(Component.translatable("item.sons_of_sins.urn_is_not_filled").getString())) {
            return Component.translatable("item.sons_of_sins.urn_is_not_filled");
        }
        return Component.literal(text == null ? "" : text);
    }
}
