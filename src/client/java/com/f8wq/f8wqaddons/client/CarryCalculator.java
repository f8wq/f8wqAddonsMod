package com.f8wq.f8wqaddons.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CarryCalculator {

    public static CompletableFuture<Suggestions> suggestAmounts(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) {
        List<String> options = Arrays.asList("5", "10", "20", "50");
        return suggestOptions(options, builder);
    }

    public static CompletableFuture<Suggestions> suggestSlayers(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) {
        List<String> options = Arrays.asList("eman", "blaze");
        return suggestOptions(options, builder);
    }

    public static CompletableFuture<Suggestions> suggestTiers(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) {
        try {
            String slayer = StringArgumentType.getString(context, "slayer");
            if ("eman".equalsIgnoreCase(slayer)) {
                return suggestOptions(Arrays.asList("4"), builder);
            }
        } catch (IllegalArgumentException e) {

        }
        return suggestOptions(Arrays.asList("1", "2", "3", "4"), builder);
    }

    public static CompletableFuture<Suggestions> suggestLocations(CommandContext<FabricClientCommandSource> context,
            SuggestionsBuilder builder) {
        try {
            String slayer = StringArgumentType.getString(context, "slayer");
            if ("eman".equalsIgnoreCase(slayer)) {
                return suggestOptions(Arrays.asList("bruiser", "void"), builder);
            }
        } catch (IllegalArgumentException e) {
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestOptions(List<String> options, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(remaining)) {
                builder.suggest(option);
            }
        }
        return builder.buildFuture();
    }

    public static int execute(CommandContext<FabricClientCommandSource> context, boolean hasLocation) {
        Minecraft.getInstance().schedule(() -> {
            try {
                ModConfig config = F8wqAddonsClient.config.getInstance();
                if (config == null || !config.carry.enableCarryCalc) {
                    sendError("Carry calculator is disabled in config! Toggle it on via /f8a.");
                    return;
                }
                int amount = IntegerArgumentType.getInteger(context, "amount");
                String slayer = StringArgumentType.getString(context, "slayer");
                int tier = IntegerArgumentType.getInteger(context, "tier");
                String location = hasLocation ? StringArgumentType.getString(context, "location") : "";

                calculateAndDisplay(amount, slayer, tier, location);
            } catch (Exception e) {
                sendError("An error occurred while parsing command arguments.");
                e.printStackTrace();
            }
        });
        return 1;
    }

    public static void displayHelp() {
        Minecraft.getInstance().schedule(() -> {
            ModConfig config = F8wqAddonsClient.config.getInstance();
            if (config == null || !config.carry.enableCarryCalc) {
                sendError("Carry calculator is disabled in config! Toggle it on via /f8a.");
                return;
            }
            Minecraft client = Minecraft.getInstance();
            if (client.player == null)
                return;

            Component divider = Component.literal("-----------------------------------")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

            Component title = Component.literal("            F8wqAddons Carry Calculator Help")
                    .withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true));

            Component usage = Component.literal("Usage: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("/f8a cc [amount] [eman/blaze] [tier] [location]")
                            .withStyle(ChatFormatting.WHITE));

            Component info1 = Component.literal("  • Blaze: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("Tiers 1-4. Bulk discount starts at 5+ runs.")
                            .withStyle(ChatFormatting.GRAY));

            Component info2 = Component.literal("  • Eman: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(
                            "Tier 4 only. Location is required ('bruiser' or 'void'). Bulk discount starts at 10+ runs.")
                            .withStyle(ChatFormatting.GRAY));

            client.player.displayClientMessage(Component.empty(), false);
            client.player.displayClientMessage(divider, false);
            client.player.displayClientMessage(title, false);
            client.player.displayClientMessage(usage, false);
            client.player.displayClientMessage(Component.empty(), false);
            client.player.displayClientMessage(info1, false);
            client.player.displayClientMessage(info2, false);
            client.player.displayClientMessage(divider, false);
            client.player.displayClientMessage(Component.empty(), false);
        });
    }

    private static void sendError(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return;

        Component label = Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("F8wqAddons").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(message).withStyle(ChatFormatting.RED));

        client.player.displayClientMessage(label, false);
    }

    private static void calculateAndDisplay(int amount, String slayer, int tier, String location) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return;

        if (amount < 1) {
            sendError("Amount must be at least 1.");
            return;
        }

        boolean isEman = "eman".equalsIgnoreCase(slayer);
        boolean isBlaze = "blaze".equalsIgnoreCase(slayer);

        if (!isEman && !isBlaze) {
            sendError("Invalid slayer type. Choose 'eman' or 'blaze'.");
            return;
        }

        long pricePerRun = 0;
        boolean isBulk = false;

        if (isBlaze) {
            if (tier < 1 || tier > 4) {
                sendError("Blaze carries are only available for Tiers 1-4.");
                return;
            }
            isBulk = amount >= 5;
            if (tier == 1 || tier == 2) {
                pricePerRun = isBulk ? 1_100_000 : 1_400_000;
            } else if (tier == 3) {
                pricePerRun = isBulk ? 2_200_000 : 2_500_000;
            } else {
                pricePerRun = isBulk ? 4_200_000 : 5_000_000;
            }
        } else {
            if (tier != 4) {
                sendError("Eman carries are only available for Tier 4.");
                return;
            }
            if (location == null || location.isBlank()) {
                sendError("Please specify spawn location: 'bruiser' or 'void'.");
                return;
            }

            boolean isBruiser = "bruiser".equalsIgnoreCase(location);
            boolean isVoid = "void".equalsIgnoreCase(location);

            if (!isBruiser && !isVoid) {
                sendError("Invalid spawn location. Choose 'bruiser' or 'void'.");
                return;
            }

            isBulk = amount >= 10;
            if (isBruiser) {
                pricePerRun = isBulk ? 1_600_000 : 1_800_000;
            } else {
                pricePerRun = isBulk ? 1_200_000 : 1_400_000;
            }
        }

        long totalPrice = pricePerRun * amount;
        final long finalPricePerRun = pricePerRun;

        String chatFormat;
        if (isEman) {
            chatFormat = String.format("[f8a] %d t%d at %s = %s", amount, tier, location.toLowerCase(),
                    formatShortPrice(totalPrice));
        } else {
            chatFormat = String.format("%d t%d = %s", amount, tier, formatShortPrice(totalPrice));
        }

        Component divider = Component.literal("-----------------------------------")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true));

        Component title = Component.literal("            carry price")
                .withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true));

        Component slayerLine = Component.literal("Slayer: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(capitalize(slayer) + " " + romanNumeral(tier))
                        .withStyle(ChatFormatting.WHITE));

        Component amountLine = Component.literal("Amount: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.WHITE));

        if (isEman) {
            amountLine = amountLine.copy()
                    .append(Component.literal(" Location: ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(capitalize(location)).withStyle(ChatFormatting.WHITE));
        }

        Component bulkLine = Component.literal("Bulk: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(isBulk ? "yes" : "no")
                        .withStyle(isBulk ? ChatFormatting.GREEN : ChatFormatting.RED));

        Component priceVal = Component.literal(formatCoins(totalPrice))
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Price per boss: " + formatCoins(finalPricePerRun)))));

        Component priceLine = Component.literal("Price: ").withStyle(ChatFormatting.YELLOW)
                .append(priceVal);
        // meow
        Component copyBtn = Component.literal("[copy]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.CopyToClipboard(chatFormat))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy: " + chatFormat)))
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true));

        Component actionsLine = copyBtn;

        client.player.displayClientMessage(Component.empty(), false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(title, false);
        client.player.displayClientMessage(slayerLine, false);
        client.player.displayClientMessage(amountLine, false);
        client.player.displayClientMessage(bulkLine, false);
        client.player.displayClientMessage(priceLine, false);
        client.player.displayClientMessage(actionsLine, false);
        client.player.displayClientMessage(divider, false);
        client.player.displayClientMessage(Component.empty(), false);
    }

    private static String formatCoins(long amount) {
        return String.format("%,d coins (%s)", amount, formatShortPrice(amount));
    }

    private static String formatShortPrice(long price) {
        if (price >= 1_000_000) {
            double millions = price / 1_000_000.0;
            if (millions == (long) millions) {
                return String.format("%.0fm", millions);
            } else {
                return String.format("%.1fm", millions);
            }
        } else if (price >= 1_000) {
            double thousands = price / 1_000.0;
            if (thousands == (long) thousands) {
                return String.format("%.0fk", thousands);
            } else {
                return String.format("%.1fk", thousands);
            }
        }
        return String.valueOf(price);
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty())
            return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private static String romanNumeral(int num) {
        switch (num) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            default:
                return String.valueOf(num);
        }
    }
}
