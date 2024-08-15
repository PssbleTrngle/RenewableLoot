package com.possible_triangle.renewable_loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public final class RegenerativeLoot {

    @Nullable
    private ResourceKey<LootTable> savedLootTable;

    private Long lastGeneration = -1L;

    private int generatedCount = 0;

    @Nullable
    private UUID lastPlayer;

    private final List<UUID> rewardedPlayers = new ArrayList<>();

    private RegenType regenType = RegenType.ONCE_PER_PLAYER;

    public RegenType getRegenType() {
        return regenType;
    }

    public void saveTimestamp(@Nullable Player player, @Nullable ResourceKey<LootTable> lootTable) {
        generatedCount++;
        lastGeneration = System.currentTimeMillis();
        if (player != null) {
            lastPlayer = player.getUUID();
            rewardedPlayers.add(lastPlayer);
        }

        if (lootTable != null) {
            savedLootTable = lootTable;
        }
    }

    public boolean canRegenrate(@Nullable Player player) {
        if (savedLootTable == null) return false;
        if (lastGeneration < 0) return false;
        if (regenType == RegenType.DISABLED) return false;
        if (player == null && regenType != RegenType.ALWAYS) return false;
        if (regenType == RegenType.DIFFERENT_PLAYER && player.getUUID().equals(lastPlayer)) return false;
        if (regenType == RegenType.ONCE_PER_PLAYER && rewardedPlayers.contains(player.getUUID())) return false;

        var now = System.currentTimeMillis();
        return now - lastGeneration >= Config.INSTANCE.getTimeout();
    }

    public void onLooted(@Nullable Player player, Consumer<ResourceKey<LootTable>> setLootTable) {
        if (canRegenrate(player)) {
            setLootTable.accept(savedLootTable);
        }
    }

    public void save(CompoundTag nbt) {
        if (lastGeneration > 0) nbt.putLong(Constants.TIMESTAMP_TAG, lastGeneration);

        if (savedLootTable != null) nbt.putString(Constants.SAVED_TABLE_TAG, savedLootTable.toString());

        nbt.putInt(Constants.GEN_COUNT_TAG, generatedCount);

        nbt.putString(Constants.REGEN_TYPE_TAG, regenType.getSerializedName());

        if (lastPlayer != null) nbt.putUUID(Constants.LAST_PLAYER_TAG, lastPlayer);

        var rewardedPlayersTag = new ListTag();
        rewardedPlayers.forEach(uuid -> {
            rewardedPlayersTag.add(NbtUtils.createUUID(uuid));
        });
        nbt.put(Constants.ALL_PLAYERS_TAG, rewardedPlayersTag);
    }

    public void load(CompoundTag nbt) {
        if (nbt.contains(Constants.TIMESTAMP_TAG)) lastGeneration = nbt.getLong(Constants.TIMESTAMP_TAG);

        if (nbt.contains(Constants.SAVED_TABLE_TAG))
            savedLootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(nbt.getString(Constants.SAVED_TABLE_TAG)));

        if (nbt.contains(Constants.GEN_COUNT_TAG)) generatedCount = nbt.getInt(Constants.GEN_COUNT_TAG);

        if (nbt.contains(Constants.REGEN_TYPE_TAG)) {
            regenType = RegenType.valueOf(nbt.getString(Constants.REGEN_TYPE_TAG).toUpperCase(Locale.ROOT));
        }

        if (nbt.contains(Constants.LAST_PLAYER_TAG)) lastPlayer = nbt.getUUID(Constants.LAST_PLAYER_TAG);
        if (nbt.contains(Constants.ALL_PLAYERS_TAG)) {
            rewardedPlayers.clear();
            nbt.getList(Constants.ALL_PLAYERS_TAG, 11).forEach(tag -> {
                rewardedPlayers.add(NbtUtils.loadUUID(tag));
            });
        }
    }

}
