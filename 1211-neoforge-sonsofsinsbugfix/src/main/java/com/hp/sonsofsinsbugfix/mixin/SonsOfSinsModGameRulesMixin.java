package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.init.SonsOfSinsModGameRules;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-02（NeoForge 1.21.1）：在并行的 FMLCommonSetupEvent 中改用 enqueueWork 串行注册 GameRules，避免 TreeMap 并发崩溃。
 */
@Mixin(value = SonsOfSinsModGameRules.class, remap = false)
public abstract class SonsOfSinsModGameRulesMixin {
    @Shadow
    public static GameRules.Key<GameRules.IntegerValue> JAWS_DAMAGE_LIMIT;

    @Inject(method = "registerGameRules", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$enqueueGameRules(FMLCommonSetupEvent event, CallbackInfo ci) {
        ci.cancel();
        event.enqueueWork(() -> JAWS_DAMAGE_LIMIT = GameRules.register(
                "jawsDamageLimit", GameRules.Category.PLAYER, GameRules.IntegerValue.create(666)));
    }
}
