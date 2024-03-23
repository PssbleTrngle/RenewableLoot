package com.possible_triangle.renewable_loot.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {

    @Inject(
            method = "itemStackFromJson(Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/ItemStack;",
            cancellable = true,
            at = @At("HEAD")
    )
    private static void readStackWithNbt(JsonObject json, CallbackInfoReturnable<ItemStack> cir) {
        Item item = ShapedRecipe.itemFromJson(json);

        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1) {
            throw new JsonSyntaxException("Invalid output count: " + count);
        }

        var stack = new ItemStack(item, count);

        if (json.has("data")) {
            var nbt = CompoundTag.CODEC.parse(JsonOps.INSTANCE, json.get("data")).result();
            nbt.ifPresent(stack::setTag);
        }

        cir.setReturnValue(stack);
    }

}
