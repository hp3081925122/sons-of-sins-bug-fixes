package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.NibblerBucketedProcedure;
import net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 Nibbler 装桶与放桶 3 个过程：
 * 1. NibblerBucketedProcedure：支持副手持桶、防止对已移除实体重复装桶、成功后取消交互事件、仅保留真实自定义名；
 * 2. NibblerBucketRightclickedOnBlockProcedure：禁止在下界等超热维度凭空放水、防止空/非法器官 ID 抛出 ResourceLocationException、保护 spawn==null；
 * 3. NibblerLavaBucketRightclickedOnBlockProcedure：防止非法器官 ID 崩溃与 spawn==null 空指针。
 */
@Mixin(value = NibblerBucketedProcedure.class, remap = false)
public abstract class NibblerBucketFixMixin {
    @Redirect(
            method = "onRightClickEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$EntityInteract;getHand()Lnet/minecraft/world/InteractionHand;")
    )
    private static InteractionHand sonsofsinsbugfix$allowBothHands(PlayerInteractEvent.EntityInteract event) {
        return event.getEntity().getUsedItemHand();
    }

    @Inject(method = "onRightClickEntity", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$skipRemovedTarget(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event == null || event.getTarget() == null || event.getTarget().isRemoved()) {
            ci.cancel();
        }
    }

    @Inject(method = "onRightClickEntity", at = @At("TAIL"))
    private static void sonsofsinsbugfix$cancelEventAfterBucketed(PlayerInteractEvent.EntityInteract event, CallbackInfo ci) {
        if (event != null && event.getTarget() != null && event.getTarget().isRemoved() && event.isCancelable()) {
            event.setCanceled(true);
        }
    }
}
