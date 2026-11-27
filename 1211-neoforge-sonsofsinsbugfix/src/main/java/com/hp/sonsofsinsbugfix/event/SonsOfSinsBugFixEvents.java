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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 服务端/双端运行时事件补丁（NeoForge 1.21.1）。
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
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.getPersistentData().getBoolean("sonsofsinsbugfix_greed_nophysics")) {
            if (!player.hasEffect(SonsOfSinsModMobEffects.GREED)) {
                if (!player.isSpectator()) {
                    player.noPhysics = false;
                }
                player.getPersistentData().remove("sonsofsinsbugfix_greed_nophysics");
            }
        }
    }
}
