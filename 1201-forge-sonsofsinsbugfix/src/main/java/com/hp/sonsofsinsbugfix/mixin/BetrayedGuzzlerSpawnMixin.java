package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.entity.BetrayedGuzzlerEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 为背叛吞噬者的自然刷新谓词补充安全群系排除（阻止在蘑菇岛与深暗之域刷怪，修复 MCR-15）。
 */
@Mixin(value = BetrayedGuzzlerEntity.class, remap = false)
public abstract class BetrayedGuzzlerSpawnMixin {
    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/SpawnPlacements;register(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/SpawnPlacements$Type;Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/world/entity/SpawnPlacements$SpawnPredicate;)V",
                    remap = true
            ),
            index = 3
    )
    private static SpawnPlacements.SpawnPredicate<BetrayedGuzzlerEntity> sonsofsinsbugfix$wrapSpawnPredicate(
            SpawnPlacements.SpawnPredicate<BetrayedGuzzlerEntity> original) {
        return (entityType, world, reason, pos, random) -> {
            if (SonsOfSinsBugFixConfig.COMMON.excludeSafeBiomesFromBetrayedGuzzler.get()) {
                Holder<Biome> biome = world.getBiome(pos);
                if (biome.is(Biomes.MUSHROOM_FIELDS) || biome.is(Biomes.DEEP_DARK)) {
                    return false;
                }
            }
            return original.test(entityType, world, reason, pos, random);
        };
    }
}
