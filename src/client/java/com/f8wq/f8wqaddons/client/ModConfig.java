package com.f8wq.f8wqaddons.client;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigAccordionId;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorAccordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import net.minecraft.client.Minecraft;

public class ModConfig extends Config {

    @Expose
    @Category(name = "General", desc = "General settings")
    public GeneralCategory general = new GeneralCategory();

    public static class GeneralCategory {
        @Expose
        @ConfigOption(name = "Placeholder Toggle", desc = "This is a placeholder setting")
        @ConfigEditorBoolean
        public boolean placeholderToggle = false;
    }

    @Expose
    @Category(name = "Dungeons", desc = "Dungeon related settings")
    public DungeonCategory dungeon = new DungeonCategory();

    public static class DungeonCategory {
        @Expose
        @ConfigOption(name = "Dungeon Class", desc = "Select your class for F6/M6 boss room waypoints")
        @ConfigEditorDropdown
        public DungeonClass dungeonClass = DungeonClass.NONE;
    }

    public enum DungeonClass {
        NONE, HEALER, MAGE, BERSERK, ARCHER, TANK
    }

    @Expose
    @Category(name = "Party", desc = "Party related settings")
    public PartyCategory party = new PartyCategory();

    public static class PartyCategory {

        @Expose
        @ConfigOption(name = "Party HUD", desc = "Settings for the party member overlay")
        @ConfigEditorAccordion(id = 0)
        public boolean partyHudOpen = false;

        @Expose
        @ConfigAccordionId(id = 0)
        @ConfigOption(name = "Show Party Overlay", desc = "Show the party members overlay on screen")
        @ConfigEditorBoolean
        public boolean showPartyOverlay = false;

        @ConfigAccordionId(id = 0)
        @ConfigOption(name = "Edit HUD", desc = "Open the HUD editor to drag and resize the party overlay")
        @ConfigEditorButton(buttonText = "Open")
        public Runnable editHud = () -> {
            Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(new HudPositionEditorScreen()));
        };

        @Expose
        public int partyOverlayX = 10;

        @Expose
        public int partyOverlayY = 50;

        @Expose
        public float hudScale = 1.0f;
    }

    @Expose
    @Category(name = "Chat", desc = "Chat related settings")
    public ChatCategory chat = new ChatCategory();

    public static class ChatCategory {
        @Expose
        @ConfigOption(name = "Chat Commands", desc = "Toggle chat commands on and off")
        @ConfigEditorBoolean
        public boolean enableChatCommands = false;
    }

    @Expose
    @Category(name = "Carry", desc = "Carry settings")
    public CarryCategory carry = new CarryCategory();

    public static class CarryCategory {
        @Expose
        @ConfigOption(name = "Carry Price Calculator", desc = "Toggle the carry price calculator command")
        @ConfigEditorBoolean
        public boolean enableCarryCalc = false;
    }
}