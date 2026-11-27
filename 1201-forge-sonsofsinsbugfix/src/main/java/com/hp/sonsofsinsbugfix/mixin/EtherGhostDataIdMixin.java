package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.EtherGhostEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 MCR-14④（Forge 1.20.1）：EtherGhostEntity 前 7 个 SynchedEntityData.defineId 误传 WistiverEntity.class、
 * 后 2 个传自身，导致 ID 16 重复碰撞崩溃，统一修正为 EtherGhostEntity.class。
 */
@Mixin(EtherGhostEntity.class)
public abstract class EtherGhostDataIdMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;defineId(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)Lnet/minecraft/network/syncher/EntityDataAccessor;"
            )
    )
    private static <T> EntityDataAccessor<T> sonsofsinsbugfix$fixEtherGhostDefineId(
            Class<? extends net.minecraft.world.entity.Entity> clazz, EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(EtherGhostEntity.class, serializer);
    }
}
