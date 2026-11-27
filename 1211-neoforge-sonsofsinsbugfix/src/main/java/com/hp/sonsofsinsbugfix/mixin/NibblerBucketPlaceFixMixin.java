package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 1.21.1 Nibbler 水桶/岩浆桶右键放置过程：
 * 1. 仅服务端执行，禁止在下界等超热维度放水；
 * 2. 将 NibblerLavaBucket 中的 ResourceLocation.parse 替换为安全 tryParse，避免空器官 ID 崩服；
 * 3. 消除 1.21.1 NibblerBucketRightclickedOnBlockProcedure#isCreative 裸调客户端 Minecraft 类（MCR-03）。
 */
@Mixin(value = {
        NibblerBucketRightclickedOnBlockProcedure.class,
        NibblerLavaBucketRightclickedOnBlockProcedure.class
}, remap = false)
public abstract class NibblerBucketPlaceFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$serverOnly(
            LevelAccessor world, double x, double y, double z, Direction direction, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        if (world == null || world.isClientSide()) {
            ci.cancel();
        }
    }

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true, require = 0)
    private static void sonsofsinsbugfix$safeIsCreative(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(SafeGameModeHelper.isCreative(entity));
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"),
            require = 0
    )
    private static ResourceLocation sonsofsinsbugfix$safeParseLocation(String raw) {
        ResourceLocation parsed = raw != null && !raw.isEmpty() ? ResourceLocation.tryParse(raw) : null;
        return parsed != null ? parsed : ResourceLocation.withDefaultNamespace("air");
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setCustomName(Lnet/minecraft/network/chat/Component;)V"),
            require = 0
    )
    private static void sonsofsinsbugfix$onlyCopyRealCustomName(
            Entity spawned, Component name,
            LevelAccessor world, double x, double y, double z, Direction direction, Entity entity, ItemStack itemstack) {
        if (spawned != null && itemstack != null && itemstack.has(DataComponents.CUSTOM_NAME)) {
            spawned.setCustomName(itemstack.getHoverName().copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    )
    private static boolean sonsofsinsbugfix$noWaterInNether(LevelAccessor world, BlockPos pos, BlockState state, int flags) {
        if (world instanceof Level level && level.dimensionType().ultraWarm() && state.is(Blocks.WATER)) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.02);
            }
            return false;
        }
        return world.setBlock(pos, state, flags);
    }
}
