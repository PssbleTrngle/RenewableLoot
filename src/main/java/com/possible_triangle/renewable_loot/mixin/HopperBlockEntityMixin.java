package com.possible_triangle.renewable_loot.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.possible_triangle.renewable_loot.FabricEntrypoint;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(
            method = "getContainerAt(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/Container;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
                    shift = At.Shift.BY, by = 2
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void preventLootSucking(Level level, double d, double e, double f, CallbackInfoReturnable<Container> cir,  @Local BlockEntity blockEntity) {
        if(FabricEntrypoint.isLootContainer(blockEntity)) {
            cir.setReturnValue(null);
        }
    }

}
