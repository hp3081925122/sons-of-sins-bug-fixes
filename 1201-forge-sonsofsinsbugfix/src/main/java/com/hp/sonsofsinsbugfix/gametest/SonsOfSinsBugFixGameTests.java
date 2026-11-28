package com.hp.sonsofsinsbugfix.gametest;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.mcreator.sonsofsins.block.entity.UrnBlockBlockEntity;
import net.mcreator.sonsofsins.entity.BetrayedGuzzlerEntity;
import net.mcreator.sonsofsins.entity.BloodPrEntity;
import net.mcreator.sonsofsins.entity.BludAggressiveEntity;
import net.mcreator.sonsofsins.entity.BludTradeEntity;
import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.mcreator.sonsofsins.entity.NullPrEntity;
import net.mcreator.sonsofsins.entity.WistiverJawEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModBlocks;
import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipe;
import net.mcreator.sonsofsins.procedures.AcidBloodstreamOnEffectActiveTickProcedure;
import net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation2Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation3Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.UrnItemInInventoryTickProcedure;
import net.mcreator.sonsofsins.procedures.UrnSpecialInformationProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Sons of Sins Bug Fixes（Forge 1.20.1）服务端 GameTest 回归验收套件。
 */
@GameTestHolder(SonsOfSinsBugFixMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SonsOfSinsBugFixGameTests {

    private SonsOfSinsBugFixGameTests() {
    }

    private static void assertTrue(GameTestHelper helper, boolean cond, String msg) {
        if (!cond) {
            helper.fail(msg);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void mixinAuditPasses(GameTestHelper helper) {
        MixinEnvironment.getCurrentEnvironment().audit();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void workQueueZeroTickAndExceptionSafe(GameTestHelper helper) {
        AtomicBoolean zeroRan = new AtomicBoolean(false);
        AtomicBoolean afterThrowRan = new AtomicBoolean(false);
        SonsOfSinsMod.queueServerWork(0, () -> zeroRan.set(true));
        SonsOfSinsMod.queueServerWork(1, () -> {
            throw new RuntimeException("intentional_test_exception");
        });
        SonsOfSinsMod.queueServerWork(2, () -> afterThrowRan.set(true));

        helper.runAfterDelay(5, () -> {
            assertTrue(helper, zeroRan.get(), "tick<=0 队列任务应立即/次刻执行而不泄漏");
            assertTrue(helper, afterThrowRan.get(), "单任务异常不应中断后续延迟任务执行");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void mapColorsAndFleshBlockLight(GameTestHelper helper) {
        int checked = 0;
        for (RegistryObject<Block> reg : SonsOfSinsModBlocks.REGISTRY.getEntries()) {
            Block block = reg.get();
            BlockState state = block.defaultBlockState();
            MapColor color = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            assertTrue(helper, color != MapColor.NONE, "方块缺失 MapColor: " + reg.getId());
            checked++;
        }
        assertTrue(helper, checked >= 22, "应核查至少 22 个方块，实际: " + checked);

        BlockState flesh = SonsOfSinsModBlocks.FLESH_BLOCK.get().defaultBlockState();
        assertTrue(helper, flesh.getLightBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) == 15, "FleshBlock 应不透光(lightBlock=15)");
        assertTrue(helper, flesh.is(BlockTags.MINEABLE_WITH_HOE), "FleshBlock 应归属 mineable/hoe");
        assertTrue(helper, SonsOfSinsModBlocks.URN_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE), "UrnBlock 应归属 mineable/pickaxe");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void entityTrackingAndCategories(GameTestHelper helper) {
        int count = 0;
        for (RegistryObject<EntityType<?>> reg : SonsOfSinsModEntities.REGISTRY.getEntries()) {
            EntityType<?> type = reg.get();
            int range = type.clientTrackingRange();
            assertTrue(helper, range >= 2 && range <= 16, "实体追踪半径越界: " + reg.getId() + "=" + range);
            count++;
        }
        assertTrue(helper, count >= 25, "应核查全部 25 个实体，实际: " + count);

        assertTrue(helper, SonsOfSinsModEntities.BLOOD_PR.get().getCategory() == MobCategory.MISC, "blood_pr 应为 MISC");
        assertTrue(helper, SonsOfSinsModEntities.NULL_PR.get().getCategory() == MobCategory.MISC, "null_pr 应为 MISC");
        assertTrue(helper, SonsOfSinsModEntities.BLUD_TRADE.get().getCategory() == MobCategory.MISC, "blud_trade 应为 MISC");
        assertTrue(helper, SonsOfSinsModEntities.WISTIVER_JAW.get().getCategory() == MobCategory.MISC, "wistiver_jaw 应为 MISC");
        assertTrue(helper, SonsOfSinsModEntities.DEVOURER_ORGANS.get().getCategory() == MobCategory.MISC, "devourer_organs 应为 MISC");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void recipeSpecialAndRegistered(GameTestHelper helper) {
        ResourceLocation id = ResourceLocation.tryBuild("sons_of_sins", "etheric_blood_imbibation");
        assertTrue(helper, ForgeRegistries.RECIPE_TYPES.containsKey(id), "1.20.1 应已注册 etheric_blood_imbibation RecipeType");

        ItemStack out = new ItemStack(SonsOfSinsModItems.CRYSTALLIZED_ETHER.get(), 2);
        EthericBloodImbibationRecipe recipe = new EthericBloodImbibationRecipe(
                id, out, NonNullList.withSize(1, Ingredient.EMPTY));
        assertTrue(helper, recipe.isSpecial(), "自定义 JEI 配方 isSpecial() 应返回 true");
        ItemStack assembled = recipe.assemble(new SimpleContainer(1), helper.getLevel().registryAccess());
        assembled.shrink(2);
        assertTrue(helper, recipe.getResultItem(helper.getLevel().registryAccess()).getCount() == 2, "assemble() 不应返回可变单例引用");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void urnBlockNoDupeAndItemTickSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos rel = new BlockPos(2, 2, 2);
        BlockPos abs = helper.absolutePos(rel);
        helper.setBlock(rel, SonsOfSinsModBlocks.URN_BLOCK.get().defaultBlockState());
        if (level.getBlockEntity(abs) instanceof UrnBlockBlockEntity urnBe) {
            urnBe.setItem(0, new ItemStack(SonsOfSinsModItems.ETHER_ASHES.get(), 16));
        } else {
            helper.fail("未创建 UrnBlockBlockEntity");
            return;
        }
        level.destroyBlock(abs, true);

        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(3.0));
        int looseAshes = 0;
        for (ItemEntity itemEntity : drops) {
            if (itemEntity.getItem().getItem() == SonsOfSinsModItems.ETHER_ASHES.get()) {
                looseAshes += itemEntity.getItem().getCount();
            }
        }
        assertTrue(helper, looseAshes == 0, "破坏骨灰坛不应同时散落裸灰烬导致刷物品，实际散落: " + looseAshes);

        // 测试便携骨灰坛剩 1 份灰烬时清空至 EMPTY（不会变 -1）
        Zombie dummy = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        dummy.getPersistentData().putBoolean("isEmptying", true);
        ItemStack urnStack = new ItemStack(SonsOfSinsModItems.URN.get());
        urnStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            if (handler instanceof IItemHandlerModifiable mod) {
                mod.setStackInSlot(0, new ItemStack(SonsOfSinsModItems.ETHER_ASHES.get(), 1));
            }
        });
        UrnItemInInventoryTickProcedure.execute(dummy, urnStack);
        AtomicInteger remainingCount = new AtomicInteger(-99);
        urnStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler ->
                remainingCount.set(handler.getStackInSlot(0).getCount()));
        assertTrue(helper, remainingCount.get() == 0, "消耗最后 1 份灰烬后槽位应归零而非负数，实际: " + remainingCount.get());
        assertTrue(helper, !dummy.getPersistentData().getBoolean("isEmptying"), "成功消耗灰烬后应重置 isEmptying");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void invincibleEntitiesRespectKillDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BludTradeEntity trade = helper.spawn(SonsOfSinsModEntities.BLUD_TRADE.get(), new BlockPos(1, 2, 1));
        BludAggressiveEntity agg = helper.spawn(SonsOfSinsModEntities.BLUD_AGGRESSIVE.get(), new BlockPos(2, 2, 1));
        WistiverJawEntity jaw = helper.spawn(SonsOfSinsModEntities.WISTIVER_JAW.get(), new BlockPos(3, 2, 1));

        assertTrue(helper, trade.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "BludTrade 应放行 /kill 伤害");
        assertTrue(helper, agg.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "BludAggressive 应放行 /kill 伤害");
        assertTrue(helper, jaw.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "WistiverJaw 应放行 /kill 伤害");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void projectilesAndEmptySingletonSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BloodPrEntity bloodPr = new BloodPrEntity(SonsOfSinsModEntities.BLOOD_PR.get(), level);
        NullPrEntity nullPr = new NullPrEntity(SonsOfSinsModEntities.NULL_PR.get(), level);
        assertTrue(helper, bloodPr.pickup != BloodPrEntity.Pickup.ALLOWED, "BloodPr 不应允许生存模式拾取刷弹");
        assertTrue(helper, nullPr.pickup != NullPrEntity.Pickup.ALLOWED, "NullPr 不应允许生存模式拾取刷弹");

        BetrayedGuzzlerEntity bg = helper.spawn(SonsOfSinsModEntities.BETRAYED_GUZZLER.get(), new BlockPos(2, 2, 2));
        bg.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
        assertTrue(helper, ItemStack.EMPTY.getCount() == 0, "ItemStack.EMPTY 全局单例数量不可被污染");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void tooltipsLocalizationGrabAndHelpersSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // 维度名称含连续下划线不应抛 StringIndexOutOfBoundsException
        assertTrue(helper, "Foo Bar".equals(SonsOfSinsHelper.safeFormatDimensionName("__foo__bar")), "维度名格式化异常");
        assertTrue(helper, "___".equals(SonsOfSinsHelper.safeFormatDimensionName("___")), "全下划线维度名应安全保留原值而不越界");

        // 短名器官 Tooltip 不越界
        ItemStack carcass = new ItemStack(SonsOfSinsModItems.FLESH_CARCASS.get());
        ItemStack shortOrgan = new ItemStack(SonsOfSinsModItems.HEART.get());
        shortOrgan.setHoverName(Component.literal("A"));
        carcass.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            if (handler instanceof IItemHandlerModifiable mod) {
                mod.setStackInSlot(3, shortOrgan.copy());
                mod.setStackInSlot(4, shortOrgan.copy());
                mod.setStackInSlot(5, shortOrgan.copy());
            }
        });
        assertTrue(helper, FleshCarcassSpecialInformationProcedure.execute(carcass) != null, "器官提示1不应抛异常");
        assertTrue(helper, FleshCarcassSpecialInformation2Procedure.execute(carcass) != null, "器官提示2不应抛异常");
        assertTrue(helper, FleshCarcassSpecialInformation3Procedure.execute(carcass) != null, "器官提示3不应抛异常");

        ItemStack puppet = new ItemStack(SonsOfSinsModItems.PUPPET_OF_STRIFE.get());
        puppet.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            if (handler instanceof IItemHandlerModifiable mod) {
                mod.setStackInSlot(0, shortOrgan.copy());
            }
        });
        assertTrue(helper, PuppetOfStrifeSpecialInformationProcedure.execute(puppet) != null, "纷争人偶提示不应抛异常");
        assertTrue(helper, UrnSpecialInformationProcedure.execute(new ItemStack(SonsOfSinsModItems.URN.get())) != null, "骨灰坛提示不应抛异常");

        // 本地化键核验
        List<Component> boneTips = new ArrayList<>();
        SonsOfSinsModItems.BLOODY_BONE.get().appendHoverText(new ItemStack(SonsOfSinsModItems.BLOODY_BONE.get()), level, boneTips, TooltipFlag.NORMAL);
        assertTrue(helper, boneTips.stream().anyMatch(c -> c.getContents() instanceof TranslatableContents tc
                && "item.sons_of_sins.bloody_bone.description_0".equals(tc.getKey())), "BloodyBone 应使用本地化键");

        List<Component> swordTips = new ArrayList<>();
        SonsOfSinsModItems.SINFUL_SWORD.get().appendHoverText(new ItemStack(SonsOfSinsModItems.SINFUL_SWORD.get()), level, swordTips, TooltipFlag.NORMAL);
        assertTrue(helper, swordTips.stream().anyMatch(c -> c.getContents() instanceof TranslatableContents tc
                && "item.sons_of_sins.sinful_sword.description_0".equals(tc.getKey())), "SinfulSword 应使用 sinful_sword.description_0");

        // ButcherTick、AcidBloodstream 与安全模式判定在服务端不抛异常
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        ButcherOnEntityTickUpdateProcedure.execute(level, z.getX(), z.getY(), z.getZ(), z);
        AcidBloodstreamOnEffectActiveTickProcedure.execute(z);
        assertTrue(helper, !SafeGameModeHelper.isGameMode(z, GameType.CREATIVE), "服务端 SafeGameModeHelper 不应触发客户端类加载");

        // 8 个原罪/幽灵生物战利品表存在
        String[] entities = {"blud", "butcher", "curse", "ether_ghost", "kelvin", "prowler", "walking_bed", "wistiver"};
        for (String name : entities) {
            ResourceLocation ltId = ResourceLocation.tryBuild("sons_of_sins", "entities/" + name);
            LootTable table = level.getServer().getLootData().getLootTable(ltId);
            assertTrue(helper, table != LootTable.EMPTY, "缺失生物战利品表: " + ltId);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void nibblerBucketingAndPlacementSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockSurvivalPlayer();

        // 1. 未驯服 Nibbler 主手水桶装桶，校验返回 CONSUME 且成功替换为鱼桶
        NibblerEntity wildNibbler = helper.spawn(SonsOfSinsModEntities.NIBBLER.get(), new BlockPos(1, 2, 1));
        wildNibbler.setNoAi(true);
        wildNibbler.setItemSlot(EquipmentSlot.FEET, new ItemStack(SonsOfSinsModItems.CREEPER_RIBS.get()));
        wildNibbler.setItemSlot(EquipmentSlot.LEGS, new ItemStack(SonsOfSinsModItems.HEART.get()));
        wildNibbler.setItemSlot(EquipmentSlot.CHEST, new ItemStack(SonsOfSinsModItems.RAVAGER_MUSCLE.get()));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

        InteractionResult mainResult = player.interactOn(wildNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, mainResult == InteractionResult.CONSUME, "主手水桶装 Nibbler 应返回 CONSUME，实际: " + mainResult);
        assertTrue(helper, wildNibbler.isRemoved(), "装桶后 Nibbler 应已被移除");
        assertTrue(helper, player.getMainHandItem().is(SonsOfSinsModItems.NIBBLER_BUCKET.get()), "主手应变为 Nibbler 水桶");
        ItemStack filledBucket = player.getMainHandItem().copy();

        // 2. 已驯服 Nibbler 副手水桶装桶 + 对已移除实体再次交互不得重复刷桶
        NibblerEntity tamedNibbler = new NibblerEntity(SonsOfSinsModEntities.NIBBLER.get(), level) {
            @Override
            public LivingEntity getOwner() {
                return player.getUUID().equals(this.getOwnerUUID()) ? player : super.getOwner();
            }
        };
        BlockPos tamedPos = helper.absolutePos(new BlockPos(2, 2, 1));
        tamedNibbler.moveTo(tamedPos.getX() + 0.5, tamedPos.getY(), tamedPos.getZ() + 0.5);
        tamedNibbler.setNoAi(true);
        tamedNibbler.tame(player);
        level.addFreshEntity(tamedNibbler);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WATER_BUCKET));

        InteractionResult offResult = player.interactOn(tamedNibbler, InteractionHand.OFF_HAND);
        assertTrue(helper, offResult == InteractionResult.CONSUME, "副手水桶装 Nibbler 应返回 CONSUME，实际: " + offResult);
        assertTrue(helper, tamedNibbler.isRemoved(), "副手装桶后 Nibbler 应已被移除");
        assertTrue(helper, player.getOffhandItem().is(SonsOfSinsModItems.NIBBLER_BUCKET.get()), "副手应变为 Nibbler 水桶");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.interactOn(tamedNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, player.getMainHandItem().is(Items.WATER_BUCKET), "对已移除 Nibbler 二次交互不应重复刷出鱼桶");

        // 2b. 其他玩家驯服的 Nibbler 不得被当前玩家装走
        Player otherPlayer = helper.makeMockSurvivalPlayer();
        NibblerEntity othersNibbler = helper.spawn(SonsOfSinsModEntities.NIBBLER.get(), new BlockPos(2, 2, 3));
        othersNibbler.setNoAi(true);
        othersNibbler.tame(otherPlayer);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        player.interactOn(othersNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, !othersNibbler.isRemoved() && player.getMainHandItem().is(Items.WATER_BUCKET), "他人驯服的 Nibbler 不应被装桶");

        // 3. 装备炽足兽肌肉并拥有抗火效果的 Nibbler：水桶不触发，岩浆桶可正常装桶并返回 CONSUME
        NibblerEntity lavaNibbler = helper.spawn(SonsOfSinsModEntities.NIBBLER.get(), new BlockPos(3, 2, 1));
        lavaNibbler.setNoAi(true);
        lavaNibbler.setItemSlot(EquipmentSlot.CHEST, new ItemStack(SonsOfSinsModItems.STRIDER_MUSCLE.get()));
        lavaNibbler.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        player.interactOn(lavaNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, !lavaNibbler.isRemoved() && player.getMainHandItem().is(Items.WATER_BUCKET), "抗火 Nibbler 不应被水桶装起");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        InteractionResult lavaResult = player.interactOn(lavaNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, lavaResult == InteractionResult.CONSUME, "岩浆桶装抗火 Nibbler 应返回 CONSUME，实际: " + lavaResult);
        assertTrue(helper, lavaNibbler.isRemoved(), "岩浆桶装桶后 Nibbler 应已被移除");
        assertTrue(helper, player.getMainHandItem().is(SonsOfSinsModItems.NIBBLER_LAVA_BUCKET.get()), "主手应变为 Nibbler 岩浆桶");

        // 4. 空桶不得装桶；濒死 Nibbler 不得被水桶装起
        NibblerEntity dyingNibbler = helper.spawn(SonsOfSinsModEntities.NIBBLER.get(), new BlockPos(1, 2, 3));
        dyingNibbler.setNoAi(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        player.interactOn(dyingNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, !dyingNibbler.isRemoved() && player.getMainHandItem().is(Items.BUCKET), "空桶不应装起 Nibbler");

        dyingNibbler.setHealth(0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        player.interactOn(dyingNibbler, InteractionHand.MAIN_HAND);
        assertTrue(helper, player.getMainHandItem().is(Items.WATER_BUCKET), "濒死 Nibbler 不应被水桶装起");

        // 5. 放桶恢复实体与空 NBT 桶不崩溃
        BlockPos floorRel = new BlockPos(2, 1, 2);
        BlockPos floorAbs = helper.absolutePos(floorRel);
        helper.setBlock(floorRel, Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, filledBucket);
        NibblerBucketRightclickedOnBlockProcedure.execute(
                level, floorAbs.getX(), floorAbs.getY(), floorAbs.getZ(), Direction.UP, player, filledBucket);
        assertTrue(helper, player.getMainHandItem().is(Items.BUCKET), "放桶后生存玩家主手应恢复为空桶");
        List<NibblerEntity> released = level.getEntitiesOfClass(NibblerEntity.class, new AABB(floorAbs.above()).inflate(2.0));
        assertTrue(helper, !released.isEmpty(), "放桶后应在目标位置生成 Nibbler");

        ItemStack emptyNbtBucket = new ItemStack(SonsOfSinsModItems.NIBBLER_BUCKET.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, emptyNbtBucket);
        NibblerBucketRightclickedOnBlockProcedure.execute(
                level, floorAbs.getX(), floorAbs.getY(), floorAbs.getZ(), Direction.UP, player, emptyNbtBucket);
        assertTrue(helper, player.getMainHandItem().is(Items.BUCKET), "空 NBT 的 Nibbler 水桶放桶不应崩溃");
        helper.succeed();
    }
}
