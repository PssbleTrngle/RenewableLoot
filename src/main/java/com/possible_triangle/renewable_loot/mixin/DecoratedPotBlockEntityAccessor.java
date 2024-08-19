package com.possible_triangle.renewable_loot.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DecoratedPotBlockEntity.class)
public interface DecoratedPotBlockEntityAccessor {

    @Accessor
    @Nullable
    ResourceKey<LootTable> getLootTable();

}
