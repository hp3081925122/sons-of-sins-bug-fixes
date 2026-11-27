package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.mcreator.sonsofsins.entity.CurseEntity;
import net.mcreator.sonsofsins.procedures.CurseOnEntityTickUpdateProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 优化 1.21.1 诅咒（Curse）每 tick 64×64×64 全实体扫描为服务端每 5 tick O(1) UUID 查找，16×16×16 缩窄为 Player.class（MCR-32）。
 */
@Mixin(value = CurseOnEntityTickUpdateProcedure.class, remap = false)
public abstract class CurseScanOptimizeMixin {

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;")
    )
    @SuppressWarnings("unchecked")
    private static <T extends Entity> List<T> sonsofsinsbugfix$optimizeCurseScan(
            LevelAccessor world, Class<T> clazz, AABB box, Predicate<? super T> pred,
            LevelAccessor w, double x, double y, double z, Entity entity) {
        if (SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get() && entity instanceof CurseEntity) {
            if (box.getXsize() > 32.0) {
                if (!(world instanceof ServerLevel serverLevel) || entity.tickCount % 5 != 0) {
                    return Collections.emptyList();
                }
                String uuidStr = entity.getPersistentData().getString("currentTarget");
                if (uuidStr.isEmpty()) {
                    return Collections.emptyList();
                }
                try {
                    Entity found = serverLevel.getEntity(UUID.fromString(uuidStr));
                    if (found != null && found.isAlive() && box.contains(found.position())) {
                        return Collections.singletonList((T) found);
                    }
                } catch (IllegalArgumentException ignored) {
                }
                return Collections.emptyList();
            }
            return (List<T>) world.getEntitiesOfClass(Player.class, box, p -> true);
        }
        return world.getEntitiesOfClass(clazz, box, pred);
    }
}
