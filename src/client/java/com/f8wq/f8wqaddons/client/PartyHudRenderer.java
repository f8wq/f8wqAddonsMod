package com.f8wq.f8wqaddons.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

import java.util.ArrayList;
import java.util.List;

public class PartyHudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(GuiGraphics drawContext, DeltaTracker renderTickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        if (client.screen instanceof HudPositionEditorScreen) return;

        ModConfig config = F8wqAddonsClient.config.getInstance();
        if (config == null || !config.party.showPartyOverlay) return;

        List<String> displayLines = new ArrayList<>();
        synchronized (PartyManager.lock) {
            if (PartyManager.members.isEmpty()) {
                return;
            }

            String selfName = PartyManager.getSelfName();
            if (selfName != null) {
                PartyManager.members.add(selfName);
                if (PartyManager.leader == null) {
                    PartyManager.leader = selfName;
                }
            }

            int count = PartyManager.members.size();
            displayLines.add("§6§lParty (" + count + ")§r");

            for (String name : PartyManager.members) {
                if (name.equals(PartyManager.leader)) {
                    displayLines.add("§e★ " + name + " §7(Leader)§r");
                } else if (PartyManager.moderators.contains(name)) {
                    displayLines.add("§a⚒ " + name + " §7(Mod)§r");
                } else {
                    displayLines.add("§f● " + name + "§r");
                }
            }
        }

        Font font = client.font;
        int x = config.party.partyOverlayX;
        int y = config.party.partyOverlayY;
        float scale = config.party.hudScale;

        drawContext.pose().pushMatrix();
        drawContext.pose().translate(x, y);
        drawContext.pose().scale(scale, scale);

        int ty = 0;
        for (String line : displayLines) {
            drawContext.drawString(font, line, 0, ty, 0xFFFFFFFF, true);
            ty += font.lineHeight + 2;
        }

        drawContext.pose().popMatrix();
    }
}
