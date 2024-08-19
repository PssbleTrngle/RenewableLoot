package com.possible_triangle.renewable_loot;

public class Config {

    public static final Config INSTANCE = new Config();

    public long getTimeout() {
        return 1000L * 60 * 60 * 7;
    }

    public boolean clearContents() {
        return true;
    }

    public boolean disableBreaking() {
        return true;
    }

    public boolean disableBreakingPots() {
        return false;
    }

}
