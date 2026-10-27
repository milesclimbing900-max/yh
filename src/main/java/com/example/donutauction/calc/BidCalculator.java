package com.example.donutauction.calc;

import com.example.donutauction.config.ModConfig;

/** Pure decision-support maths. It suggests a number; it never sends anything and guarantees nothing. */
public final class BidCalculator {
    public record Suggestion(boolean hasBase, double base, double suggested, boolean overBudget) {}

    private BidCalculator() {}

    public static Suggestion suggest(ModConfig c, double latestBid) {
        if (Double.isNaN(latestBid)) return new Suggestion(false, Double.NaN, Double.NaN, false);
        double next = c.incrementMode == ModConfig.IncrementMode.FIXED
                ? latestBid + c.incrementFixed
                : latestBid * (1.0 + c.incrementPercent / 100.0);
        next = Math.ceil(next);
        if (next <= latestBid) next = Math.floor(latestBid) + 1; // always strictly higher
        boolean over = c.maxBudget > 0 && next > c.maxBudget;
        return new Suggestion(true, latestBid, next, over);
    }
}
