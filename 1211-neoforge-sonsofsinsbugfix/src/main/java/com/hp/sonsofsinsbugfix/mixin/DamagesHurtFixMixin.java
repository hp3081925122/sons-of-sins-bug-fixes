package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.function.Consumer;
import net.mcreator.sonsofsins.procedures.DamagesHurtProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 1.21.1 DamagesHurtProcedure 严重缺陷：
 * 1. 修复 onEntityAttacked 传入 getOriginalDamage()（护甲减伤前数值）覆盖 setNewDamage() 导致全额穿透护甲/保护/抗性的严重 Bug（MCR-28）；
 * 2. 修复多伤害加成互相覆盖；
 * 3. 修复屠夫菜刀误查受害者骨灰坛与 isEmptying 标记；
 * 4. 修复玻璃瓶背包检查（MCR-24）与屠夫菜刀 hurtAndBreak 传入真实玩家。
 */
@Mixin(value = DamagesHurtProcedure.class, remap = false)
public abstract class DamagesHurtFixMixin {
    private static final String EXEC = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;D)V";

    @Redirect(
            method = "onEntityAttacked",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;getOriginalDamage()F")
    )
    private static float sonsofsinsbugfix$usePostArmorDamage(LivingDamageEvent.Pre event) {
        return event.getNewDamage();
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;contains(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private static boolean sonsofsinsbugfix$looseContains(Inventory inv, ItemStack stack) {
        return SonsOfSinsHelper.inventoryHasItem(inv, stack);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getCapability(Lnet/neoforged/neoforge/capabilities/EntityCapability;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private static <T, C> T sonsofsinsbugfix$checkAttackerUrn(
            Entity targetEntity, EntityCapability<T, C> cap, C context,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        Entity owner = sourceentity != null ? sourceentity : targetEntity;
        return owner.getCapability(cap, context);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putBoolean(Ljava/lang/String;Z)V")
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    )
    private static void sonsofsinsbugfix$passRealPlayerToHurt(
            ItemStack stack, int damage, ServerLevel level, ServerPlayer ignored, Consumer<Item> onBreak,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        int safeDamage = Math.max(0, damage);
        if (safeDamage == 0 || stack.isEmpty()) {
            return;
        }
        LivingEntity living = sourceentity instanceof LivingEntity le ? le : null;
        Consumer<Item> callback = living != null ? item -> living.onEquippedItemBroken(item, EquipmentSlot.MAINHAND) : onBreak;
        stack.hurtAndBreak(safeDamage, level, living instanceof ServerPlayer sp ? sp : living, callback);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V", ordinal = 0)
    )
    private static void sonsofsinsbugfix$stackHerculesDamage(
            LivingDamageEvent.Pre damageEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        damageEvent.setNewDamage(damageEvent.getNewDamage() + (newAmount - (float) amount));
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V", ordinal = 1)
    )
    private static void sonsofsinsbugfix$stackJawsDamage(
            LivingDamageEvent.Pre damageEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        damageEvent.setNewDamage(damageEvent.getNewDamage() + (newAmount - (float) amount));
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V", ordinal = 2)
    )
    private static void sonsofsinsbugfix$stackCleaverDamage(
            LivingDamageEvent.Pre damageEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        damageEvent.setNewDamage(damageEvent.getNewDamage() + (newAmount - (float) amount));
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V", ordinal = 3)
    )
    private static void sonsofsinsbugfix$multiplySinfulDamage(
            LivingDamageEvent.Pre damageEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        damageEvent.setNewDamage(damageEvent.getNewDamage() * 4.0F);
    }

    @Redirect(
            method = EXEC,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;setNewDamage(F)V", ordinal = 4)
    )
    private static void sonsofsinsbugfix$stackWistiverDamage(
            LivingDamageEvent.Pre damageEvent, float newAmount,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        damageEvent.setNewDamage(damageEvent.getNewDamage() + (newAmount - (float) amount));
    }
}
