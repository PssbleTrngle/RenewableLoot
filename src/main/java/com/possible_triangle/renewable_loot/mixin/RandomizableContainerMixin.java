package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.Config;
import com.possible_triangle.renewable_loot.RegenerativeContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {

    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/RandomizableContainer;setLootTable(Lnet/minecraft/resources/ResourceKey;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void saveTimestamp(Player player, CallbackInfo ci) {
        if (!(this instanceof RegenerativeContainer container)) return;
        container.regenerative_loot$getData().saveTimestamp(player, container.regenerative_loot$getLootTable());
    }

    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD")
    )
    private void beforeLootUnpack(Player player, CallbackInfo ci) {
        var self = (RandomizableContainer) this;
        if (!(this instanceof RegenerativeContainer container)) return;

        var level = self.getLevel();
        if (level == null || level.isClientSide()) return;

        container.regenerative_loot$getData().onLooted(player, (table) -> {
            if (Config.INSTANCE.clearContents()) self.clearContent();
            self.setLootTable(table, self.getLevel().random.nextLong());
        });
    }

    @Inject(
            method = "trySaveLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void saveAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof RegenerativeContainer container)) return;
        container.regenerative_loot$getData().save(nbt);
    }

    @Inject(
            method = "tryLoadLootTable(Lnet/minecraft/nbt/CompoundTag;)Z",
            at = @At("HEAD")
    )
    private void loadAdditional(CompoundTag nbt, CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof RegenerativeContainer container)) return;
        container.regenerative_loot$getData().load(nbt);
    }

}
