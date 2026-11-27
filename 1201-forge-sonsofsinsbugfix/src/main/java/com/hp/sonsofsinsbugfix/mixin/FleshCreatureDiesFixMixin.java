package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.procedures.BetrayedGuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.DevourerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GrubEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GulberEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.NibblerEntityDiesProcedure;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 6 个生物死亡过程（BetrayedGuzzler/Guzzler/Devourer/Grub/Gulber/Nibbler）：
 * 1. 掉落主手武器时先 .copy() 且忽略空栈，避免空 ItemEntity 与共享引用损毁；
 * 2. 细胞分裂（Cell Division）增加 16 格内同类数量上限（防指数刷爆服务器）与防重入锁；
 * 3. 分裂子体继承器官装备时自动 .copy() 避免共享可变 ItemStack；
 * 4. 保护 spawn() 返回 null 时的空指针，并使用 hasCustomName() 替代服务端翻译串比较（修复 MCR-43）。
 */
@Mixin(value = {
        BetrayedGuzzlerEntityDiesProcedure.class,
        DevourerEntityDiesProcedure.class,
        GrubEntityDiesProcedure.class,
        GulberEntityDiesProcedure.class,
        GuzzlerEntityDiesProcedure.class,
        NibblerEntityDiesProcedure.class
}, remap = false)
public abstract class FleshCreatureDiesFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardReentryAndDivisionCap(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (entity == null || entity.isRemoved()) {
            ci.cancel();
            return;
        }
        if (entity.getPersistentData().getBoolean("sonsofsinsbugfix_dies_handled")) {
            ci.cancel();
            return;
        }
        entity.getPersistentData().putBoolean("sonsofsinsbugfix_dies_handled", true);

        // 细胞分裂上限控制：16 格内同类达到上限时阻止再次分裂
        if (entity instanceof LivingEntity living
                && living.hasEffect(SonsOfSinsModMobEffects.CELL_DIVISION.get())
                && world instanceof ServerLevel serverLevel) {
            int cap = SonsOfSinsBugFixConfig.COMMON.cellDivisionNearbyCap.get();
            AABB box = entity.getBoundingBox().inflate(16.0);
            int nearbySame = serverLevel.getEntitiesOfClass(entity.getClass(), box, Entity::isAlive).size();
            if (nearbySame >= cap) {
                living.removeEffect(SonsOfSinsModMobEffects.CELL_DIVISION.get());
            }
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;", remap = true),
            require = 0
    )
    private static ItemStack sonsofsinsbugfix$copyMainHandForDrop(LivingEntity living) {
        ItemStack stack = living.getMainHandItem();
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", remap = true),
            require = 0
    )
    private static boolean sonsofsinsbugfix$skipEmptyItemEntity(ServerLevel level, Entity toAdd) {
        if (toAdd instanceof ItemEntity itemEntity && itemEntity.getItem().isEmpty()) {
            return false;
        }
        return level.addFreshEntity(toAdd);
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", remap = true),
            require = 0
    )
    private static void sonsofsinsbugfix$copyOrganEquipmentOnSplit(LivingEntity child, EquipmentSlot slot, ItemStack stack) {
        if (child != null) {
            child.setItemSlot(slot, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setCustomName(Lnet/minecraft/network/chat/Component;)V", remap = true),
            require = 0
    )
    private static void sonsofsinsbugfix$onlyCopyRealCustomName(Entity child, Component name, LevelAccessor world, Entity entity) {
        if (child != null && entity != null && entity.hasCustomName()) {
            child.setCustomName(entity.getCustomName().copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;", remap = true),
            require = 0
    )
    private static ItemStack sonsofsinsbugfix$onlyCopyCarcassCustomName(ItemStack carcass, Component name, LevelAccessor world, Entity entity) {
        if (entity != null && entity.hasCustomName()) {
            return carcass.setHoverName(entity.getCustomName().copy());
        }
        return carcass;
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", remap = true),
            require = 0
    )
    private static void sonsofsinsbugfix$safeSetSplitVelocity(Entity child, net.minecraft.world.phys.Vec3 vec) {
        if (child != null) {
            child.setDeltaMovement(vec);
        }
    }
}
