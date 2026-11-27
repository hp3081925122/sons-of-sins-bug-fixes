package com.hp.sonsofsinsbugfix.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Sons of Sins Bug Fixes 通用与客户端配置项（Forge 1.20.1）。
 */
public final class SonsOfSinsBugFixConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    private SonsOfSinsBugFixConfig() {
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue fixWorkQueueLeaks;
        public final ForgeConfigSpec.BooleanValue fixEntityTrackingRange;
        public final ForgeConfigSpec.IntValue sinBossTrackingRange;
        public final ForgeConfigSpec.IntValue normalMobTrackingRange;
        public final ForgeConfigSpec.IntValue projectileTrackingRange;
        public final ForgeConfigSpec.BooleanValue excludeSafeBiomesFromBetrayedGuzzler;
        public final ForgeConfigSpec.BooleanValue fixSinMonsterBlockBreaking;
        public final ForgeConfigSpec.BooleanValue allowBetrayedGuzzlerStealWeapon;
        public final ForgeConfigSpec.IntValue cellDivisionNearbyCap;
        public final ForgeConfigSpec.BooleanValue allowSinMonsterDespawn;
        public final ForgeConfigSpec.BooleanValue protectTeammatesFromPetHunting;
        public final ForgeConfigSpec.BooleanValue fixMapColors;
        public final ForgeConfigSpec.BooleanValue reduceTickOverhead;

        private Common(ForgeConfigSpec.Builder b) {
            b.push("general");
            fixWorkQueueLeaks = b.comment("修复 SonsOfSinsMod 延迟任务队列的 tick<=0 永久内存泄漏、异常中断、跨档残留与血池灌爆问题")
                    .define("fixWorkQueueLeaks", true);
            fixMapColors = b.comment("为原模组缺失 MapColor 的 22 个方块补齐地图色，修复 Xaero 地图与原版地图穿透隐形")
                    .define("fixMapColors", true);
            reduceTickOverhead = b.comment("优化高频方块与实体 tick 开销（骨灰坛事件驱动更新、血池空转节流、变形工具按需写 NBT、药水判空与生物扫描优化）")
                    .define("reduceTickOverhead", true);
            b.pop();

            b.push("entities");
            fixEntityTrackingRange = b.comment("修正全部 25 个实体错按方块数填写的超大区块追踪半径（原 64~256 区块）")
                    .define("fixEntityTrackingRange", true);
            sinBossTrackingRange = b.comment("原罪 Boss 与幽灵实体的区块追踪半径（单位：区块）")
                    .defineInRange("sinBossTrackingRange", 10, 4, 32);
            normalMobTrackingRange = b.comment("常规血肉生物与器官实体的区块追踪半径（单位：区块）")
                    .defineInRange("normalMobTrackingRange", 8, 3, 24);
            projectileTrackingRange = b.comment("弹射物实体的区块追踪半径（单位：区块）")
                    .defineInRange("projectileTrackingRange", 4, 2, 16);
            excludeSafeBiomesFromBetrayedGuzzler = b.comment("阻止背叛吞噬者在蘑菇岛与深暗之域等安全群系自然刷新")
                    .define("excludeSafeBiomesFromBetrayedGuzzler", true);
            fixSinMonsterBlockBreaking = b.comment("阻止原罪怪破坏不可破坏方块（基岩/命令方块/传送门框架等）与无掉落吃箱子，并遵循 mobGriefing 及缓存射线检测")
                    .define("fixSinMonsterBlockBreaking", true);
            allowBetrayedGuzzlerStealWeapon = b.comment("是否允许背叛吞噬者受击时抢夺玩家主手武器（开启时亦已修复丢物、残血瞬偷与耐久随机损毁问题）")
                    .define("allowBetrayedGuzzlerStealWeapon", true);
            cellDivisionNearbyCap = b.comment("细胞分裂（Cell Division）死亡分裂时的 16 格内同类实体密度上限")
                    .defineInRange("cellDivisionNearbyCap", 8, 1, 64);
            allowSinMonsterDespawn = b.comment("是否允许原罪怪在远离玩家时自然消失（默认 false 保持原版 Boss 不刷掉设计）")
                    .define("allowSinMonsterDespawn", false);
            protectTeammatesFromPetHunting = b.comment("防止已驯服血肉造物（如狩猎模式的 Guzzler）攻击主人、同队队友或在关闭 PvP 的服务器上攻击其他玩家")
                    .define("protectTeammatesFromPetHunting", true);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue cacheBakedLayerModels;
        public final ForgeConfigSpec.BooleanValue cacheGuiPreviewEntities;
        public final ForgeConfigSpec.BooleanValue fixRemnantChestplateRender;
        public final ForgeConfigSpec.BooleanValue skipMissingAddonOrganTextures;
        public final ForgeConfigSpec.BooleanValue skipDeadFogColorScan;

        private Client(ForgeConfigSpec.Builder b) {
            b.push("rendering");
            cacheBakedLayerModels = b.comment("缓存全部 24 个实体渲染层与遗骸头盔的烘焙模型，消除每帧 bakeLayer() 导致的严重掉帧")
                    .define("cacheBakedLayerModels", true);
            cacheGuiPreviewEntities = b.comment("缓存血肉躯壳界面中的 5 个预览实体，避免每渲染帧新建 5 个完整 Mob")
                    .define("cacheGuiPreviewEntities", true);
            fixRemnantChestplateRender = b.comment("修复遗骸胸甲在 renderToBuffer 中绕过传入缓冲导致的悬浮重复模型遮挡视野与附魔光泽丢失问题")
                    .define("fixRemnantChestplateRender", true);
            skipMissingAddonOrganTextures = b.comment("当附属模组器官缺少对应体表贴图时安全跳过该层渲染，避免紫黑缺失材质与数组越界崩溃")
                    .define("skipMissingAddonOrganTextures", true);
            skipDeadFogColorScan = b.comment("跳过每帧空扫实体但从未调用 setColor 的无效雾效颜色计算过程")
                    .define("skipDeadFogColorScan", true);
            b.pop();
        }
    }
}
