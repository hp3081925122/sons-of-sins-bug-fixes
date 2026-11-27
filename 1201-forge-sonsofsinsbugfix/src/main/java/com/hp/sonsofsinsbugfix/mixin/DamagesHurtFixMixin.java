package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.DamagesHurtProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.20.1 DamagesHurtProcedure：
 * 1. 修复屠夫菜刀误检查受害者而非攻击者的背包骨灰坛与 isEmptying 标记；
 * 2. 修复多个伤害加成同时触发时后分支覆盖前分支修改的问题，并在 event == null 时安全跳过；
 * 3. 修复玻璃瓶背包检查严格比对 NBT（MCR-24）；
 * 4. 修复屠夫菜刀耐久损耗传 null 玩家导致耐久附魔失效；
 * 5. 修复数据包移除进度时 _adv 为 null 崩溃（MCR-31）。
 */
@Mixin(value = DamagesHurtProcedure.class, remap = false)
public abstract class DamagesHurtFixMixin {
    private static final String EXEC = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;D)V";

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;")
    )
    private static <T> LazyOptional<T> sonsofsinsbugfix$checkAttackerUrn(
            Entity targetEntity, Capability<T> cap, Direction side,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        Entity owner = sourceentity != null ? sourceentity : targetEntity;
        return owner.getCapability(cap, side);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putBoolean(Ljava/lang/String;Z)V", remap = true)
    )
    private static void sonsofsinsbugfix$emptyAttackerUrn(
            CompoundTag tag, String key, boolean value,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if ("isEmptying".equals(key) && sourceentity != null) {
            sourceentity.getPersistentData().putBoolean(key, value);
        } else {
            tag.putBoolean(key, value);
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean sonsofsinsbugfix$passRealPlayerToHurt(
            ItemStack stack, int damage, RandomSource random, ServerPlayer ignored,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        int safeDamage = Math.max(0, damage);
        if (safeDamage == 0) {
            return false;
        }
        ServerPlayer sp = sourceentity instanceof ServerPlayer p ? p : null;
        return stack.hurt(safeDamage, random, sp);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 0)
    )
    private static void sonsofsinsbugfix$stackHerculesDamage(
            LivingHurtEvent hurtEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(hurtEvent.getAmount() + (newAmount - (float) amount));
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 1)
    )
    private static void sonsofsinsbugfix$stackJawsDamage(
            LivingHurtEvent hurtEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(hurtEvent.getAmount() + (newAmount - (float) amount));
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 2)
    )
    private static void sonsofsinsbugfix$stackCleaverDamage(
            LivingHurtEvent hurtEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(hurtEvent.getAmount() + (newAmount - (float) amount));
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 3)
    )
    private static void sonsofsinsbugfix$multiplySinfulDamage(
            LivingHurtEvent hurtEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(hurtEvent.getAmount() * 4.0F);
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 4)
    )
    private static void sonsofsinsbugfix$stackWistiverDamage(
            LivingHurtEvent hurtEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        if (hurtEvent != null) {
            hurtEvent.setAmount(hurtEvent.getAmount() + (newAmount - (float) amount));
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
