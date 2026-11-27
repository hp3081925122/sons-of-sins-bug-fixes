package com.hp.sonsofsinsbugfix.mixin;

import java.util.List;
import java.util.function.Predicate;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeEntitySwingsItemProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 PuppetOfStrife 劫掠者肌肉冲击波误判施法者而非目标为 LivingEntity、导致震飞并破坏掉落物/画/展示框及按名字去重的问题（NeoForge 1.21.1）。
 */
@Mixin(value = PuppetOfStrifeEntitySwingsItemProcedure.class, remap = false)
public abstract class PuppetOfStrifeAoeFixMixin {

    @Unique
    private static final ThreadLocal<Entity> SONSOFSINSBUGFIX$CASTER = new ThreadLocal<>();

    @Inject(method = "execute", at = @At("HEAD"))
    private static void sonsofsinsbugfix$captureCaster(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        SONSOFSINSBUGFIX$CASTER.set(entity);
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private static void sonsofsinsbugfix$releaseCaster(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        SONSOFSINSBUGFIX$CASTER.remove();
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", remap = true)
    )
    private static <T extends Entity> List<T> sonsofsinsbugfix$onlyLivingTargets(
            LevelAccessor world, Class<T> clazz, AABB aabb, Predicate<? super T> filter) {
        Entity caster = SONSOFSINSBUGFIX$CASTER.get();
        return world.getEntitiesOfClass(clazz, aabb, e -> e instanceof LivingEntity && e != caster && filter.test(e));
    }
}
