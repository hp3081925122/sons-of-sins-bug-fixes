package com.hp.sonsofsinsbugfix.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * 仅在客户端侧安全查询玩家游戏模式的隔离辅助类，防止专用服务端加载 net.minecraft.client.Minecraft。
 */
public final class SafeClientGameMode {
    private SafeClientGameMode() {
    }

    public static boolean isClientGameMode(Player player, GameType expected) {
        if (player == null) {
            return false;
        }
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn == null) {
            return false;
        }
        PlayerInfo info = conn.getPlayerInfo(player.getGameProfile().getId());
        return info != null && info.getGameMode() == expected;
    }
}
