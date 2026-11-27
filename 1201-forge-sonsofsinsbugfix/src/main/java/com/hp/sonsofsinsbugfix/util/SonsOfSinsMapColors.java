package com.hp.sonsofsinsbugfix.util;

import java.util.Map;
import net.minecraft.world.level.material.MapColor;

/**
 * 原罪之子 22 个缺失 MapColor 方块的语义与纹理均色映射表（双版本共用逻辑）。
 */
public final class SonsOfSinsMapColors {
    private static final Map<String, MapColor> MAP_COLORS = Map.ofEntries(
            Map.entry("block_of_blazing_hearts", MapColor.TERRACOTTA_YELLOW),
            Map.entry("block_of_creeper_ribs", MapColor.COLOR_GREEN),
            Map.entry("block_of_enderman_muscles", MapColor.TERRACOTTA_GRAY),
            Map.entry("block_of_golem_cuirass", MapColor.TERRACOTTA_WHITE),
            Map.entry("block_of_hearts", MapColor.CRIMSON_STEM),
            Map.entry("block_of_ice_heart", MapColor.ICE),
            Map.entry("block_of_muscles", MapColor.TERRACOTTA_MAGENTA),
            Map.entry("block_of_ravager_muscle", MapColor.TERRACOTTA_GRAY),
            Map.entry("block_of_ribs", MapColor.TERRACOTTA_MAGENTA),
            Map.entry("block_of_slime_rear", MapColor.GRASS),
            Map.entry("block_of_spider_heart", MapColor.TERRACOTTA_LIGHT_GRAY),
            Map.entry("block_of_strider_muscle", MapColor.TERRACOTTA_RED),
            Map.entry("flesh_block", MapColor.CRIMSON_STEM),
            Map.entry("soul_steel_block", MapColor.DEEPSLATE),
            Map.entry("soul_steel_tiles", MapColor.STONE),
            Map.entry("soul_steel_stairs", MapColor.STONE),
            Map.entry("soul_steel_slab", MapColor.STONE),
            Map.entry("soul_steel_door", MapColor.STONE),
            Map.entry("soul_steel_trapdoor", MapColor.STONE),
            Map.entry("soul_steel_chain", MapColor.DEEPSLATE),
            Map.entry("ether_lantern", MapColor.METAL),
            Map.entry("urn_block", MapColor.DEEPSLATE)
    );

    private SonsOfSinsMapColors() {
    }

    public static MapColor resolve(String blockPath) {
        return MAP_COLORS.get(blockPath);
    }
}
