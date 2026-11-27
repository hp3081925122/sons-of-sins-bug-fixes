package com.hp.sonsofsinsbugfix.mixin;

import java.util.function.Supplier;
import net.mcreator.sonsofsins.network.SonsOfSinsModVariables;
import net.mcreator.sonsofsins.world.inventory.FlessCarcassGUIMenu;
import net.mcreator.sonsofsins.world.inventory.PuppetOfStrifeGUIMenu;
import net.mcreator.sonsofsins.world.inventory.UrnBlockGUIMenu;
import net.mcreator.sonsofsins.world.inventory.UrnGUIMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-13 与 currentOpenItem 泄漏（NeoForge 1.21.1）。
 */
@Mixin({
        FlessCarcassGUIMenu.class,
        PuppetOfStrifeGUIMenu.class,
        UrnGUIMenu.class,
        UrnBlockGUIMenu.class
})
public abstract class GuiMenuStillValidMixin extends AbstractContainerMenu {
    @Shadow(remap = false)
    private boolean bound;

    @Shadow(remap = false)
    private BlockEntity boundBlockEntity;

    @Shadow(remap = false)
    private Entity boundEntity;

    @Shadow(remap = false)
    private Supplier<Boolean> boundItemMatcher;

    protected GuiMenuStillValidMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeStillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null || !player.isAlive() || player.isRemoved()) {
            cir.setReturnValue(false);
            return;
        }
        if (!this.bound) {
            cir.setReturnValue(false);
            return;
        }
        if (this.boundBlockEntity != null) {
            boolean valid = !this.boundBlockEntity.isRemoved()
                    && this.boundBlockEntity.getLevel() == player.level()
                    && player.distanceToSqr(
                            this.boundBlockEntity.getBlockPos().getX() + 0.5,
                            this.boundBlockEntity.getBlockPos().getY() + 0.5,
                            this.boundBlockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
            cir.setReturnValue(valid);
            return;
        }
        if (this.boundEntity != null) {
            cir.setReturnValue(this.boundEntity.isAlive() && player.distanceToSqr(this.boundEntity) <= 64.0);
            return;
        }
        if (this.boundItemMatcher != null) {
            cir.setReturnValue(Boolean.TRUE.equals(this.boundItemMatcher.get()));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player != null && !player.level().isClientSide()) {
            SonsOfSinsModVariables.PlayerVariables vars = player.getData(SonsOfSinsModVariables.PLAYER_VARIABLES);
            if (vars != null && !vars.currentOpenItem.isEmpty()) {
                vars.currentOpenItem = ItemStack.EMPTY;
                vars.syncPlayerVariables(player);
            }
        }
    }
}
