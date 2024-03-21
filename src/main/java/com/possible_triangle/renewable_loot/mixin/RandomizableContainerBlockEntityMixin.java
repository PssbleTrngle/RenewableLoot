package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.Config;
import com.possible_triangle.renewable_loot.Constants;
import net.minecraft.nbt.CompoundTag;
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
    public boolean regenDisabled;

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
        if (player != null) lastPlayer = player.getUUID();

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

        if (regenDisabled) return;
        if (savedLootTable == null) return;
        if (lastGeneration < 0) return;
        if(player == null || player.getUUID().equals(lastPlayer)) return;

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
        nbt.putBoolean(Constants.REGEN_DISABLED_TAG, regenDisabled);
        if (lastPlayer != null) nbt.putUUID(Constants.LAST_PLAYER_TAG, lastPlayer);
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
        if (nbt.contains(Constants.REGEN_DISABLED_TAG)) regenDisabled = nbt.getBoolean(Constants.REGEN_DISABLED_TAG);
        if (nbt.contains(Constants.LAST_PLAYER_TAG)) lastPlayer = nbt.getUUID(Constants.LAST_PLAYER_TAG);
    }

}
