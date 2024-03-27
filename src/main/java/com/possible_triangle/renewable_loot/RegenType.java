package com.possible_triangle.renewable_loot;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum RegenType implements StringRepresentable {

    ALWAYS,
    ONCE_PER_PLAYER,
    DIFFERENT_PLAYER,
    DISABLED;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
