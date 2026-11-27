package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.SinMonster;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 MCR-14④（Forge 1.20.1）：SinMonster 全部 7 个 SynchedEntityData.defineId 误传子类 WistiverEntity.class，
 * 修正为 SinMonster.class，消除 ID 16 碰撞崩溃及父类/子类 <clinit> 类加载死锁风险。
 */
@Mixin(SinMonster.class)
public abstract class SinMonsterDataIdMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;defineId(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)Lnet/minecraft/network/syncher/EntityDataAccessor;"
            )
    )
    private static <T> EntityDataAccessor<T> sonsofsinsbugfix$fixSinMonsterDefineId(
            Class<? extends net.minecraft.world.entity.Entity> clazz, EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(SinMonster.class, serializer);
    }
}
