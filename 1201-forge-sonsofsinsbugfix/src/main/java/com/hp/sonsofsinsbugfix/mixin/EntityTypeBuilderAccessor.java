package com.hp.sonsofsinsbugfix.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问并修正 EntityType.Builder 的实体分类字段（用于将器官/非敌对与可驯服血肉造物从 MONSTER 修正为合理分类）。
 */
@Mixin(EntityType.Builder.class)
public interface EntityTypeBuilderAccessor {
    @Mutable
    @Accessor("category")
    void sonsofsinsbugfix$setCategory(MobCategory category);
}
