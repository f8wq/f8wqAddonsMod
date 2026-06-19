package com.f8wq.f8wqaddons.client;

import io.github.notenoughupdates.moulconfig.common.IMinecraft;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class F8wqAddonsClient implements ClientModInitializer {

	public static ManagedConfig<ModConfig> config;

	@Override
	public void onInitializeClient() {
		ManagedConfigBuilder<ModConfig> builder = new ManagedConfigBuilder<>(
				new File("config/f8wqaddons/config.json"),
				ModConfig.class
		);
		builder.setSaveFailed((dataFile, ex) -> {
			System.out.println("Moulconfig save failed: " + ex.getMessage() + ". Retrying with fallback...");
			try {
				String serialized = dataFile.getMapper().serialize(dataFile.getInstance());
				java.nio.file.Files.writeString(dataFile.getFile().toPath(), serialized);
				System.out.println("Successfully saved config using fallback!");
				File parentDir = dataFile.getFile().getParentFile();
				if (parentDir != null && parentDir.exists()) {
					File[] files = parentDir.listFiles((dir, name) -> name.startsWith("config-") && name.endsWith("-save.json"));
					if (files != null) {
						for (File tempFile : files) {
							tempFile.delete();
						}
					}
				}
			} catch (Exception ioEx) {
				System.err.println("Fallback save also failed!");
				ioEx.printStackTrace();
			}
		});
		config = new ManagedConfig<>(builder);

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			PartyManager.onChatMessage(message.getString());
			ChatCommandManager.onChatMessage(message.getString(), overlay);
		});
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			PartyManager.onChatMessage(message.getString());
			ChatCommandManager.onChatMessage(message.getString(), false);
		});

		ClientSendMessageEvents.ALLOW_CHAT.register((message) ->
				!ChatCommandManager.onOutgoingChatMessage(message));

		HudRenderCallback.EVENT.register(new PartyHudRenderer());

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			var server = client.getCurrentServer();
			String ip = (server != null) ? server.ip : "";
			WelcomeManager.onServerJoin(ip);
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> WelcomeManager.onClientTick(client));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("f8wqAddons")
					.executes(ctx -> {
						Minecraft.getInstance().schedule(() -> {
							var editor = config.getEditor();
							IMinecraft.INSTANCE.openWrappedScreen(new GuiContext(new GuiElementComponent(editor)) {
								@Override
								public void onAfterClose() {
									super.onAfterClose();
									editor.onAfterClose();
								}
							});
						});
						return 1;
					})
					.then(literal("help").executes(ctx -> {
						Minecraft.getInstance().schedule(() -> ChatCommandManager.displayHelpMessage());
						return 1;
					}))
					.then(buildCcCommand())
			);
			dispatcher.register(literal("f8a")
					.executes(ctx -> {
						Minecraft.getInstance().schedule(() -> {
							var editor = config.getEditor();
							IMinecraft.INSTANCE.openWrappedScreen(new GuiContext(new GuiElementComponent(editor)) {
								@Override
								public void onAfterClose() {
									super.onAfterClose();
									editor.onAfterClose();
								}
							});
						});
						return 1;
					})
					.then(literal("help").executes(ctx -> {
						Minecraft.getInstance().schedule(() -> ChatCommandManager.displayHelpMessage());
						return 1;
					}))
					.then(buildCcCommand())
			);
		});
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> buildCcCommand() {
		return literal("cc")
				.executes(ctx -> {
					CarryCalculator.displayHelp();
					return 1;
				})
				.then(argument("amount", integer(1))
						.suggests(CarryCalculator::suggestAmounts)
						.executes(ctx -> {
							CarryCalculator.displayHelp();
							return 1;
						})
						.then(argument("slayer", word())
								.suggests(CarryCalculator::suggestSlayers)
								.executes(ctx -> {
									CarryCalculator.displayHelp();
									return 1;
								})
								.then(argument("tier", integer(1, 4))
										.suggests(CarryCalculator::suggestTiers)
										.executes(ctx -> CarryCalculator.execute(ctx, false))
										.then(argument("location", word())
												.suggests(CarryCalculator::suggestLocations)
												.executes(ctx -> CarryCalculator.execute(ctx, true))
										)
								)
						)
				);
	}
}