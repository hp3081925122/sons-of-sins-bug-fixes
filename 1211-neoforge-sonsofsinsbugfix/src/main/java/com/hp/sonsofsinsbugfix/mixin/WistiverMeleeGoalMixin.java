package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.WistiverEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 WistiverEntity 中 MeleeAttackGoal 攻击距离被写死为 0.0 导致近战攻击永不触发的问题（NeoForge 1.21.1）。
 */
@Mixin(WistiverEntity.class)
public abstract class WistiverMeleeGoalMixin {
    @Redirect(
            method = "registerGoals",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"
            )
    )
    private void sonsofsinsbugfix$replaceZeroReachMelee(GoalSelector selector, int priority, Goal goal) {
        if (priority == 1 && goal instanceof MeleeAttackGoal) {
            selector.addGoal(priority, new MeleeAttackGoal((PathfinderMob) (Object) this, 2.0, false));
        } else {
            selector.addGoal(priority, goal);
        }
    }
}
