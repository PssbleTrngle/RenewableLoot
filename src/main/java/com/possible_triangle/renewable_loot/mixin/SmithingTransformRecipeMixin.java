package com.possible_triangle.renewable_loot.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {

    @Inject(
            method = "assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            cancellable = true,
            at = @At("HEAD")
    )
    private void mergeNbt(Container container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        var accessor = (SmithingTransformRecipeAccessor) this;
        var result = accessor.getResult().copy();
        var input = container.getItem(1);

        if(input.hasTag()) {
            var tag = result.getOrCreateTag().copy();
            result.getOrCreateTag().merge(input.getTag());
            result.getOrCreateTag().merge(tag);
        }

        cir.setReturnValue(result);
    }

}
