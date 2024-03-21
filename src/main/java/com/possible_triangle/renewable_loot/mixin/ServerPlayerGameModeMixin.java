package com.possible_triangle.renewable_loot.mixin;

import com.possible_triangle.renewable_loot.Config;
import com.possible_triangle.renewable_loot.FabricEntrypoint;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(
            method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z",
            cancellable = true,
            at = @At("HEAD")
    )
    private void preventLootBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.INSTANCE.disableBreaking()) return;

        var accessor = (ServerPlayerGameModeAccessor) this;

        if(accessor.getPlayer().isCreative()) return;

        var blockEntity = accessor.getLevel().getBlockEntity(pos);

        if (FabricEntrypoint.isLootContainer(blockEntity)) {
            accessor.getPlayer().connection.send(new ClientboundSetActionBarTextPacket(Component.literal("You cannot break loot containers")));
            cir.setReturnValue(false);
        }
    }

}
