package com.hp.sonsofsinsbugfix.mixin;

import net.mcreator.sonsofsins.item.RemnantItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 补回 1.21.1 RemnantItem.Chestplate 缺失的 getArmorTexture，确保 3D 胸甲模型使用正确的 remnant_chest.png 贴图。
 */
@Mixin(RemnantItem.Chestplate.class)
public abstract class RemnantChestplateTexture1211Mixin extends ArmorItem {

    @Unique
    private static final ResourceLocation SONSOFSINSBUGFIX$REMNANT_CHEST_TEX =
            ResourceLocation.fromNamespaceAndPath("sons_of_sins", "textures/entities/remnant_chest.png");

    private RemnantChestplateTexture1211Mixin() {
        super(null, null, null);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return SONSOFSINSBUGFIX$REMNANT_CHEST_TEX;
    }
}
