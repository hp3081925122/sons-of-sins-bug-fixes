package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修正 SonsOfSinsModEntities 注册阶段的全部 25 个实体初始区块追踪半径与非敌对实体 MobCategory（NeoForge 1.21.1）。
 */
@Mixin(value = SonsOfSinsModEntities.class, remap = false)
public abstract class SonsOfSinsModEntitiesMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private static <T extends Entity> void sonsofsinsbugfix$fixEntityBuilder(
            String registryname, EntityType.Builder<T> entityTypeBuilder,
            CallbackInfoReturnable<DeferredHolder<EntityType<?>, EntityType<T>>> cir) {
        if (registryname == null || entityTypeBuilder == null) {
            return;
        }
        int defaultChunks = switch (registryname) {
            case "wistiver", "walking_bed", "blud", "prowler", "kelvin", "butcher", "curse",
                 "wistiver_jaw", "blud_trade", "blud_aggressive", "ether_ghost" -> 10;
            case "blood_pr", "null_pr" -> 4;
            default -> 8;
        };
        entityTypeBuilder.clientTrackingRange(defaultChunks);
        entityTypeBuilder.setTrackingRange(defaultChunks);

        switch (registryname) {
            case "gulber_organs", "grub_organs", "devourer_organs", "guzzler_organs", "nibbler_organs",
                 "wistiver_jaw", "blud_trade", "blud_aggressive" ->
                    ((EntityTypeBuilderAccessor) (Object) entityTypeBuilder).sonsofsinsbugfix$setCategory(MobCategory.MISC);
            case "gulber", "devourer", "grub", "guzzler", "nibbler" ->
                    ((EntityTypeBuilderAccessor) (Object) entityTypeBuilder).sonsofsinsbugfix$setCategory(MobCategory.CREATURE);
            default -> {
            }
        }
    }
}
