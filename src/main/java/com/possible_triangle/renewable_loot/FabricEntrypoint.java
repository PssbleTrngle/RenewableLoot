package com.possible_triangle.renewable_loot;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {

    }

    public static boolean isLootContainer(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof BrushableBlockEntity) return true;
        if (!(blockEntity instanceof RandomizableContainer)) return false;
        if (blockEntity instanceof DecoratedPotBlockEntity && !Config.INSTANCE.disableBreakingPots()) return false;

        var level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel)) return false;

        var nbt = blockEntity.saveWithoutMetadata(level.getServer().registryAccess());

        if (nbt.contains("LootTable")) return true;
        if (nbt.contains(Constants.SAVED_TABLE_TAG)) return true;

        return false;
    }

}
