package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.function.Consumer;
import net.mcreator.sonsofsins.item.ShapeShiftingToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 ShapeShiftingToolItem：
 * 1. hurtEnemy 误将受害者 entity 而非攻击者 sourceentity 传入 hurtAndBreak；
 * 2. useOn 改用 hurtAndBreak 传入真实玩家以触发耐久附魔与破碎反馈；
 * 3. isCorrectToolForDrops 与 getDestroySpeed 纯读路径不再使用 getOrCreateTag() 污染空物品 NBT（修复 MCR-22）。
 */
@Mixin(ShapeShiftingToolItem.class)
public abstract class ShapeShiftingToolItemFixMixin {
    @Redirect(
            method = "hurtEnemy",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    )
    private void sonsofsinsbugfix$hurtAttackerTool(
            ItemStack stack, int amount, LivingEntity wrongVictim, Consumer<LivingEntity> onBroken,
            ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        LivingEntity holder = sourceentity != null ? sourceentity : wrongVictim;
        stack.hurtAndBreak(amount, holder, i -> i.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }

    @Redirect(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z")
    )
    private boolean sonsofsinsbugfix$useProperHurtAndBreak(
            ItemStack stack, int amount, RandomSource random, ServerPlayer ignored, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            stack.hurtAndBreak(amount, player, p -> p.broadcastBreakEvent(context.getHand()));
            return false;
        }
        return stack.hurt(amount, random, null);
    }

    @Redirect(
            method = {"isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z", "getDestroySpeed"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;")
    )
    private CompoundTag sonsofsinsbugfix$readOnlyTag(ItemStack stack) {
        return stack.hasTag() ? stack.getTag() : SonsOfSinsHelper.EMPTY_READONLY_TAG;
    }
}
