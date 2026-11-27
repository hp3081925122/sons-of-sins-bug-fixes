package com.hp.sonsofsinsbugfix.client;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * NeoForge 1.21.1 客户端初始化：
 * 注册 TOP 优先级内置客户端覆盖资源包与资源重载清缓存监听。
 * 注：NeoForge 21.1 的 ClientExtensionsManager#earlyInit 仍会自动采集原模组物品的 initializeClient，
 * 因此遗骸胸甲的模型缓存与渲染缓冲修复改由独立客户端 Mixin 完成，避免 RegisterClientExtensionsEvent 重复注册冲突。
 */
@EventBusSubscriber(modid = SonsOfSinsBugFixMod.MOD_ID, value = Dist.CLIENT)
public final class SonsOfSinsClientSetup {

    private SonsOfSinsClientSetup() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(SonsOfSinsBugFixMod.MOD_ID, "resourcepacks/sonsofsinsbugfix_overrides"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("Sons of Sins Bug Fixes Overrides"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
            );
        }
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((barrier, manager, prepProfiler, reloadProfiler, bgExec, gameExec) ->
                barrier.wait(null).thenRunAsync(SonsOfSinsClientCache::clearResourceCaches, gameExec));
    }
}
