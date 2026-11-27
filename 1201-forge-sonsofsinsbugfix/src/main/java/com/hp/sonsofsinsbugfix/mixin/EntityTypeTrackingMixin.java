package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 动态修正 Sons of Sins 全部 25 个实体的区块追踪半径（修复 MCR-14①/MCR-44②），支持配置热读取。
 */
@Mixin(EntityType.class)
public abstract class EntityTypeTrackingMixin {
    /**
     * 缓存实体追踪分组：0=未初始化，-1=非目标模组，1=原罪Boss/幽灵，2=常规生物/器官，3=弹射物。
     */
    @Unique
    private int sonsofsinsbugfix$trackingGroup = 0;

    @Unique
    private int sonsofsinsbugfix$originalRange = -1;

    @Inject(method = "clientTrackingRange", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$overrideTrackingRange(CallbackInfoReturnable<Integer> cir) {
        int group = this.sonsofsinsbugfix$trackingGroup;
        if (group == 0) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>) (Object) this);
            if (key == null || !"sons_of_sins".equals(key.getNamespace())) {
                this.sonsofsinsbugfix$trackingGroup = -1;
                return;
            }
            group = sonsofsinsbugfix$classify(key.getPath());
            this.sonsofsinsbugfix$originalRange = sonsofsinsbugfix$originalTrackingChunks(key.getPath());
            this.sonsofsinsbugfix$trackingGroup = group;
        }
        if (group <= 0) {
            return;
        }
        if (SonsOfSinsBugFixConfig.COMMON_SPEC.isLoaded()) {
            if (!SonsOfSinsBugFixConfig.COMMON.fixEntityTrackingRange.get()) {
                if (this.sonsofsinsbugfix$originalRange > 0) {
                    cir.setReturnValue(this.sonsofsinsbugfix$originalRange);
                }
                return;
            }
            int chunks = switch (group) {
                case 1 -> SonsOfSinsBugFixConfig.COMMON.sinBossTrackingRange.get();
                case 3 -> SonsOfSinsBugFixConfig.COMMON.projectileTrackingRange.get();
                default -> SonsOfSinsBugFixConfig.COMMON.normalMobTrackingRange.get();
            };
            cir.setReturnValue(chunks);
        }
    }

    @Unique
    private static int sonsofsinsbugfix$classify(String path) {
        return switch (path) {
            case "wistiver", "walking_bed", "blud", "prowler", "kelvin", "butcher", "curse",
                 "wistiver_jaw", "blud_trade", "blud_aggressive", "ether_ghost" -> 1;
            case "blood_pr", "null_pr" -> 3;
            default -> 2;
        };
    }

    @Unique
    private static int sonsofsinsbugfix$originalTrackingChunks(String path) {
        return switch (path) {
            case "wistiver", "blud", "butcher", "wistiver_jaw", "blud_trade", "blud_aggressive" -> 256;
            case "walking_bed", "prowler", "kelvin", "curse" -> 128;
            case "nibbler" -> 80;
            default -> 64;
        };
    }
}
