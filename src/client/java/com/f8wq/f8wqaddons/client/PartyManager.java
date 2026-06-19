package com.f8wq.f8wqaddons.client;

import net.minecraft.client.Minecraft;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyManager {
    public static final Object lock = new Object();
    public static String leader = null;
    public static final Set<String> moderators = new LinkedHashSet<>();
    public static final Set<String> members = new LinkedHashSet<>();

    public static String getSelfName() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            return client.player.getName().getString();
        }
        return null;
    }

    public static void clearParty() {
        synchronized (lock) {
            leader = null;
            moderators.clear();
            members.clear();
        }
    }

    public static void onChatMessage(String message) {
        if (message == null) return;
        String clean = cleanColorCodes(message).trim();
        if (clean.isEmpty()) return;

        if (clean.equals("You left the party.") ||
            clean.equals("You are not currently in a party.") ||
            clean.equals("You are not in a party.") ||
            clean.equals("The party was disbanded.") ||
            clean.startsWith("The party was disbanded by") ||
            clean.equals("You have been kicked from the party.") ||
            clean.equals("You were kicked from the party.")) {
            clearParty();
            return;
        }

        if (clean.startsWith("You invited ") && clean.endsWith(" to the party!")) {
            String self = getSelfName();
            synchronized (lock) {
                if (self != null) {
                    members.add(self);
                    if (leader == null) {
                        leader = self;
                    }
                }
            }
            return;
        }

        if (clean.startsWith("You have joined ") && clean.endsWith("'s party!")) {
            clearParty();
            String namePart = clean.substring("You have joined ".length(), clean.length() - "'s party!".length());
            String name = extractNameFromRankedString(namePart);
            if (name != null) {
                synchronized (lock) {
                    leader = name;
                    members.add(name);
                    String self = getSelfName();
                    if (self != null) {
                        members.add(self);
                    }
                }
            }
            return;
        }

        if (clean.endsWith(" has joined the party.") || clean.endsWith(" joined the party.")) {
            String namePart = clean.replace(" has joined the party.", "").replace(" joined the party.", "");
            String name = extractNameFromRankedString(namePart);
            if (name != null) {
                synchronized (lock) {
                    members.add(name);
                }
            }
            return;
        }

        if (clean.endsWith(" has left the party.") || clean.endsWith(" left the party.")) {
            String namePart = clean.replace(" has left the party.", "").replace(" left the party.", "");
            String name = extractNameFromRankedString(namePart);
            if (name != null) {
                synchronized (lock) {
                    members.remove(name);
                    moderators.remove(name);
                    if (name.equals(leader)) {
                        leader = null;
                    }
                }
            }
            return;
        }

        if (clean.endsWith(" has been removed from the party.") || clean.endsWith(" was removed from the party.")) {
            String namePart = clean.replace(" has been removed from the party.", "").replace(" was removed from the party.", "");
            String name = extractNameFromRankedString(namePart);
            if (name != null) {
                synchronized (lock) {
                    members.remove(name);
                    moderators.remove(name);
                    if (name.equals(leader)) {
                        leader = null;
                    }
                }
            }
            return;
        }

        if (clean.contains("has been transferred to")) {
            int idx = clean.indexOf("has been transferred to");
            String part = clean.substring(idx + "has been transferred to".length()).replace("!", "").trim();
            String name = extractNameFromRankedString(part);
            if (name != null) {
                synchronized (lock) {
                    leader = name;
                    members.add(name);
                }
            }
            return;
        }

        if (clean.startsWith("Party Leader: ")) {
            clearParty();
            String content = clean.substring("Party Leader: ".length());
            parseNamesInto(content, true, false);
            return;
        }
        if (clean.startsWith("Party Moderators: ")) {
            String content = clean.substring("Party Moderators: ".length());
            parseNamesInto(content, false, true);
            return;
        }
        if (clean.startsWith("Party Members: ")) {
            String content = clean.substring("Party Members: ".length());
            parseNamesInto(content, false, false);
            return;
        }
    }

    private static void parseNamesInto(String content, boolean isLeader, boolean isMod) {
        String cleaned = content.replaceAll("\\[[^\\]]+\\]", "");
        Matcher m = Pattern.compile("\\w+").matcher(cleaned);
        synchronized (lock) {
            while (m.find()) {
                String name = m.group();
                if (isLeader) {
                    leader = name;
                }
                if (isMod) {
                    moderators.add(name);
                }
                members.add(name);
            }
        }
    }

    public static String extractNameFromRankedString(String str) {
        if (str == null) return null;
        String cleaned = str.replaceAll("\\[[^\\]]+\\]", "").trim();
        Matcher m = Pattern.compile("\\w+").matcher(cleaned);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    public static String cleanColorCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
