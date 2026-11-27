package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.NibblerLavaBucketRightclickedOnBlockProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/**
 * 修复 1.20.1 Nibbler 水桶/岩浆桶右键放置过程：
 * 1. 仅服务端放置方块与水，禁止在下界等 ultraWarm 维度凭空放置水源；
 * 2. 防止空或无效器官 NBT 字符串构造 ResourceLocation 抛异常；
 * 3. 保护 spawn() 返回 null 时 setCustomName 空指针。
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

    @Redirect(
            method = "execute",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", remap = true)
    )
    private static ResourceLocation sonsofsinsbugfix$safeResourceLocation(String raw) {
        ResourceLocation parsed = raw != null && !raw.isEmpty() ? ResourceLocation.tryParse(raw) : null;
        return parsed != null ? parsed : new ResourceLocation("minecraft", "air");
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setCustomName(Lnet/minecraft/network/chat/Component;)V", remap = true),
            require = 0
    )
    private static void sonsofsinsbugfix$safeSetCustomName(
            Entity spawned, Component name,
            LevelAccessor world, double x, double y, double z, Direction direction, Entity entity, ItemStack itemstack) {
        if (spawned != null && itemstack != null && itemstack.hasCustomHoverName()) {
            spawned.setCustomName(itemstack.getHoverName().copy());
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", remap = true)
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
