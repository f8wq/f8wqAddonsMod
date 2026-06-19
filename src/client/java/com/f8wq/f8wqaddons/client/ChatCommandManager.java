package com.f8wq.f8wqaddons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatCommandManager {

    private static String lastProcessedMessage = null;
    private static long lastProcessedTime = 0;

    public static boolean onOutgoingChatMessage(String message) {
        if (message == null)
            return false;

        ModConfig config = F8wqAddonsClient.config.getInstance();
        if (config == null || !config.chat.enableChatCommands)
            return false;

        String trimmed = message.trim();
        if (trimmed.equalsIgnoreCase("!f8ahelp")) {
            displayChatHelpMessage();
            return true;
        }

        if (!trimmed.toLowerCase().startsWith("!crypt"))
            return false;

        String[] parts = trimmed.split("\\s+", 2);
        String targetUser;
        if (parts.length < 2 || parts[1].isBlank()) {
            targetUser = getSelfName();
        } else {
            targetUser = parts[1].trim();
        }

        if (targetUser != null) {
            sendResponse(targetUser);
        }
        return true;
    }

    public static void onChatMessage(String rawMessage, boolean overlay) {
        if (overlay)
            return;
        if (rawMessage == null)
            return;

        long now = System.currentTimeMillis();
        if (rawMessage.equals(lastProcessedMessage) && (now - lastProcessedTime) < 100) {
            return;
        }
        lastProcessedMessage = rawMessage;
        lastProcessedTime = now;

        handleIncomingChatMessage(rawMessage);
    }

    private static void handleIncomingChatMessage(String rawMessage) {
        String clean = cleanColorCodes(rawMessage).trim();
        if (clean.isEmpty())
            return;

        ModConfig config = F8wqAddonsClient.config.getInstance();
        if (config == null || !config.chat.enableChatCommands)
            return;

        String selfName = getSelfName();

        Pattern helpPattern = Pattern.compile(
                "^Party > (?:\\[[^\\]]+\\] )?(\\w+): !f8ahelp", Pattern.CASE_INSENSITIVE);
        Matcher helpMatcher = helpPattern.matcher(clean);
        if (helpMatcher.find()) {
            String sender = helpMatcher.group(1);
            if (sender.equalsIgnoreCase(selfName))
                return;
            displayChatHelpMessage();
            return;
        }

        Pattern partyPattern = Pattern.compile(
                "^Party > (?:\\[[^\\]]+\\] )?(\\w+): !crypt (\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher partyMatcher = partyPattern.matcher(clean);
        if (partyMatcher.find()) {
            String sender = partyMatcher.group(1);
            if (sender.equalsIgnoreCase(selfName))
                return;
            String targetUser = partyMatcher.group(2);
            sendResponse(targetUser);
        }
    }

    private static void sendResponse(String targetUser) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null)
            return;

        String url = "https://sky.shiiyu.moe/stats/" + targetUser;

        Component divider = Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

        Component label = Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("F8wqAddons").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY));

        Component link = Component.literal("[here]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withUnderlined(true));

        Component mainLine = Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("F8wqAddons").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Click ").withStyle(ChatFormatting.WHITE))
                .append(link)
                .append(Component.literal(" to open ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(targetUser).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("'s SkyCrypt!").withStyle(ChatFormatting.WHITE));

        client.player.displayClientMessage(Component.empty(), false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(mainLine, false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(Component.empty(), false);
    }

    public static void displayHelpMessage() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null)
            return;

        Component divider = Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

        Component title = Component.literal("                  F8wqAddons Commands")
                .withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true));

        Component configHelp = Component.literal("/f8wqAddons (or /f8a) ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("- ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Open the configuration menu").withStyle(ChatFormatting.WHITE));

        Component helpHelp = Component.literal("/f8wqAddons help (or /f8a help) ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("- ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("View all available commands").withStyle(ChatFormatting.WHITE));

        client.player.displayClientMessage(Component.empty(), false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(title, false);
        client.player.displayClientMessage(configHelp, false);
        client.player.displayClientMessage(helpHelp, false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(Component.empty(), false);
    }

    public static void displayChatHelpMessage() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null)
            return;

        Component divider = Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

        Component commandsList = Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("F8wqAddons").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Commands: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("!crypt [username]").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("!f8ahelp").withStyle(ChatFormatting.YELLOW));

        client.player.displayClientMessage(Component.empty(), false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(commandsList, false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(Component.empty(), false);
    }

    private static String getSelfName() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            return client.player.getName().getString();
        }
        return null;
    }

    private static String cleanColorCodes(String input) {
        if (input == null)
            return null;
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
