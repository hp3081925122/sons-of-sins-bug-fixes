package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.function.Consumer;
import net.mcreator.sonsofsins.procedures.InstinctHurtProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 InstinctHurtProcedure：
 * 1. 修复 onEntityAttacked 传入 getOriginalDamage() 导致本能效果反而无视护甲/保护减伤的严重 Bug（MCR-28）；
 * 2. 放行无视无敌伤害（/kill、虚空）；
 * 3. 本能传送搜点循环上限从 1000 降至 64；
 * 4. 限制遗骸甲单次耐久扣减上限并传入真实玩家、修复以太引擎背包检查（MCR-24）。
 */
@Mixin(value = InstinctHurtProcedure.class, remap = false)
public abstract class InstinctHurtFixMixin {
    private static final String EXEC = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;D)V";

    @Inject(method = "onEntityAttacked", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$skipBypassInvulnerability(LivingDamageEvent.Pre event, CallbackInfo ci) {
        if (event != null && event.getSource() != null && event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "onEntityAttacked",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;getOriginalDamage()F")
    )
    private static float sonsofsinsbugfix$usePostArmorDamage(LivingDamageEvent.Pre event) {
        return event.getNewDamage();
    }

    @ModifyConstant(method = EXEC, constant = @Constant(intValue = 1000))
    private static int sonsofsinsbugfix$capTeleportSearchIterations(int original) {
        return 64;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    )
    private static void sonsofsinsbugfix$capArmorDamageAndPassPlayer(
            ItemStack stack, int damage, ServerLevel level, ServerPlayer ignored, Consumer<Item> onBreak,
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, double amount) {
        int clamped = Math.max(0, Math.min(damage, 100));
        if (clamped == 0 || stack.isEmpty()) {
            return;
        }
        LivingEntity living = entity instanceof LivingEntity le ? le : null;
        stack.hurtAndBreak(clamped, level, living instanceof ServerPlayer sp ? sp : living, onBreak);
    }
}
