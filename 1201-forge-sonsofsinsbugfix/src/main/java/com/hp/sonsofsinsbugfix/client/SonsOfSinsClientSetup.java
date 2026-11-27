package com.hp.sonsofsinsbugfix.client;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;

/**
 * Forge 1.20.1 客户端扩展初始化：
 * 1. 通过 AddPackFindersEvent 注册 Pack.Position.TOP 常驻内置覆盖资源包（修复 MCR-44①）；
 * 2. 注册资源重载监听器，在重载资源包时自动清空烘焙模型与贴图存在性缓存。
 */
@Mod.EventBusSubscriber(modid = SonsOfSinsBugFixMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SonsOfSinsClientSetup {
    private SonsOfSinsClientSetup() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        Path resourcePath = ModList.get()
                .getModFileById(SonsOfSinsBugFixMod.MOD_ID)
                .getFile()
                .findResource("resourcepacks/sonsofsinsbugfix_overrides");
        Pack pack = Pack.readMetaAndCreate(
                SonsOfSinsBugFixMod.MOD_ID + "_overrides",
                Component.literal("Sons of Sins Bug Fixes Overrides"),
                true,
                id -> new PathPackResources(id, true, resourcePath),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN
        );
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((barrier, manager, prepProfiler, reloadProfiler, bgExec, gameExec) ->
                barrier.wait(null).thenRunAsync(SonsOfSinsClientCache::clearResourceCaches, gameExec));
    }
}
