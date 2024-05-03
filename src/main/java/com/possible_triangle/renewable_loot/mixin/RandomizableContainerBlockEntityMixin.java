package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.Config;
import com.possible_triangle.renewable_loot.RegenerativeLoot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomizableContainerBlockEntity.class)
public class RandomizableContainerBlockEntityMixin {

    @Unique
    private final RegenerativeLoot data = new RegenerativeLoot();

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
        data.saveTimestamp(player, accessor.getLootTable());
    }

    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD")
    )
    private void beforeLootUnpack(Player player, CallbackInfo ci) {
        var self = (RandomizableContainerBlockEntity) (Object) this;

        if (!self.hasLevel() || self.getLevel().isClientSide()) return;

        data.onLooted(player, (table) -> {
            if (Config.INSTANCE.clearContents()) self.clearContent();
            self.setLootTable(table, self.getLevel().random.nextLong());
        });
    }

    @Inject(
            method = "trySaveLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void saveAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        data.save(nbt);
    }

    @Inject(
            method = "tryLoadLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void loadAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        data.load(nbt);
    }

}
