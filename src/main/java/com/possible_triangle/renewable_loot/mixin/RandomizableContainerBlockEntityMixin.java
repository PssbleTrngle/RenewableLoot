package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.Config;
import com.possible_triangle.renewable_loot.Constants;
import com.possible_triangle.renewable_loot.RegenType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Mixin(RandomizableContainerBlockEntity.class)
public class RandomizableContainerBlockEntityMixin {

    @Unique
    @Nullable
    public ResourceLocation savedLootTable;

    @Unique
    public Long lastGeneration = -1L;

    @Unique
    public int generatedCount = 0;

    @Unique
    @Nullable
    public UUID lastPlayer;

    @Unique
    public final List<UUID> rewardedPlayers = new ArrayList<>();

    @Unique
    public RegenType regenType = RegenType.ONCE_PER_PLAYER;

    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;lootTable:Lnet/minecraft/resources/ResourceLocation;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private void saveTimestamp(Player player, CallbackInfo ci) {
        var accessor = (RandomizableContainerBlockEntityAccessor) this;

        generatedCount++;
        lastGeneration = System.currentTimeMillis();
        if (player != null) {
            lastPlayer = player.getUUID();
            rewardedPlayers.add(lastPlayer);
        }

        if (accessor.getLootTable() != null) {
            savedLootTable = accessor.getLootTable();
        }
    }


    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD")
    )
    private void beforeLootUnpack(Player player, CallbackInfo ci) {
        var self = (RandomizableContainerBlockEntity) (Object) this;

        if (!self.hasLevel() || self.getLevel().isClientSide()) return;

        if (savedLootTable == null) return;
        if (lastGeneration < 0) return;
        if (regenType == RegenType.DISABLED) return;
        if (player == null && regenType != RegenType.ALWAYS) return;
        if (regenType == RegenType.DIFFERENT_PLAYER && player.getUUID().equals(lastPlayer)) return;
        if (regenType == RegenType.ONCE_PER_PLAYER && rewardedPlayers.contains(player.getUUID())) return;

        var now = System.currentTimeMillis();

        if (now - lastGeneration >= Config.INSTANCE.getTimeout()) {
            if (Config.INSTANCE.clearContents()) self.clearContent();
            self.setLootTable(savedLootTable, self.getLevel().random.nextLong());
        } else {
            Constants.LOGGER.debug("loot container has now recharged yet");
        }
    }

    @Inject(
            method = "trySaveLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void saveAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
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

    @Inject(
            method = "tryLoadLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void loadAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        if (nbt.contains(Constants.TIMESTAMP_TAG)) lastGeneration = nbt.getLong(Constants.TIMESTAMP_TAG);

        if (nbt.contains(Constants.SAVED_TABLE_TAG))
            savedLootTable = new ResourceLocation(nbt.getString(Constants.SAVED_TABLE_TAG));

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
