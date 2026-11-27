package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.procedures.BetrayedGuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.DevourerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GrubEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GulberEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.NibblerEntityDiesProcedure;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 的 6 个生物死亡过程（BetrayedGuzzler/Guzzler/Devourer/Grub/Gulber/Nibbler）：
 * 1. 恢复 1.21.1 丢失的幼体（isBaby）过滤，阻止细胞分裂幼体死亡再掉带全套器官的血肉躯壳无限刷器官；
 * 2. 掉落主手武器先 .copy() 且忽略空栈；
 * 3. 细胞分裂上限控制与防重入；
 * 4. 分裂子体器官装备自动 .copy()、保护 spawn()==null 空指针，使用 hasCustomName() 替代服务端翻译串比较。
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

        if (entity instanceof LivingEntity living
                && living.hasEffect(SonsOfSinsModMobEffects.CELL_DIVISION)
                && world instanceof ServerLevel serverLevel) {
            int cap = SonsOfSinsBugFixConfig.COMMON.cellDivisionNearbyCap.get();
            AABB box = entity.getBoundingBox().inflate(16.0);
            int nearbySame = serverLevel.getEntitiesOfClass(entity.getClass(), box, Entity::isAlive).size();
            if (nearbySame >= cap) {
                living.removeEffect(SonsOfSinsModMobEffects.CELL_DIVISION);
            }
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"),
            require = 0
    )
    private static ItemStack sonsofsinsbugfix$copyMainHandForDrop(LivingEntity living) {
        ItemStack stack = living.getMainHandItem();
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0
    )
    private static boolean sonsofsinsbugfix$skipEmptyOrBabyCarcassDrop(ServerLevel level, Entity toAdd, LevelAccessor world, Entity dyingEntity) {
        if (toAdd instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) {
                return false;
            }
            // 修复 1.21.1 幼体死亡掉落血肉躯壳无限刷器官的回归漏洞
            if (dyingEntity instanceof LivingEntity living && living.isBaby()
                    && stack.getItem() == net.mcreator.sonsofsins.init.SonsOfSinsModItems.FLESH_CARCASS.get()) {
                return false;
            }
        }
        return level.addFreshEntity(toAdd);
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"),
            require = 0
    )
    private static void sonsofsinsbugfix$copyOrganEquipmentOnSplit(LivingEntity child, EquipmentSlot slot, ItemStack stack) {
        if (child != null) {
            child.setItemSlot(slot, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setCustomName(Lnet/minecraft/network/chat/Component;)V"),
            require = 0
    )
    private static void sonsofsinsbugfix$onlyCopyRealCustomName(Entity child, Component name, LevelAccessor world, Entity entity) {
        if (child != null && entity != null && entity.hasCustomName()) {
            child.setCustomName(entity.getCustomName().copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"),
            require = 0
    )
    @SuppressWarnings("unchecked")
    private static <T> T sonsofsinsbugfix$onlyCopyCarcassCustomName(
            ItemStack carcass, DataComponentType<T> type, T value, LevelAccessor world, Entity entity) {
        if (type == DataComponents.CUSTOM_NAME) {
            if (entity != null && entity.hasCustomName()) {
                return carcass.set(type, (T) entity.getCustomName().copy());
            }
            return null;
        }
        return carcass.set(type, value);
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"),
            require = 0
    )
    private static void sonsofsinsbugfix$safeSetSplitVelocity(Entity child, Vec3 vec) {
        if (child != null) {
            child.setDeltaMovement(vec);
        }
    }
}
