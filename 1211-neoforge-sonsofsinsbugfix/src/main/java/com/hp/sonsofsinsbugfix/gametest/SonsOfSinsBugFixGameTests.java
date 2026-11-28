package com.hp.sonsofsinsbugfix.gametest;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import com.hp.sonsofsinsbugfix.util.SafeGameModeHelper;
import com.hp.sonsofsinsbugfix.util.SonsOfSinsHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.mcreator.sonsofsins.block.entity.UrnBlockBlockEntity;
import net.mcreator.sonsofsins.entity.BetrayedGuzzlerEntity;
import net.mcreator.sonsofsins.entity.BloodPrEntity;
import net.mcreator.sonsofsins.entity.BludAggressiveEntity;
import net.mcreator.sonsofsins.entity.BludTradeEntity;
import net.mcreator.sonsofsins.entity.DevourerEntity;
import net.mcreator.sonsofsins.entity.NibblerEntity;
import net.mcreator.sonsofsins.entity.NullPrEntity;
import net.mcreator.sonsofsins.entity.WalkingBedEntity;
import net.mcreator.sonsofsins.entity.WistiverJawEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModBlocks;
import net.mcreator.sonsofsins.init.SonsOfSinsModEntities;
import net.mcreator.sonsofsins.init.SonsOfSinsModItems;
import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipe;
import net.mcreator.sonsofsins.procedures.AcidBloodstreamOnEffectActiveTickProcedure;
import net.mcreator.sonsofsins.procedures.ButcherOnEntityTickUpdateProcedure;
import net.mcreator.sonsofsins.procedures.DevourerEntityDiesProcedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation2Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformation3Procedure;
import net.mcreator.sonsofsins.procedures.FleshCarcassSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.NibblerBucketRightclickedOnBlockProcedure;
import net.mcreator.sonsofsins.procedures.PuppetOfStrifeSpecialInformationProcedure;
import net.mcreator.sonsofsins.procedures.UrnItemInInventoryTickProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Sons of Sins Bug Fixes（NeoForge 1.21.1）服务端 GameTest 回归验收套件。
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
            assertTrue(helper, zeroRan.get(), "tick<=0 队列任务应安全执行而不泄漏");
            assertTrue(helper, afterThrowRan.get(), "单任务异常不应中断后续延迟任务执行");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void mapColorsTagsAndEnchantability(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int checked = 0;
        for (DeferredHolder<Block, ? extends Block> reg : SonsOfSinsModBlocks.REGISTRY.getEntries()) {
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

        // 验证 1.21.1 血肉工具与变形工具可正常附魔，血肉锄头使用 FLESH_OF_DEMISE 修复
        Holder<Enchantment> eff = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        Holder<Enchantment> sharp = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
        ItemStack shiftTool = new ItemStack(SonsOfSinsModItems.SHAPE_SHIFTING_TOOL.get());
        ItemStack fleshPick = new ItemStack(SonsOfSinsModItems.FLESH_PICKAXE.get());
        ItemStack fleshSword = new ItemStack(SonsOfSinsModItems.FLESH_SWORD.get());
        ItemStack fleshHoe = new ItemStack(SonsOfSinsModItems.FLESH_HOE.get());

        assertTrue(helper, shiftTool.supportsEnchantment(eff), "变形工具应支持效率附魔");
        assertTrue(helper, fleshPick.supportsEnchantment(eff), "血肉镐应支持效率附魔");
        assertTrue(helper, fleshSword.supportsEnchantment(sharp), "血肉剑应支持锋利附魔");
        assertTrue(helper, SonsOfSinsModItems.FLESH_HOE.get().isValidRepairItem(fleshHoe, new ItemStack(SonsOfSinsModItems.FLESH_OF_DEMISE.get())),
                "1.21.1 血肉锄应恢复为使用 FLESH_OF_DEMISE 修复");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void entityTrackingCategoriesAndStructureClean(GameTestHelper helper) {
        int count = 0;
        for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> reg : SonsOfSinsModEntities.REGISTRY.getEntries()) {
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

        // 校验清洗后的 1.21.1 sinful_church.nbt 可正常加载
        ResourceLocation churchId = ResourceLocation.fromNamespaceAndPath("sons_of_sins", "sinful_church");
        assertTrue(helper, helper.getLevel().getStructureManager().get(churchId).isPresent(), "sinful_church 结构应正常加载");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void recipeSpecialAndUrnNoDupe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack out = new ItemStack(SonsOfSinsModItems.CRYSTALLIZED_ETHER.get(), 2);
        EthericBloodImbibationRecipe recipe = new EthericBloodImbibationRecipe(
                out, NonNullList.withSize(1, Ingredient.EMPTY));
        assertTrue(helper, recipe.isSpecial(), "自定义 JEI 配方 isSpecial() 应返回 true");
        ItemStack assembled = recipe.assemble(new SingleRecipeInput(ItemStack.EMPTY), level.registryAccess());
        assembled.shrink(2);
        assertTrue(helper, recipe.getResultItem(level.registryAccess()).getCount() == 2, "assemble() 不应返回可变单例引用");

        // 骨灰坛破坏不双倍刷灰烬
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
            if (itemEntity.getItem().is(SonsOfSinsModItems.ETHER_ASHES.get())) {
                looseAshes += itemEntity.getItem().getCount();
            }
        }
        assertTrue(helper, looseAshes == 0, "破坏骨灰坛不应同时散落裸灰烬导致刷物品，实际散落: " + looseAshes);

        // 便携骨灰坛剩 1 份灰烬安全清空
        Zombie dummy = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        dummy.getPersistentData().putBoolean("isEmptying", true);
        ItemStack urnStack = new ItemStack(SonsOfSinsModItems.URN.get());
        IItemHandler handler = urnStack.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (handler instanceof IItemHandlerModifiable mod) {
            mod.setStackInSlot(0, new ItemStack(SonsOfSinsModItems.ETHER_ASHES.get(), 1));
        }
        UrnItemInInventoryTickProcedure.execute(dummy, urnStack);
        IItemHandler after = urnStack.getCapability(Capabilities.ItemHandler.ITEM, null);
        assertTrue(helper, after != null && after.getStackInSlot(0).isEmpty(), "消耗最后 1 份灰烬后槽位应归空");
        assertTrue(helper, !dummy.getPersistentData().getBoolean("isEmptying"), "成功消耗灰烬后应重置 isEmptying");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void armorPreservedAndBabyNoCarcassDupe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // 校验 1.21.1 护甲减伤不被 InstinctHurt/DamagesHurt 的 LivingDamageEvent.Pre getOriginalDamage() 冲掉
        Zombie armored = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        armored.setNoAi(true);
        armored.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        armored.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        armored.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        armored.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        armored.addEffect(new MobEffectInstance(SonsOfSinsModMobEffects.INSTINCT, 200, 0));
        Zombie attacker = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
        attacker.setNoAi(true);

        // 等待 2 tick 让 detectEquipmentUpdates() 应用装备护甲属性
        helper.runAfterDelay(2, () -> {
            assertTrue(helper, armored.getArmorValue() >= 20, "下界合金套装备属性应已生效，当前护甲值: " + armored.getArmorValue());
            float beforeHp = armored.getHealth();
            armored.hurt(level.damageSources().mobAttack(attacker), 10.0F);
            float taken = beforeHp - armored.getHealth();
            // 未修复前 InstinctHurt 会按 getOriginalDamage()*0.7=7.0 直接穿甲扣血；修复后应在护甲减伤后再乘 0.7（约 2.0）
            assertTrue(helper, taken > 0.0F && taken < 4.5F, "全套下界合金护甲+本能受 10 点物理伤应保留护甲减伤，实际扣血: " + taken);

            // 校验 1.21.1 细胞分裂幼体死亡不会掉落血肉躯壳刷器官
            DevourerEntity baby = helper.spawn(SonsOfSinsModEntities.DEVOURER.get(), new BlockPos(2, 2, 2));
            baby.setBaby(true);
            baby.setItemSlot(EquipmentSlot.CHEST, new ItemStack(SonsOfSinsModItems.RAVAGER_MUSCLE.get()));
            baby.setItemSlot(EquipmentSlot.LEGS, new ItemStack(SonsOfSinsModItems.BLAZING_HEART.get()));
            baby.setItemSlot(EquipmentSlot.FEET, new ItemStack(SonsOfSinsModItems.CREEPER_RIBS.get()));
            DevourerEntityDiesProcedure.execute(level, baby);
            List<ItemEntity> carcassDrops = level.getEntitiesOfClass(ItemEntity.class, baby.getBoundingBox().inflate(3.0),
                    ie -> ie.getItem().is(SonsOfSinsModItems.FLESH_CARCASS.get()));
            assertTrue(helper, carcassDrops.isEmpty(), "细胞分裂幼体死亡不应掉落血肉躯壳刷器官");

            // 校验行走之床一阶段站立 -> 二阶段半血伏地碰撞箱高度正确缩减（原版 1.21.1 先 refreshDimensions 再设 DATA_grave_cooldown=2 且删了 baseTick 刷新，导致碰撞箱卡死在 2.8）
            WalkingBedEntity bed = helper.spawn(SonsOfSinsModEntities.WALKING_BED.get(), new BlockPos(3, 2, 1));
            bed.getEntityData().set(WalkingBedEntity.DATA_scream_animtime, 0);
            bed.baseTick();
            float standingHeight = bed.getBbHeight();
            bed.setHealth(bed.getMaxHealth() * 0.4F);
            bed.getEntityData().set(WalkingBedEntity.DATA_scream_animtime, 0);
            bed.baseTick();
            float crouchedHeight = bed.getBbHeight();
            assertTrue(helper, crouchedHeight < standingHeight * 0.6F, "WalkingBed 半血转二阶段伏地后碰撞箱高度应缩减: " + standingHeight + " -> " + crouchedHeight);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void invincibleKillProjectilesTooltipsAndLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BludTradeEntity trade = helper.spawn(SonsOfSinsModEntities.BLUD_TRADE.get(), new BlockPos(1, 2, 1));
        BludAggressiveEntity agg = helper.spawn(SonsOfSinsModEntities.BLUD_AGGRESSIVE.get(), new BlockPos(2, 2, 1));
        WistiverJawEntity jaw = helper.spawn(SonsOfSinsModEntities.WISTIVER_JAW.get(), new BlockPos(3, 2, 1));
        assertTrue(helper, trade.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "BludTrade 应放行 /kill 伤害");
        assertTrue(helper, agg.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "BludAggressive 应放行 /kill 伤害");
        assertTrue(helper, jaw.hurt(level.damageSources().genericKill(), Float.MAX_VALUE), "WistiverJaw 应放行 /kill 伤害");

        BloodPrEntity bloodPr = new BloodPrEntity(SonsOfSinsModEntities.BLOOD_PR.get(), level);
        NullPrEntity nullPr = new NullPrEntity(SonsOfSinsModEntities.NULL_PR.get(), level);
        assertTrue(helper, bloodPr.pickup != BloodPrEntity.Pickup.ALLOWED, "BloodPr 不应允许生存模式拾取刷弹");
        assertTrue(helper, nullPr.pickup != NullPrEntity.Pickup.ALLOWED, "NullPr 不应允许生存模式拾取刷弹");

        BetrayedGuzzlerEntity bg = helper.spawn(SonsOfSinsModEntities.BETRAYED_GUZZLER.get(), new BlockPos(2, 2, 2));
        bg.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
        assertTrue(helper, ItemStack.EMPTY.getCount() == 0, "ItemStack.EMPTY 全局单例数量不可被污染");

        // 维度名、短名器官提示、GrabExecution、AcidBloodstream 与 SafeGameModeHelper
        assertTrue(helper, "Foo Bar".equals(SonsOfSinsHelper.safeFormatDimensionName("__foo__bar")), "维度名格式化异常");
        ItemStack carcass = new ItemStack(SonsOfSinsModItems.FLESH_CARCASS.get());
        ItemStack shortOrgan = new ItemStack(SonsOfSinsModItems.HEART.get());
        shortOrgan.set(DataComponents.CUSTOM_NAME, Component.literal("A"));
        IItemHandler cHandler = carcass.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (cHandler instanceof IItemHandlerModifiable mod) {
            mod.setStackInSlot(3, shortOrgan.copy());
            mod.setStackInSlot(4, shortOrgan.copy());
            mod.setStackInSlot(5, shortOrgan.copy());
        }
        assertTrue(helper, FleshCarcassSpecialInformationProcedure.execute(carcass) != null, "器官提示1不应抛异常");
        assertTrue(helper, FleshCarcassSpecialInformation2Procedure.execute(carcass) != null, "器官提示2不应抛异常");
        assertTrue(helper, FleshCarcassSpecialInformation3Procedure.execute(carcass) != null, "器官提示3不应抛异常");

        ItemStack puppet = new ItemStack(SonsOfSinsModItems.PUPPET_OF_STRIFE.get());
        IItemHandler pHandler = puppet.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (pHandler instanceof IItemHandlerModifiable mod) {
            mod.setStackInSlot(0, shortOrgan.copy());
        }
        assertTrue(helper, PuppetOfStrifeSpecialInformationProcedure.execute(puppet) != null, "纷争人偶提示不应抛异常");

        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        ButcherOnEntityTickUpdateProcedure.execute(level, z.getX(), z.getY(), z.getZ(), z);
        AcidBloodstreamOnEffectActiveTickProcedure.execute(z);
        assertTrue(helper, !SafeGameModeHelper.isGameMode(z, GameType.CREATIVE), "服务端 SafeGameModeHelper 不应触发客户端类加载");

        // 8 个实体表 + 1 个教堂箱子表存在
        String[] tables = {"entities/blud", "entities/butcher", "entities/curse", "entities/ether_ghost",
                "entities/kelvin", "entities/prowler", "entities/walking_bed", "entities/wistiver", "chests/sinful_church"};
        for (String name : tables) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("sons_of_sins", name));
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            assertTrue(helper, table != LootTable.EMPTY, "缺失战利品表: " + key.location());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void nibblerBucketingAndPlacementSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

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
        Player otherPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
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

        // 5. 放桶恢复实体与空自定义数据桶不崩溃
        BlockPos floorRel = new BlockPos(2, 1, 2);
        BlockPos floorAbs = helper.absolutePos(floorRel);
        helper.setBlock(floorRel, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, filledBucket);
        NibblerBucketRightclickedOnBlockProcedure.execute(
                level, floorAbs.getX(), floorAbs.getY(), floorAbs.getZ(), Direction.UP, player, filledBucket);
        assertTrue(helper, player.getMainHandItem().is(Items.BUCKET), "放桶后生存玩家主手应恢复为空桶");
        List<NibblerEntity> released = level.getEntitiesOfClass(NibblerEntity.class, new AABB(floorAbs.above()).inflate(2.0));
        assertTrue(helper, !released.isEmpty(), "放桶后应在目标位置生成 Nibbler");

        ItemStack emptyDataBucket = new ItemStack(SonsOfSinsModItems.NIBBLER_BUCKET.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, emptyDataBucket);
        NibblerBucketRightclickedOnBlockProcedure.execute(
                level, floorAbs.getX(), floorAbs.getY(), floorAbs.getZ(), Direction.UP, player, emptyDataBucket);
        assertTrue(helper, player.getMainHandItem().is(Items.BUCKET), "空数据的 Nibbler 水桶放桶不应崩溃");
        helper.succeed();
    }
}
