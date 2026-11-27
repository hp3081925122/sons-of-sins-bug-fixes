package com.hp.sonsofsinsbugfix.event;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.util.WorkQueueCleaner;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.world.inventory.FlessCarcassGUIMenu;
import net.mcreator.sonsofsins.world.inventory.PuppetOfStrifeGUIMenu;
import net.mcreator.sonsofsins.world.inventory.UrnGUIMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 服务端/双端运行时事件补丁（Forge 1.20.1）：
 * 1. 停服/切档时清空 SonsOfSinsMod.workQueue 防止内存泄漏与跨档污染（MCR-01）；
 * 2. 在服务端侧正确处理丢弃便携容器时关闭对应 GUI 菜单（修复原模组错标 Dist.CLIENT 的 MCR-06②）；
 * 3. 防止 Greed 药水异常退出后玩家永久卡在 noPhysics = true 穿地坠落虚空（MCR-29）。
 */
public final class SonsOfSinsBugFixEvents {
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        if (SonsOfSinsBugFixConfig.COMMON.fixWorkQueueLeaks.get()) {
            WorkQueueCleaner.clearQueue();
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        ItemStack stack = event.getEntity().getItem();
        if ((stack.is(SonsOfSinsModItems.FLESH_CARCASS.get()) && player.containerMenu instanceof FlessCarcassGUIMenu)
                || (stack.is(SonsOfSinsModItems.PUPPET_OF_STRIFE.get()) && player.containerMenu instanceof PuppetOfStrifeGUIMenu)
                || (stack.is(SonsOfSinsModItems.URN.get()) && player.containerMenu instanceof UrnGUIMenu)) {
            player.closeContainer();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.getPersistentData().getBoolean("sonsofsinsbugfix_greed_nophysics")) {
            if (!player.hasEffect(SonsOfSinsModMobEffects.GREED.get())) {
                if (!player.isSpectator()) {
                    player.noPhysics = false;
                }
                player.getPersistentData().remove("sonsofsinsbugfix_greed_nophysics");
            }
        }
    }
}
