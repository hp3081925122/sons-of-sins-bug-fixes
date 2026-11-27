package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.EtherGhostEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.mcreator.sonsofsins.procedures.WhenEntityDie2Procedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 WhenEntityDie2Procedure 中 Blud Boss 召唤死代码（NeoForge 1.21.1）。
 */
@Mixin(value = WhenEntityDie2Procedure.class, remap = false)
public abstract class WhenEntityDie2FixMixin {
    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private static boolean sonsofsinsbugfix$spawnBludWhenOverFiveVillagers(ServerLevel serverLevel, Entity spawned) {
        if (spawned instanceof EtherGhostEntity ghost) {
            int variant = ghost.getEntityData().get(EtherGhostEntity.DATA_sin_variant);
            if (variant < 0) {
                Entity blud = SonsOfSinsModEntities.BLUD.get().spawn(
                        serverLevel,
                        BlockPos.containing(ghost.getX(), ghost.getY(), ghost.getZ()),
                        MobSpawnType.MOB_SUMMONED
                );
                if (blud != null) {
                    blud.setYRot(ghost.getYRot());
                    blud.setYBodyRot(ghost.getYRot());
                    blud.setYHeadRot(ghost.getYRot());
                    blud.setXRot(ghost.getXRot());
                }
                return true;
            }
        }
        return serverLevel.addFreshEntity(spawned);
    }
}
