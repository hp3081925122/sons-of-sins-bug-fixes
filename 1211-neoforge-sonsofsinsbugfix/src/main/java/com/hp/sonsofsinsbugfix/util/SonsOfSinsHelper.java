package com.hp.sonsofsinsbugfix.util;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 通用运行时辅助方法（NeoForge 1.21.1）。
 */
public final class SonsOfSinsHelper {
    private SonsOfSinsHelper() {
    }

    public static boolean inventoryHasItem(Inventory inv, ItemStack template) {
        if (inv == null || template == null || template.isEmpty()) {
            return false;
        }
        Item target = template.getItem();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(target)) {
                return true;
            }
        }
        return false;
    }

    public static String formatOrganTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "§7" + Component.translatable("tooltip.sons_of_sins.empty").getString();
        }
        String raw = stack.getHoverName().getString();
        String stripped = raw.replaceAll("§.", "");
        return "§7[" + stripped + "]";
    }

    public static boolean safeSinBreakBlock(LevelAccessor world, BlockPos pos) {
        if (!(world instanceof Level level) || level.isClientSide()) {
            return false;
        }
        if (SonsOfSinsBugFixConfig.COMMON.fixSinMonsterBlockBreaking.get()) {
            if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(BlockTags.WITHER_IMMUNE)) {
                return false;
            }
            float destroySpeed = state.getDestroySpeed(level, pos);
            if (destroySpeed < 0.0F || destroySpeed > 50.0F) {
                return false;
            }
            boolean hasContainer = level.getBlockEntity(pos) != null;
            return level.destroyBlock(pos, hasContainer);
        }
        return level.destroyBlock(pos, false);
    }

    public static boolean canPetTargetPlayer(TamableAnimal pet, LivingEntity target) {
        if (pet == null || target == null) {
            return false;
        }
        if (!SonsOfSinsBugFixConfig.COMMON.protectTeammatesFromPetHunting.get()) {
            return true;
        }
        if (!(target instanceof Player targetPlayer)) {
            return true;
        }
        if (pet.isTame()) {
            if (pet.isOwnedBy(targetPlayer)) {
                return false;
            }
            LivingEntity owner = pet.getOwner();
            if (owner != null && owner.isAlliedTo(targetPlayer)) {
                return false;
            }
            MinecraftServer server = pet.getServer();
            if (server != null && !server.isPvpAllowed()) {
                return false;
            }
            if (owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(targetPlayer)) {
                return false;
            }
        }
        return true;
    }

    public static void safeStopFirstPassengerRiding(Entity vehicle) {
        if (vehicle == null) {
            return;
        }
        Entity passenger = vehicle.getFirstPassenger();
        if (passenger != null) {
            passenger.stopRiding();
        }
    }

    public static String safeFormatDimensionName(String rawPath) {
        if (rawPath == null || rawPath.isEmpty()) {
            return "Unknown";
        }
        String[] parts = rawPath.replace('_', ' ').trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.length() > 0 ? sb.toString() : rawPath;
    }
}
