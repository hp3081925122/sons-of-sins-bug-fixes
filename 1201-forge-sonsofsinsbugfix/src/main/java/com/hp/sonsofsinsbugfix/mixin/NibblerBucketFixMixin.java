package com.hp.sonsofsinsbugfix.mixin;

import com.mojang.datafixers.util.Pair;
import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.mcreator.sonsofsins.procedures.NibblerBucketedProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 NibblerBucketedProcedure：
 * 1. 支持副手持桶；
 * 2. 防止对已移除或濒死实体重复装桶；
 * 3. 客户端提前预测装桶并消费交互，阻止穿透到 BucketItem#use 倒出幽灵水/岩浆；
 * 4. 服务端装桶成功后显式设置 InteractionResult.CONSUME 再取消事件。
 */
@Mixin(value = NibblerBucketedProcedure.class, remap = false)
public abstract class NibblerBucketFixMixin {
    @Redirect(
            method = "onRightClickEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$EntityInteract;getHand()Lnet/minecraft/world/InteractionHand;")
    )
    private static InteractionHand sonsofsinsbugfix$allowBothHands(PlayerInteractEvent.EntityInteract event) {
        return event.getEntity().getUsedItemHand();
    }

    @Inject(method = "onRightClickEntity", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardAndClientPredict(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event == null || event.getTarget() == null) {
            ci.cancel();
            return;
        }
        Entity target = event.getTarget();
        if (target.isRemoved() || (target instanceof LivingEntity living && living.isDeadOrDying())) {
            ci.cancel();
            return;
        }
        // 客户端根据实体归属与抗火器官提前消费交互，防止穿透到 BucketItem#use 倒出幽灵流体
        if (event.getLevel() != null && event.getLevel().isClientSide()
                && target instanceof NibblerEntity nibbler
                && event.getEntity() != null) {
            Player player = event.getEntity();
            if (!nibbler.isTame() || nibbler.isOwnedBy(player)) {
                Item requiredBucket = sonsofsinsbugfix$requiresLavaBucket(nibbler)
                        ? Items.LAVA_BUCKET
                        : Items.WATER_BUCKET;
                boolean mainMatches = player.getMainHandItem().getItem() == requiredBucket;
                boolean offMatches = player.getOffhandItem().getItem() == requiredBucket;
                if ((mainMatches || offMatches) && event.isCancelable()) {
                    boolean activeHandMatches = player.getItemInHand(event.getHand()).getItem() == requiredBucket;
                    event.setCancellationResult(activeHandMatches ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
                    event.setCanceled(true);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onRightClickEntity", at = @At("TAIL"))
    private static void sonsofsinsbugfix$cancelEventAfterBucketed(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event != null && event.getTarget() != null && event.getTarget().isRemoved() && event.isCancelable()) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @Unique
    private static boolean sonsofsinsbugfix$requiresLavaBucket(NibblerEntity nibbler) {
        if (nibbler.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return true;
        }
        // 客户端不同步生物药水表，补充检查已同步的护甲槽器官（如炽足兽肌肉）是否附带抗火效果
        return sonsofsinsbugfix$hasFireResistanceFood(nibbler.getItemBySlot(EquipmentSlot.FEET), nibbler)
                || sonsofsinsbugfix$hasFireResistanceFood(nibbler.getItemBySlot(EquipmentSlot.LEGS), nibbler)
                || sonsofsinsbugfix$hasFireResistanceFood(nibbler.getItemBySlot(EquipmentSlot.CHEST), nibbler)
                || sonsofsinsbugfix$hasFireResistanceFood(nibbler.getItemBySlot(EquipmentSlot.HEAD), nibbler);
    }

    @Unique
    private static boolean sonsofsinsbugfix$hasFireResistanceFood(ItemStack stack, NibblerEntity nibbler) {
        if (stack.isEmpty()) {
            return false;
        }
        FoodProperties food = stack.getFoodProperties(nibbler);
        if (food == null) {
            return false;
        }
        for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
            if (pair.getFirst() != null && pair.getFirst().getEffect() == MobEffects.FIRE_RESISTANCE) {
                return true;
            }
        }
        return false;
    }
}
