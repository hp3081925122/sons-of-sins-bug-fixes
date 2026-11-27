package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.CursedHeadLivingEntityIsHitWithItemProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 诅咒之首剥甲逻辑（仅服务端、尊重绑定诅咒、掉落护甲先 copy、正确判断攻击者创造模式）。
 */
@Mixin(value = CursedHeadLivingEntityIsHitWithItemProcedure.class, remap = false)
public abstract class CursedHeadFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$serverOnly(
            LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, ItemStack itemstack, CallbackInfo ci) {
        if (world == null || world.isClientSide()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack sonsofsinsbugfix$respectBindingCurseAndCopy(LivingEntity living, EquipmentSlot slot) {
        ItemStack armor = living.getItemBySlot(slot);
        if (armor.isEmpty() || EnchantmentHelper.has(armor, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return ItemStack.EMPTY;
        }
        return armor.copy();
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private static void sonsofsinsbugfix$checkAttackerCreative(
            ItemStack stack, int count,
            LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, ItemStack itemstack) {
        if (!(sourceentity instanceof Player attacker && attacker.getAbilities().instabuild)) {
            stack.shrink(count);
        }
    }
}
