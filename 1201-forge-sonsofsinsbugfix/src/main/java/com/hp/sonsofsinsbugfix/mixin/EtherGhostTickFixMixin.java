package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import net.mcreator.sonsofsins.procedures.EtherGhostOnEntityTickUpdateProcedure;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.20.1 EtherGhostOnEntityTickUpdateProcedure：
 * 1. 防止已移除实体重入导致重复变身刷出多个原罪 Boss；
 * 2. 安全格式化维度名称（跳过连续下划线 "__" 导致的 substring(0, 1) 越界崩服）；
 * 3. 使用服务端安全广播，并优化每 tick 216 次方块扫描（客户端与非必要 tick 跳过，静态复用 WITHER_IMMUNE 标签）。
 */
@Mixin(value = EtherGhostOnEntityTickUpdateProcedure.class, remap = false)
public abstract class EtherGhostTickFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$guardRemoved(
            LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (entity == null || entity.isRemoved()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;split(Ljava/lang/String;)[Ljava/lang/String;")
    )
    private static String[] sonsofsinsbugfix$safeSplitDimensionName(String raw, String regex) {
        return new String[]{SonsOfSinsHelper.safeFormatDimensionName(raw)};
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", remap = true)
    )
    private static void sonsofsinsbugfix$safeBroadcastSummon(
            PlayerList playerList, Component ignored, boolean overlay,
            LevelAccessor world, double x, double y, double z, Entity entity) {
        ResourceKey<Level> dimKey = world instanceof Level level
                ? level.dimension()
                : (world instanceof WorldGenLevel wgl ? wgl.getLevel().dimension() : Level.OVERWORLD);
        String formattedDim = SonsOfSinsHelper.safeFormatDimensionName(dimKey.location().getPath());
        String template = Component.translatable("entity.sons_of_sins.sin_summon").getString();
        if (template.contains("[Dimension]")) {
            playerList.broadcastSystemMessage(Component.literal(template.replace("[Dimension]", formattedDim)), overlay);
        } else {
            playerList.broadcastSystemMessage(Component.translatable("entity.sons_of_sins.sin_summon", formattedDim), overlay);
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/BlockTags;create(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/tags/TagKey;", remap = false)
    )
    private static net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> sonsofsinsbugfix$reuseWitherImmuneTag(
            net.minecraft.resources.ResourceLocation rl) {
        return BlockTags.WITHER_IMMUNE;
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", remap = true)
    )
    private static net.minecraft.world.level.block.state.BlockState sonsofsinsbugfix$throttleBlockScan(
            LevelAccessor world, net.minecraft.core.BlockPos pos,
            LevelAccessor w, double x, double y, double z, Entity entity) {
        if (SonsOfSinsBugFixConfig.COMMON.reduceTickOverhead.get()
                && (world.isClientSide() || (entity != null && entity.tickCount % 5 != 0))) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return world.getBlockState(pos);
    }
}
