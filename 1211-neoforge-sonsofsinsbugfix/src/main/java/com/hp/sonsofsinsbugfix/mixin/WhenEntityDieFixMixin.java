package com.hp.sonsofsinsbugfix.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mcreator.sonsofsins.entity.WistiverEntity;
import net.mcreator.sonsofsins.init.SonsOfSinsModBlocks;
import net.mcreator.sonsofsins.init.SonsOfSinsModMobEffects;
import net.mcreator.sonsofsins.procedures.WhenEntityDieProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 WhenEntityDieProcedure：
 * 1. 血池救鸡真正取消 LivingDeathEvent；
 * 2. 允许环境伤害死亡触发 LAST_BREATH 引爆；
 * 3. 修复 slicing/* 战利品表使用正确 LootContextParamSets.ENTITY 与完整掉落；
 * 4. 修复 Wistiver 击杀回血上限封顶。
 */
@Mixin(value = WhenEntityDieProcedure.class, remap = false)
public abstract class WhenEntityDieFixMixin {
    private static final String EXEC = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V";

    @Inject(method = "onEntityDeath", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$handleChickenRescueAndEnvLastBreath(LivingDeathEvent event, CallbackInfo ci) {
        if (event == null || event.getEntity() == null) {
            return;
        }
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        DamageSource source = event.getSource();
        if (victim instanceof Chicken && (source == null || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))) {
            BlockPos pos = BlockPos.containing(Math.floor(victim.getX()), Math.floor(victim.getY()), Math.floor(victim.getZ()));
            if (level.getFluidState(pos).createLegacyBlock().getBlock() == SonsOfSinsModBlocks.BLOOD.get()) {
                victim.setHealth(victim.getMaxHealth());
                event.setCanceled(true);
                ci.cancel();
                return;
            }
        }
        if ((source == null || source.getEntity() == null) && victim.hasEffect(SonsOfSinsModMobEffects.LAST_BREATH)) {
            if (!level.isClientSide()) {
                int amp = victim.getEffect(SonsOfSinsModMobEffects.LAST_BREATH).getAmplifier();
                level.explode(null, victim.getX(), victim.getY(), victim.getZ(), 4 + amp, Level.ExplosionInteraction.MOB);
            }
            victim.removeEffect(SonsOfSinsModMobEffects.LAST_BREATH);
        }
    }

    @Redirect(
            method = EXEC,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
            )
    )
    private static ObjectArrayList<ItemStack> sonsofsinsbugfix$properEntityLootAndSpawnAll(
            LootTable table, LootParams ignoredBrokenParams,
            Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
        if (!(world instanceof ServerLevel serverLevel) || entity == null) {
            return new ObjectArrayList<>();
        }
        DamageSource dmgSource = event instanceof LivingDeathEvent lde && lde.getSource() != null
                ? lde.getSource()
                : serverLevel.damageSources().generic();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, dmgSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, sourceentity)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, dmgSource.getDirectEntity());
        if (sourceentity instanceof Player player) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck());
        }
        ObjectArrayList<ItemStack> drops = table.getRandomItems(builder.create(LootContextParamSets.ENTITY));
        if (drops.size() <= 1) {
            return drops;
        }
        for (int i = 0; i < drops.size() - 1; i++) {
            ItemStack drop = drops.get(i);
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), drop.copy());
                itemEntity.setPickUpDelay(10);
                serverLevel.addFreshEntity(itemEntity);
            }
        }
        ObjectArrayList<ItemStack> tail = new ObjectArrayList<>(1);
        tail.add(drops.get(drops.size() - 1));
        return tail;
    }

    @Inject(method = EXEC, at = @At("TAIL"))
    private static void sonsofsinsbugfix$capWistiverKillHeal(
            Event event, LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (sourceentity instanceof WistiverEntity wistiver && wistiver.isAlive()
                && entity instanceof LivingEntity victim) {
            float heal = victim.getMaxHealth() * 0.5F;
            if (wistiver.getHealth() < wistiver.getMaxHealth() && wistiver.getHealth() + heal >= wistiver.getMaxHealth()) {
                wistiver.setHealth(wistiver.getMaxHealth());
            }
        }
    }
}
