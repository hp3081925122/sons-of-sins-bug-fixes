package com.hp.sonsofsinsbugfix.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * 跨端安全的游戏模式判定工具：替代 MCR 生成的 checkGamemode 匿名内部类（修复 MCR-03 专服崩服与客户端 getConnection 空指针）。
 */
public final class SafeGameModeHelper {
    private SafeGameModeHelper() {
    }

    public static boolean isGameMode(Entity entity, GameType expected) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.getGameModeForPlayer() == expected;
        }
        if (entity instanceof Player player && entity.level().isClientSide()) {
            return SafeClientGameMode.isClientGameMode(player, expected);
        }
        return false;
    }
}
