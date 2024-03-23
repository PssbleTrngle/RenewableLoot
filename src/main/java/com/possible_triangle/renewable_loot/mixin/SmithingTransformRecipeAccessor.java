package com.possible_triangle.renewable_loot.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {

    @Accessor
    ItemStack getResult();

}
