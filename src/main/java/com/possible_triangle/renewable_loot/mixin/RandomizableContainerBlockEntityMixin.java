package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.RegenerativeContainer;
import com.possible_triangle.renewable_loot.RegenerativeLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RandomizableContainerBlockEntity.class)
public class RandomizableContainerBlockEntityMixin implements RegenerativeContainer {

    @Unique
    private final RegenerativeLoot regenerative_loot$data = new RegenerativeLoot();

    @Override
    public RegenerativeLoot regenerative_loot$getData() {
        return regenerative_loot$data;
    }

    @Override
    public @Nullable ResourceKey<LootTable> regenerative_loot$getLootTable() {
        var accessor = (RandomizableContainerBlockEntityAccessor) this;
        return accessor.getLootTable();
    }

}
