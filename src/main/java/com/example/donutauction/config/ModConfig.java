package com.example.donutauction.config;

import com.example.donutauction.chat.EventType;
import com.example.donutauction.notify.NotificationSound;

import java.util.*;

public class ModConfig {
    public enum IncrementMode { FIXED, PERCENT }

    // ---- streamers ----
    public List<StreamerProfile> streamers = new ArrayList<>();
    public String selectedStreamer = "";

    // ---- timer / alerts ----
    public long defaultDurationMs = 60_000;
    public List<Integer> warningThresholdsSec = new ArrayList<>(List.of(30, 10, 5, 3, 2, 1));
    /** Final alert fires this many MILLISECONDS before the estimated end (0 = off). */
    public int bidAlertMs = 800;
    public int preciseBelowSec = 10;
    public boolean soundEnabled = true;
    public NotificationSound soundType = NotificationSound.PLING;
    public float soundVolume = 0.75f;
    public boolean chatNotifications = true;
    public boolean overlayNotifications = true;
    public boolean screenFlash = true;

    // ---- bidding ----
    public IncrementMode incrementMode = IncrementMode.FIXED;
    public double incrementFixed = 1000;
    public double incrementPercent = 5;
    /** 0 = no budget limit. */
    public double maxBudget = 0;
    /** If false, PAYMENT events never feed the bid calculator. */
    public boolean paymentsCountAsBids = false;

    // ---- display ----
    public boolean hudEnabled = true;
    public boolean hudShowWhenIdle = false;
    public int hudX = 6;
    public int hudY = 6;
    /** Panel background opacity 0.15 - 1.0 (text stays solid). */
    public float hudOpacity = 0.55f;

    // ---- parsing ----
    public boolean parsingEnabled = true;
    public boolean autoStartTimerFromChat = false;
    /** Only count payments whose recipient is the selected streamer (needs a (?<recipient>) group). */
    public boolean onlySelectedStreamer = true;
    /** Writes unmatched chat lines containing one of logKeywords to donutauction-chatlog.txt (local only). */
    public boolean logUnmatchedChat = false;
    public List<String> logKeywords = new ArrayList<>(List.of("auction", "bid", "paid", "payment", "sold", "won"));
    public List<ParseRule> rules = defaultRules();

    public static List<ParseRule> defaultRules() {
        List<ParseRule> l = new ArrayList<>();
        // SELFTEST rules: only match lines that START with "TEST ", used by `/dsmpa test ...`.
        l.add(new ParseRule("SELFTEST start", EventType.AUCTION_START, "^TEST START (?<user>\\w+) (?<duration>\\S+)$", true));
        l.add(new ParseRule("SELFTEST bid", EventType.BID, "^TEST BID (?<user>\\w+) (?<amount>\\S+)$", true));
        l.add(new ParseRule("SELFTEST payment", EventType.PAYMENT, "^TEST PAY (?<user>\\w+) (?<amount>\\S+)$", true));
        l.add(new ParseRule("SELFTEST end", EventType.AUCTION_END, "^TEST END(?: (?<user>\\w+) (?<amount>\\S+))?$", true));
        // Real DonutSMP line seen in chat: "IcyL0 was paid $6.4M by freebushr1"
        // recipient = the streamer, user = the bidder, amount supports K/M/B/T suffixes.
        l.add(new ParseRule("DonutSMP paid", EventType.BID,
                "(?<recipient>[\\w.]+) was paid \\$(?<amount>[\\d.,]+[kmbtKMBT]?) by (?<user>[\\w.]+)", true));
        // EXAMPLE rules: GUESSES, disabled. Replace with REAL DonutSMP messages once verified.
        l.add(new ParseRule("EXAMPLE bid (UNVERIFIED)", EventType.BID,
                "(?i)(?<user>\\w{3,16}) (?:placed a bid of|bid) \\$?(?<amount>[\\d.,]+[kmbt]?)", false));
        l.add(new ParseRule("EXAMPLE payment (UNVERIFIED)", EventType.PAYMENT,
                "(?i)(?<user>\\w{3,16}) (?:paid you|has paid you|sent you) \\$?(?<amount>[\\d.,]+[kmbt]?)", false));
        l.add(new ParseRule("EXAMPLE end (UNVERIFIED)", EventType.AUCTION_END,
                "(?i)auction (?:ended|has ended)", false));
        return l;
    }

    public StreamerProfile selected() {
        return find(selectedStreamer);
    }

    public StreamerProfile find(String name) {
        if (name == null) return null;
        for (StreamerProfile s : streamers) if (s.name.equalsIgnoreCase(name)) return s;
        return null;
    }

    /** Repairs anything a hand-edited or old config file might have broken. */
    public void sanitize() {
        if (streamers == null) streamers = new ArrayList<>();
        streamers.removeIf(s -> s == null || s.name == null || s.name.isBlank());
        for (StreamerProfile s : streamers) s.delayMs = Math.max(0, Math.min(600_000, s.delayMs));

        if (warningThresholdsSec == null) warningThresholdsSec = new ArrayList<>(List.of(30, 10, 5, 3, 2, 1));
        warningThresholdsSec.removeIf(v -> v == null || v <= 0 || v > 3600);
        warningThresholdsSec = new ArrayList<>(new TreeSet<>(warningThresholdsSec).descendingSet());

        if (rules == null) rules = defaultRules();
        rules.removeIf(Objects::isNull);
        if (logKeywords == null) logKeywords = new ArrayList<>(List.of("auction", "bid"));
        if (incrementMode == null) incrementMode = IncrementMode.FIXED;
        if (soundType == null) soundType = NotificationSound.PLING;

        soundVolume = Math.max(0f, Math.min(1f, soundVolume));
        bidAlertMs = Math.max(0, Math.min(60_000, bidAlertMs));
        hudOpacity = Math.max(0.15f, Math.min(1f, hudOpacity));
        preciseBelowSec = Math.max(0, Math.min(120, preciseBelowSec));
        defaultDurationMs = Math.max(1000, Math.min(86_400_000L, defaultDurationMs));
        incrementFixed = Math.max(0, incrementFixed);
        incrementPercent = Math.max(0, Math.min(1000, incrementPercent));
        maxBudget = Math.max(0, maxBudget);
        hudX = Math.max(0, hudX);
        hudY = Math.max(0, hudY);

        if (selected() == null) selectedStreamer = streamers.isEmpty() ? "" : streamers.get(0).name;
    }
}
