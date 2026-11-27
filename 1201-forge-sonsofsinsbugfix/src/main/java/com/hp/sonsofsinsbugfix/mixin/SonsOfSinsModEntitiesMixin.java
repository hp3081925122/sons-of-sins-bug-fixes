package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修正 SonsOfSinsModEntities 注册阶段的全部 25 个实体初始区块追踪半径与非敌对实体 MobCategory（修复 MCR-14①/② 与 MCR-44②）。
 */
@Mixin(value = SonsOfSinsModEntities.class, remap = false)
public abstract class SonsOfSinsModEntitiesMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private static <T extends Entity> void sonsofsinsbugfix$fixEntityBuilder(
            String registryname, EntityType.Builder<T> entityTypeBuilder, CallbackInfoReturnable<RegistryObject<EntityType<T>>> cir) {
        if (registryname == null || entityTypeBuilder == null) {
            return;
        }
        // 同步修正 Forge 的 trackingRangeSupplier 与原版 clientTrackingRange 默认值
        int defaultChunks = switch (registryname) {
            case "wistiver", "walking_bed", "blud", "prowler", "kelvin", "butcher", "curse",
                 "wistiver_jaw", "blud_trade", "blud_aggressive", "ether_ghost" -> 10;
            case "blood_pr", "null_pr" -> 4;
            default -> 8;
        };
        entityTypeBuilder.clientTrackingRange(defaultChunks);
        entityTypeBuilder.setTrackingRange(defaultChunks);

        // 修正非敌对实体与可驯服血肉造物的 MobCategory，防止和平模式消失及占满敌对刷怪上限
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
