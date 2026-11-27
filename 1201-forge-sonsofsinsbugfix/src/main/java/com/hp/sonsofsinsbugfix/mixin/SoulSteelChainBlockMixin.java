package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.block.SoulSteelChainBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 MCR-08（Forge 1.20.1）：移除 SoulSteelChainBlock#canHarvestBlock 对 instanceof PickaxeItem 的硬编码，
 * 恢复原版与 Forge 标准挖掘标签判定（mineable/pickaxe + needs_stone_tool），自然兼容匠魂/Tetra 等所有标准模块化工具。
 */
@Mixin(SoulSteelChainBlock.class)
public abstract class SoulSteelChainBlockMixin extends Block {
    protected SoulSteelChainBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "canHarvestBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void sonsofsinsbugfix$useStandardHarvestCheck(
            BlockState state, BlockGetter world, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.canHarvestBlock(state, world, pos, player));
    }
}
