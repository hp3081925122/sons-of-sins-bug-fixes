package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.entity.BetrayedGuzzlerEntity;
import net.mcreator.sonsofsins.entity.BludEntity;
import net.mcreator.sonsofsins.entity.ButcherEntity;
import net.mcreator.sonsofsins.entity.CurseEntity;
import net.mcreator.sonsofsins.entity.GuzzlerEntity;
import net.mcreator.sonsofsins.entity.KelvinEntity;
import net.mcreator.sonsofsins.entity.ProwlerEntity;
import net.mcreator.sonsofsins.entity.WalkingBedEntity;
import net.mcreator.sonsofsins.entity.WistiverEntity;
import net.mcreator.sonsofsins.procedures.BetrayedGuzzlerEntityIsHurtProcedure;
import net.mcreator.sonsofsins.procedures.CurseEntityIsHurtProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerEntityIsHurtProcedure;
import net.mcreator.sonsofsins.procedures.WistiverEntityIsHurtProcedure;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 9 个实体在 hurt() 中早于伤害免疫与 super.hurt() 触发受击过程的问题（修复 MCR-21）：
 * 1. 仅在伤害实际生效（返回值 true）后才触发对应受击过程；
 * 2. 背叛吞噬者濒死最后一击不再瞬偷玩家主手武器，受配置 allowBetrayedGuzzlerStealWeapon 控制，
 *    且将 MAINHAND 原版 dropChance 设为 0.0F，避免死亡时原版随机损毁耐久并由死亡过程完整返还武器。
 */
@Mixin({
        BetrayedGuzzlerEntity.class,
        BludEntity.class,
        ButcherEntity.class,
        CurseEntity.class,
        GuzzlerEntity.class,
        KelvinEntity.class,
        ProwlerEntity.class,
        WalkingBedEntity.class,
        WistiverEntity.class
})
public abstract class EntityHurtOrderMixin {
    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/sonsofsins/procedures/BetrayedGuzzlerEntityIsHurtProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            ),
            require = 0
    )
    private void sonsofsinsbugfix$deferBetrayedGuzzlerHurt(
            LevelAccessor world, double x, double y, double z, DamageSource source, Entity entity, Entity sourceentity) {
        // 后置到 hurt 返回 true 且实体存活后再执行
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/sonsofsins/procedures/GuzzlerEntityIsHurtProcedure;execute(Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            ),
            require = 0
    )
    private void sonsofsinsbugfix$deferGuzzlerHurt(DamageSource source, Entity entity, Entity sourceentity) {
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/sonsofsins/procedures/CurseEntityIsHurtProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            ),
            require = 0
    )
    private void sonsofsinsbugfix$deferCurseHurt(
            LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/sonsofsins/procedures/WistiverEntityIsHurtProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            ),
            require = 0
    )
    private void sonsofsinsbugfix$deferWistiverHurt(
            LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void sonsofsinsbugfix$runHurtProcedureAfterSuccess(
            DamageSource damagesource, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        Mob self = (Mob) (Object) this;
        Level world = self.level();
        Entity sourceentity = damagesource.getEntity();
        if (self instanceof BetrayedGuzzlerEntity) {
            self.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            if (self.isAlive() && SonsOfSinsBugFixConfig.COMMON.allowBetrayedGuzzlerStealWeapon.get()) {
                BetrayedGuzzlerEntityIsHurtProcedure.execute(
                        world, self.getX(), self.getY(), self.getZ(), damagesource, self, sourceentity);
            }
        } else if (self instanceof GuzzlerEntity) {
            if (self.isAlive()) {
                GuzzlerEntityIsHurtProcedure.execute(damagesource, self, sourceentity);
            }
        } else if (self instanceof CurseEntity) {
            CurseEntityIsHurtProcedure.execute(world, self.getX(), self.getY(), self.getZ(), self, sourceentity);
        } else {
            WistiverEntityIsHurtProcedure.execute(world, self.getX(), self.getY(), self.getZ(), self, sourceentity);
        }
    }
}
