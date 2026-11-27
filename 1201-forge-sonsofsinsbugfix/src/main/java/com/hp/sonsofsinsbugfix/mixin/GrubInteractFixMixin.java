package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.entity.GrubEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 GrubEntity#mobInteract 无条件强行骑乘的缺陷：仅允许已驯服主人非潜行且非喂食时骑乘。
 */
@Mixin(GrubEntity.class)
public abstract class GrubInteractFixMixin extends TamableAnimal {
    protected GrubInteractFixMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Redirect(
            method = "mobInteract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startRiding(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean sonsofsinsbugfix$conditionalRiding(Player player, Entity target, Player sourceentity, InteractionHand hand) {
        if (!this.isTame() || !this.isOwnedBy(player) || player.isShiftKeyDown()) {
            return false;
        }
        if (this.isFood(player.getItemInHand(hand))) {
            return false;
        }
        return player.startRiding(target);
    }
}
