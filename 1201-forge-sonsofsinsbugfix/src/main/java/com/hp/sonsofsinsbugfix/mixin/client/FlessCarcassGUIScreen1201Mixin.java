package com.hp.sonsofsinsbugfix.mixin.client;

import com.hp.sonsofsinsbugfix.client.SonsOfSinsClientCache;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import java.util.Map;
import java.util.function.Supplier;
import net.mcreator.sonsofsins.client.gui.FlessCarcassGUIScreen;
import net.mcreator.sonsofsins.entity.DevourerEntity;
import net.mcreator.sonsofsins.entity.DevourerOrgansEntity;
import net.mcreator.sonsofsins.entity.GrubEntity;
import net.mcreator.sonsofsins.entity.GrubOrgansEntity;
import net.mcreator.sonsofsins.entity.GulberEntity;
import net.mcreator.sonsofsins.entity.GulberOrgansEntity;
import net.mcreator.sonsofsins.entity.GuzzlerEntity;
import net.mcreator.sonsofsins.entity.GuzzlerOrgansEntity;
import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.mcreator.sonsofsins.entity.NibblerOrgansEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.procedures.DevourerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesDevourerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesGrubGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesGuzzlerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesNibblerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.DoesWightGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.GrubGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.GuzzlerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.NibblerGUIDisplayProcedure;
import net.mcreator.sonsofsins.procedures.WightGUIDisplayProcedure;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 优化 1.20.1 血肉躯壳界面（FlessCarcassGUIScreen）：
 * 1. 先检查展示条件再获取实体，避免未命中分支每帧无意义构造 4 个完整 Mob；
 * 2. 缓存全部 10 种预览实体（5 种本体 + 5 种器官展示体），每次渲染仅刷新装备与同步字段，消除每帧 new Entity()；
 * 3. 缓存界面叠加层贴图 ResourceLocation，避免每帧重复解析字符串。
 */
@Mixin(FlessCarcassGUIScreen.class)
public abstract class FlessCarcassGUIScreen1201Mixin {

    @Unique
    private static final ResourceLocation SONSOFSINSBUGFIX$OVERLAY_TEX =
            new ResourceLocation("sons_of_sins", "textures/screens/flesh_carcass_gui_overlay.png");

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/WightGUIDisplayProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;", remap = false)
    )
    private Entity sonsofsinsbugfix$cachedWight(LevelAccessor world, Entity entity) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheGuiPreviewEntities.get()) {
            return WightGUIDisplayProcedure.execute(world, entity);
        }
        if (!DoesWightGUIDisplayProcedure.execute(entity)) {
            return null;
        }
        return sonsofsinsbugfix$getOrUpdatePreview(world, entity, "gulber");
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/DevourerGUIDisplayProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;", remap = false)
    )
    private Entity sonsofsinsbugfix$cachedDevourer(LevelAccessor world, Entity entity) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheGuiPreviewEntities.get()) {
            return DevourerGUIDisplayProcedure.execute(world, entity);
        }
        if (!DoesDevourerGUIDisplayProcedure.execute(entity)) {
            return null;
        }
        return sonsofsinsbugfix$getOrUpdatePreview(world, entity, "devourer");
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/GrubGUIDisplayProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;", remap = false)
    )
    private Entity sonsofsinsbugfix$cachedGrub(LevelAccessor world, Entity entity) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheGuiPreviewEntities.get()) {
            return GrubGUIDisplayProcedure.execute(world, entity);
        }
        if (!DoesGrubGUIDisplayProcedure.execute(entity)) {
            return null;
        }
        return sonsofsinsbugfix$getOrUpdatePreview(world, entity, "grub");
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/GuzzlerGUIDisplayProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;", remap = false)
    )
    private Entity sonsofsinsbugfix$cachedGuzzler(LevelAccessor world, Entity entity) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheGuiPreviewEntities.get()) {
            return GuzzlerGUIDisplayProcedure.execute(world, entity);
        }
        if (!DoesGuzzlerGUIDisplayProcedure.execute(entity)) {
            return null;
        }
        return sonsofsinsbugfix$getOrUpdatePreview(world, entity, "guzzler");
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/mcreator/sonsofsins/procedures/NibblerGUIDisplayProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;", remap = false)
    )
    private Entity sonsofsinsbugfix$cachedNibbler(LevelAccessor world, Entity entity) {
        if (!SonsOfSinsBugFixConfig.CLIENT.cacheGuiPreviewEntities.get()) {
            return NibblerGUIDisplayProcedure.execute(world, entity);
        }
        if (!DoesNibblerGUIDisplayProcedure.execute(entity)) {
            return null;
        }
        return sonsofsinsbugfix$getOrUpdatePreview(world, entity, "nibbler");
    }

    @Redirect(
            method = "renderBg",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;")
    )
    private ResourceLocation sonsofsinsbugfix$cachedOverlayTexture(String path) {
        if ("sons_of_sins:textures/screens/flesh_carcass_gui_overlay.png".equals(path)) {
            return SONSOFSINSBUGFIX$OVERLAY_TEX;
        }
        return SonsOfSinsClientCache.cachedLocation(path);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static ItemStack sonsofsinsbugfix$getSlot(Entity entity, int idx) {
        if (entity instanceof Player player
                && player.containerMenu instanceof Supplier<?> supplier
                && supplier.get() instanceof Map<?, ?> map) {
            Object slotObj = map.get(idx);
            if (slotObj instanceof Slot slot) {
                return slot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private static Entity sonsofsinsbugfix$getOrUpdatePreview(LevelAccessor world, Entity entity, String kind) {
        if (!(world instanceof ClientLevel level) || entity == null) {
            return null;
        }
        boolean fullFlesh = !sonsofsinsbugfix$getSlot(entity, 0).isEmpty()
                && !sonsofsinsbugfix$getSlot(entity, 1).isEmpty()
                && !sonsofsinsbugfix$getSlot(entity, 2).isEmpty()
                && !sonsofsinsbugfix$getSlot(entity, 6).isEmpty()
                && !sonsofsinsbugfix$getSlot(entity, 7).isEmpty()
                && !sonsofsinsbugfix$getSlot(entity, 8).isEmpty();

        ItemStack ribs = sonsofsinsbugfix$getSlot(entity, 3);
        ItemStack heart = sonsofsinsbugfix$getSlot(entity, 4);
        ItemStack muscle = sonsofsinsbugfix$getSlot(entity, 5);

        if (fullFlesh) {
            String key = kind + "_full";
            Entity cached = SonsOfSinsClientCache.getCachedPreviewEntity(level, key);
            if (cached == null) {
                cached = switch (kind) {
                    case "devourer" -> new DevourerEntity(SonsOfSinsModEntities.DEVOURER.get(), level);
                    case "grub" -> new GrubEntity(SonsOfSinsModEntities.GRUB.get(), level);
                    case "guzzler" -> new GuzzlerEntity(SonsOfSinsModEntities.GUZZLER.get(), level);
                    case "nibbler" -> new NibblerEntity(SonsOfSinsModEntities.NIBBLER.get(), level);
                    default -> new GulberEntity(SonsOfSinsModEntities.GULBER.get(), level);
                };
                SonsOfSinsClientCache.putCachedPreviewEntity(level, key, cached);
            }
            if (cached instanceof LivingEntity living) {
                living.setItemSlot(EquipmentSlot.FEET, ribs);
                living.setItemSlot(EquipmentSlot.LEGS, heart);
                living.setItemSlot(EquipmentSlot.CHEST, muscle);
            }
            return cached;
        }

        String key = kind + "_organs";
        Entity cached = SonsOfSinsClientCache.getCachedPreviewEntity(level, key);
        if (cached == null) {
            cached = switch (kind) {
                case "devourer" -> new DevourerOrgansEntity(SonsOfSinsModEntities.DEVOURER_ORGANS.get(), level);
                case "grub" -> new GrubOrgansEntity(SonsOfSinsModEntities.GRUB_ORGANS.get(), level);
                case "guzzler" -> new GuzzlerOrgansEntity(SonsOfSinsModEntities.GUZZLER_ORGANS.get(), level);
                case "nibbler" -> new NibblerOrgansEntity(SonsOfSinsModEntities.NIBBLER_ORGANS.get(), level);
                default -> new GulberOrgansEntity(SonsOfSinsModEntities.GULBER_ORGANS.get(), level);
            };
            SonsOfSinsClientCache.putCachedPreviewEntity(level, key, cached);
        }

        String ribsId = "";
        if (!ribs.isEmpty()) {
            Item item = ribs.getItem();
            if (item == SonsOfSinsModItems.CREEPER_RIBS.get() || item == SonsOfSinsModItems.CHARGED_CREEPER_RIBS.get()) {
                item = SonsOfSinsModItems.CREEPER_RIBS.get();
            }
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
            ribsId = rl != null ? rl.toString() : "";
        }
        String heartId = "";
        if (!heart.isEmpty()) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(heart.getItem());
            heartId = rl != null ? rl.toString() : "";
        }
        String muscleId = "";
        if (!muscle.isEmpty()) {
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(muscle.getItem());
            muscleId = rl != null ? rl.toString() : "";
        }

        if (cached instanceof DevourerOrgansEntity e) {
            sonsofsinsbugfix$setOrganData(e, DevourerOrgansEntity.DATA_ribs_id, DevourerOrgansEntity.DATA_heart_id, DevourerOrgansEntity.DATA_muscle_id, ribsId, heartId, muscleId);
        } else if (cached instanceof GrubOrgansEntity e) {
            sonsofsinsbugfix$setOrganData(e, GrubOrgansEntity.DATA_ribs_id, GrubOrgansEntity.DATA_heart_id, GrubOrgansEntity.DATA_muscle_id, ribsId, heartId, muscleId);
        } else if (cached instanceof GuzzlerOrgansEntity e) {
            sonsofsinsbugfix$setOrganData(e, GuzzlerOrgansEntity.DATA_ribs_id, GuzzlerOrgansEntity.DATA_heart_id, GuzzlerOrgansEntity.DATA_muscle_id, ribsId, heartId, muscleId);
        } else if (cached instanceof NibblerOrgansEntity e) {
            sonsofsinsbugfix$setOrganData(e, NibblerOrgansEntity.DATA_ribs_id, NibblerOrgansEntity.DATA_heart_id, NibblerOrgansEntity.DATA_muscle_id, ribsId, heartId, muscleId);
        } else if (cached instanceof GulberOrgansEntity e) {
            sonsofsinsbugfix$setOrganData(e, GulberOrgansEntity.DATA_ribs_id, GulberOrgansEntity.DATA_heart_id, GulberOrgansEntity.DATA_muscle_id, ribsId, heartId, muscleId);
        }
        return cached;
    }

    @Unique
    private static void sonsofsinsbugfix$setOrganData(
            Entity entity,
            EntityDataAccessor<String> ribsKey,
            EntityDataAccessor<String> heartKey,
            EntityDataAccessor<String> muscleKey,
            String ribsId, String heartId, String muscleId) {
        entity.getEntityData().set(ribsKey, ribsId);
        entity.getEntityData().set(heartKey, heartId);
        entity.getEntityData().set(muscleKey, muscleId);
    }
}
