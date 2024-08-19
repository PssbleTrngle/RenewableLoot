package com.possible_triangle.renewable_loot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface RegenerativeContainer {

    default RegenerativeLoot regenerative_loot$getData() {
        return new RegenerativeLoot();
    }

    @Nullable
    default ResourceKey<LootTable> regenerative_loot$getLootTable() {
        return null;
    }

}
