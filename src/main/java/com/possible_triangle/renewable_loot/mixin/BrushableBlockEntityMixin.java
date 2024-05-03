package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.RegenType;
import com.possible_triangle.renewable_loot.RegenerativeLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushableBlockEntity.class)
public class BrushableBlockEntityMixin {

    @Unique
    private final RegenerativeLoot data = new RegenerativeLoot();

    @Redirect(
            method = "brushingCompleted(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    )
    private boolean interceptSetBlock(Level level, BlockPos blockPos, BlockState blockState, int i) {
        var self = (BrushableBlockEntity) (Object) this;

        if (data.getRegenType() == RegenType.DISABLED) {
            return level.setBlock(blockPos, blockState, i);
        }

        return level.setBlock(blockPos, self.getBlockState().setValue(BlockStateProperties.DUSTED, 0), i);
    }

    @Inject(
            method = "unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/BrushableBlockEntity;lootTable:Lnet/minecraft/resources/ResourceLocation;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private void saveTimestamp(Player player, CallbackInfo ci) {
        var accessor = (BrushableBlockEntityAccessor) this;
        data.saveTimestamp(player, accessor.getLootTable());
    }

    @Inject(
            method = "brush(JLnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Direction;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrushableBlockEntity;unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE),
            cancellable = true
    )
    private void beforeUnpacking(long l, Player player, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        var self = (BrushableBlockEntity) (Object) this;
        var accessor = (BrushableBlockEntityAccessor) this;

        if (!self.hasLevel() || self.getLevel().isClientSide()) return;

        data.onLooted(player, table -> {
            self.setLootTable(table, self.getLevel().random.nextLong());
        });

        if(accessor.getLootTable() == null && accessor.getItem().isEmpty()) {
            cir.setReturnValue(false);
        }
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
