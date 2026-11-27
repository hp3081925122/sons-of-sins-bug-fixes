package com.hp.sonsofsinsbugfix.client;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 客户端渲染缓存中心（NeoForge 1.21.1）。
 */
public final class SonsOfSinsClientCache {
    private static EntityModelSet lastModelSet;
    private static final Map<ModelLayerLocation, ModelPart> BAKED_PARTS = new HashMap<>();
    private static HumanoidModel<?> cachedRemnantHelmet;
    private static HumanoidModel<?> cachedRemnantChestplate;

    private static final Map<ResourceLocation, Boolean> TEXTURE_EXISTS = new ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> RL_CACHE = new ConcurrentHashMap<>();

    private static ClientLevel lastPreviewLevel;
    private static final Map<String, Entity> GUI_PREVIEW_ENTITIES = new HashMap<>();

    private static long lastCalendarCheckMs;
    private static boolean cachedIsDecember;
    private static boolean cachedIsDayOfDead;

    private SonsOfSinsClientCache() {
    }

    public static void clearResourceCaches() {
        lastModelSet = null;
        BAKED_PARTS.clear();
        cachedRemnantHelmet = null;
        cachedRemnantChestplate = null;
        TEXTURE_EXISTS.clear();
    }

    public static ModelPart getOrBakeLayer(EntityModelSet modelSet, ModelLayerLocation layer) {
        if (modelSet != lastModelSet) {
            lastModelSet = modelSet;
            BAKED_PARTS.clear();
            cachedRemnantHelmet = null;
            cachedRemnantChestplate = null;
        }
        return BAKED_PARTS.computeIfAbsent(layer, modelSet::bakeLayer);
    }

    public static HumanoidModel<?> getCachedRemnantHelmet(EntityModelSet modelSet) {
        if (modelSet != lastModelSet) {
            clearResourceCaches();
            lastModelSet = modelSet;
        }
        return cachedRemnantHelmet;
    }

    public static void setCachedRemnantHelmet(EntityModelSet modelSet, HumanoidModel<?> model) {
        lastModelSet = modelSet;
        cachedRemnantHelmet = model;
    }

    public static HumanoidModel<?> getCachedRemnantChestplate(EntityModelSet modelSet) {
        if (modelSet != lastModelSet) {
            clearResourceCaches();
            lastModelSet = modelSet;
        }
        return cachedRemnantChestplate;
    }

    public static void setCachedRemnantChestplate(EntityModelSet modelSet, HumanoidModel<?> model) {
        lastModelSet = modelSet;
        cachedRemnantChestplate = model;
    }

    public static ResourceLocation cachedLocation(String path) {
        return RL_CACHE.computeIfAbsent(path, ResourceLocation::tryParse);
    }

    public static ResourceLocation cachedLocation(String namespace, String path) {
        return RL_CACHE.computeIfAbsent(namespace + ":" + path, k -> ResourceLocation.tryBuild(namespace, path));
    }

    public static boolean textureExists(ResourceLocation rl) {
        if (rl == null) {
            return false;
        }
        return TEXTURE_EXISTS.computeIfAbsent(rl, loc ->
                Minecraft.getInstance().getResourceManager().getResource(loc).isPresent());
    }

    public static Entity getCachedPreviewEntity(ClientLevel level, String key) {
        if (level != lastPreviewLevel) {
            lastPreviewLevel = level;
            GUI_PREVIEW_ENTITIES.clear();
        }
        return GUI_PREVIEW_ENTITIES.get(key);
    }

    public static void putCachedPreviewEntity(ClientLevel level, String key, Entity entity) {
        if (level != lastPreviewLevel) {
            lastPreviewLevel = level;
            GUI_PREVIEW_ENTITIES.clear();
        }
        GUI_PREVIEW_ENTITIES.put(key, entity);
    }

    private static void refreshCalendarIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCalendarCheckMs > 60_000L) {
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            cachedIsDecember = (month == Calendar.DECEMBER);
            cachedIsDayOfDead = (month == Calendar.NOVEMBER && (day == 1 || day == 2));
            lastCalendarCheckMs = now;
        }
    }

    public static boolean isDecember() {
        refreshCalendarIfNeeded();
        return cachedIsDecember;
    }

    public static boolean isDayOfDead() {
        refreshCalendarIfNeeded();
        return cachedIsDayOfDead;
    }
}
