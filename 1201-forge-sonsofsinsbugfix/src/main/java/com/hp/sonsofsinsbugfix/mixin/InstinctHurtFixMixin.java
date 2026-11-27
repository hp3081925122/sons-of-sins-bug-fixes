package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.InstinctHurtProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 InstinctHurtProcedure：
 * 1. 放行无视无敌伤害（/kill、虚空），防止遗骸甲挡 /kill 及巨额伤害直接扣爆全套护甲；
 * 2. 将本能传送搜点循环上限从 1000 次降至 64 次，避免受击瞬间卡顿；
 * 3. 限制遗骸甲单次耐久扣减上限并传入真实玩家（使耐久附魔生效）；
 * 4. 修复以太引擎背包检查（MCR-24）与进度判空（MCR-31）。
 */
@Mixin(value = InstinctHurtProcedure.class, remap = false)
public abstract class InstinctHurtFixMixin {
    private static final String EXEC = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;D)V";

    @Inject(method = "onEntityAttacked", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$skipBypassInvulnerability(LivingHurtEvent event, CallbackInfo ci) {
        if (event != null && event.getSource() != null && event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = EXEC, constant = @Constant(intValue = 1000))
    private static int sonsofsinsbugfix$capTeleportSearchIterations(int original) {
        return 64;
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$capArmorDamageAndPassPlayer(
            ItemStack stack, int damage, RandomSource random, ServerPlayer ignored,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        int clamped = Math.max(0, Math.min(damage, 100));
        if (clamped == 0 || stack.isEmpty()) {
            return false;
        }
        ServerPlayer sp = entity instanceof ServerPlayer p ? p : null;
        return stack.hurt(clamped, random, sp);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V")
    )
    private static void sonsofsinsbugfix$safeSetAmount(LivingHurtEvent hurtEvent, float newAmount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(newAmount);
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/AdvancementProgress;", remap = true)
    )
    private static AdvancementProgress sonsofsinsbugfix$safeAdvancementProgress(PlayerAdvancements advancements, Advancement adv) {
        if (adv == null) {
            return new AdvancementProgress();
        }
        return advancements.getOrStartProgress(adv);
    }
}
