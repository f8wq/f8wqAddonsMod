package com.f8wq.f8wqaddons.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;

import java.net.URI;

public class WelcomeManager {

    private static boolean hasShownWelcome = false;
    private static boolean pendingWelcome = false;

    public static void onClientTick(Minecraft client) {
        if (!pendingWelcome)
            return;
        if (client.player == null)
            return;

        pendingWelcome = false;
        showWelcomeMessage(client);
    }

    public static void onServerJoin(String serverIp) {
        if (hasShownWelcome)
            return;
        if (serverIp == null)
            return;
        if (serverIp.toLowerCase().contains("hypixel")) {
            pendingWelcome = true;
        }
    }

    private static void showWelcomeMessage(Minecraft client) {
        hasShownWelcome = true;

        String version = FabricLoader.getInstance()
                .getModContainer("f8wqaddons")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");

        Component divider = Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

        Component titleLine = Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("F8wqAddons").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("v" + version).withStyle(ChatFormatting.AQUA));

        Component thankYouLine = Component.literal("Thank you for using my mod ")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(":D").withStyle(ChatFormatting.YELLOW));

        Component githubLink = Component.literal("[GitHub]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com")))
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withUnderlined(true));

        Component youtubeLink = Component.literal("[YouTube]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.youtube.com/@f8wq")))
                        .withColor(ChatFormatting.RED)
                        .withBold(true)
                        .withUnderlined(true));

        Component kofiLink = Component.literal("[Ko-fi]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://ko-fi.com")))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Donating only changes the color of your name\nfor other mod users in the future!")
                                        .withStyle(ChatFormatting.GOLD)))
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true)
                        .withUnderlined(true));

        Component linksLine = Component.literal("My ").withStyle(ChatFormatting.WHITE)
                .append(githubLink)
                .append(Component.literal(", ").withStyle(ChatFormatting.WHITE))
                .append(youtubeLink)
                .append(Component.literal(", and ").withStyle(ChatFormatting.WHITE))
                .append(kofiLink);

        client.player.displayClientMessage(Component.empty(), false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(titleLine, false);
        client.player.displayClientMessage(thankYouLine, false);
        client.player.displayClientMessage(linksLine, false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(Component.empty(), false);
    }
}
