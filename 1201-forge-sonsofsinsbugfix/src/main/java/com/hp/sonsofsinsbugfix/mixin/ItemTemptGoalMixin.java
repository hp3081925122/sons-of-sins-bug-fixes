package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.mcreator.sonsofsins.entity.behavior.ItemTemptGoal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 优化 ItemTemptGoal：
 * 1. 静态缓存 TagKey 消除每次判定的对象分配；
 * 2. 增加 10 tick 物品扫描节流与 5 tick 重寻路节流；
 * 3. 增加 16 格最大追逐距离上限，防止追踪漂走物品失控。
 */
@Mixin(ItemTemptGoal.class)
public abstract class ItemTemptGoalMixin {
    @Unique
    private static final TagKey<Item> TAG_IS_FLESH = ItemTags.create(new ResourceLocation("sons_of_sins", "is_flesh"));
    @Unique
    private static final TagKey<Item> TAG_FLESH_BLOCKS = ItemTags.create(new ResourceLocation("sons_of_sins", "flesh_blocks"));

    @Shadow(remap = false)
    @Final
    protected PathfinderMob mob;

    @Shadow(remap = false)
    protected ItemEntity foodItem;

    @Unique
    private int sonsofsinsbugfix$scanCooldown = 0;

    @Unique
    private int sonsofsinsbugfix$repathCooldown = 0;

    @Inject(method = "isValidItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$cachedTagCheck(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(stack.is(TAG_IS_FLESH) || stack.is(TAG_FLESH_BLOCKS));
        }
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$throttleScan(CallbackInfoReturnable<Boolean> cir) {
        if (!SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            return;
        }
        if (this.sonsofsinsbugfix$scanCooldown > 0) {
            this.sonsofsinsbugfix$scanCooldown--;
            cir.setReturnValue(false);
        } else {
            this.sonsofsinsbugfix$scanCooldown = 10;
        }
    }

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    private void sonsofsinsbugfix$limitChaseDistance(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && this.foodItem != null && this.mob.distanceToSqr(this.foodItem) > 256.0) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(DDDD)Z")
    )
    private boolean sonsofsinsbugfix$throttleRepath(PathNavigation nav, double x, double y, double z, double speed) {
        if (SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()) {
            if (this.sonsofsinsbugfix$repathCooldown > 0 && !nav.isDone()) {
                this.sonsofsinsbugfix$repathCooldown--;
                return true;
            }
            this.sonsofsinsbugfix$repathCooldown = 5;
        }
        return nav.moveTo(x, y, z, speed);
    }
}
