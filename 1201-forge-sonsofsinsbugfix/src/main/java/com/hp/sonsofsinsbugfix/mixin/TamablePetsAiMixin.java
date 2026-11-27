package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.entity.DevourerEntity;
import net.mcreator.sonsofsins.entity.GrubEntity;
import net.mcreator.sonsofsins.entity.GulberEntity;
import net.mcreator.sonsofsins.entity.GuzzlerEntity;
import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 5 种可驯服血肉造物的 AI 目标缺陷：
 * 1. 将误注册在 goalSelector 中的 OwnerHurtByTargetGoal 移入正确的 targetSelector；
 * 2. 覆写 canAttack(LivingEntity)，防止已驯服造物（尤其是狩猎模式的 Guzzler）攻击主人、同队队友或关闭 PvP 的服务器玩家。
 */
@Mixin({DevourerEntity.class, GrubEntity.class, GulberEntity.class, GuzzlerEntity.class, NibblerEntity.class})
public abstract class TamablePetsAiMixin extends TamableAnimal {
    protected TamablePetsAiMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Redirect(
            method = "registerGoals",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"
            )
    )
    private void sonsofsinsbugfix$routeOwnerHurtByTargetGoal(GoalSelector selector, int priority, Goal goal) {
        if (selector == this.goalSelector && goal instanceof OwnerHurtByTargetGoal) {
            this.targetSelector.addGoal(priority, goal);
        } else {
            selector.addGoal(priority, goal);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (!SonsOfSinsHelper.canPetTargetPlayer(this, target)) {
            return false;
        }
        return super.canAttack(target);
    }
}
