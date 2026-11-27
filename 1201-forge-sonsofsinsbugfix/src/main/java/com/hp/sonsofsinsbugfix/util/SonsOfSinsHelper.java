package com.hp.sonsofsinsbugfix.util;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
 * 通用运行时辅助方法（Forge 1.20.1）：覆盖安全背包物品匹配、安全格式化 Tooltip、安全破块检查、友伤校验与空只读 NBT。
 */
public final class SonsOfSinsHelper {
    /**
     * 只读空 NBT 复用实例，禁止外部写入（用于替代 1.20.1 纯只读路径中的 getOrCreateTag）。
     */
    public static final CompoundTag EMPTY_READONLY_TAG = new CompoundTag();

    private SonsOfSinsHelper() {
    }

    /**
     * 按物品类型宽松匹配玩家背包（替代严格 NBT 比对 Inventory#contains(new ItemStack(...))，修复 MCR-24）。
     */
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

    /**
     * 安全格式化血肉躯壳与纷争人偶的槽位名称 Tooltip（修复 substring(3, len - 1) 越界崩溃与英文硬编码）。
     */
    public static String formatOrganTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "§7" + Component.translatable("tooltip.sons_of_sins.empty").getString();
        }
        String raw = stack.getHoverName().getString();
        String stripped = raw.replaceAll("§.", "");
        return "§7[" + stripped + "]";
    }

    /**
     * 安全的原罪怪破块判定：拒绝不可破坏方块（destroySpeed < 0）、尊重 mobGriefing，并在存在方块实体时改为掉落模式避免吞箱子。
     */
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

    /**
     * 判断已驯服血肉造物是否允许攻击目标玩家（保护主人、同队队友以及关闭 PvP 的服务器玩家）。
     */
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

    /**
     * 安全获取并令首个乘客停止骑乘（修复抓人处决后乘客死亡自动下马导致的空指针崩服）。
     */
    public static void safeStopFirstPassengerRiding(Entity vehicle) {
        if (vehicle == null) {
            return;
        }
        Entity passenger = vehicle.getFirstPassenger();
        if (passenger != null) {
            passenger.stopRiding();
        }
    }

    /**
     * 安全的维度路径首字母大写格式化（跳过前导/连续下划线产生的空段，避免 substring(0, 1) 越界崩溃）。
     */
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
