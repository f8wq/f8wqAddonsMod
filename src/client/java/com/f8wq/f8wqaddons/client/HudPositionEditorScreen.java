package com.f8wq.f8wqaddons.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class HudPositionEditorScreen extends Screen {

    private static final int DEFAULT_X = 10;
    private static final int DEFAULT_Y = 50;
    private static final float DEFAULT_SCALE = 1.0f;

    private static final float SCALE_MIN = 0.5f;
    private static final float SCALE_MAX = 3.0f;
    private static final float SCALE_STEP = 0.1f;

    private static final int PADDING = 4;

    private boolean dragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public HudPositionEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    private static List<String> getMockLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lParty (3)§r");
        lines.add("§e★ Steve §7(Leader)§r");
        lines.add("§a⚒ Alex §7(Mod)§r");
        lines.add("§f● Notch§r");
        return lines;
    }

    private static List<String> getDisplayLines() {
        List<String> lines = new ArrayList<>();
        synchronized (PartyManager.lock) {
            if (!PartyManager.members.isEmpty()) {
                String selfName = PartyManager.getSelfName();
                if (selfName != null)
                    PartyManager.members.add(selfName);

                int count = PartyManager.members.size();
                lines.add("§6§lParty (" + count + ")§r");
                for (String name : PartyManager.members) {
                    if (name.equals(PartyManager.leader)) {
                        lines.add("§e★ " + name + " §7(Leader)§r");
                    } else if (PartyManager.moderators.contains(name)) {
                        lines.add("§a⚒ " + name + " §7(Mod)§r");
                    } else {
                        lines.add("§f● " + name + "§r");
                    }
                }
            }
        }
        return lines.isEmpty() ? getMockLines() : lines;
    }

    private ModConfig getConfig() {
        return F8wqAddonsClient.config.getInstance();
    }

    private int[] getHudBounds(List<String> lines) {
        ModConfig cfg = getConfig();
        float scale = cfg.party.hudScale;
        int lineH = minecraft.font.lineHeight;
        int rawW = 0;
        for (String l : lines)
            rawW = Math.max(rawW, minecraft.font.width(l));
        int rawH = lines.size() * (lineH + 2);

        int screenW = (int) (rawW * scale) + PADDING * 2;
        int screenH = (int) (rawH * scale) + PADDING * 2;
        return new int[] { cfg.party.partyOverlayX, cfg.party.partyOverlayY, screenW, screenH };
    }

    private boolean isMouseOverHud(double mx, double my, int[] b) {
        return mx >= b[0] && mx <= b[0] + b[2]
                && my >= b[1] && my <= b[1] + b[3];
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        gfx.fill(0, 0, width, height, 0x88000000);

        gfx.drawCenteredString(minecraft.font,
                "§eDrag§r to move  |  §eScroll§r to resize  |  §eR§r to reset  |  §eESC§r to save & exit",
                width / 2, 8, 0xFFFFFFFF);

        List<String> lines = getDisplayLines();
        int[] bounds = getHudBounds(lines);
        boolean hovered = dragging || isMouseOverHud(mouseX, mouseY, bounds);

        ModConfig cfg = getConfig();
        float scale = cfg.party.hudScale;
        int bx = bounds[0], by = bounds[1], bw = bounds[2], bh = bounds[3];

        gfx.fill(bx, by, bx + bw, by + bh, hovered ? 0x55FFFF00 : 0x44000000);

        int col = hovered ? 0xFFFFFF00 : 0xFFFFFFFF;
        gfx.fill(bx, by, bx + bw, by + 1, col);
        gfx.fill(bx, by + bh - 1, bx + bw, by + bh, col);
        gfx.fill(bx, by, bx + 1, by + bh, col);
        gfx.fill(bx + bw - 1, by, bx + bw, by + bh, col);

        gfx.pose().pushMatrix();
        gfx.pose().translate(bx + PADDING, by + PADDING);
        gfx.pose().scale(scale, scale);

        int lineH = minecraft.font.lineHeight;
        int ty = 0;
        for (String line : lines) {
            gfx.drawString(minecraft.font, line, 0, ty, 0xFFFFFFFF, true);
            ty += lineH + 2;
        }
        gfx.pose().popMatrix();

        String label = String.format("§7Scale: %.1fx§r", scale);
        gfx.drawString(minecraft.font, label, bx + 2, by + bh + 3, 0xFFAAAAAA, false);

        super.render(gfx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() == 0) {
            int[] bounds = getHudBounds(getDisplayLines());
            if (isMouseOverHud(event.x(), event.y(), bounds)) {
                dragging = true;
                dragOffsetX = event.x() - bounds[0];
                dragOffsetY = event.y() - bounds[1];
                return true;
            }
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging && event.button() == 0) {
            ModConfig cfg = getConfig();
            cfg.party.partyOverlayX = (int) Mth.clamp(event.x() - dragOffsetX, 0, width - 10);
            cfg.party.partyOverlayY = (int) Mth.clamp(event.y() - dragOffsetY, 0, height - 10);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            F8wqAddonsClient.config.saveToFile();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ModConfig cfg = getConfig();
        float newScale = Mth.clamp(cfg.party.hudScale + (float) scrollY * SCALE_STEP, SCALE_MIN, SCALE_MAX);
        cfg.party.hudScale = Math.round(newScale * 10f) / 10f;
        F8wqAddonsClient.config.saveToFile();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            ModConfig cfg = getConfig();
            cfg.party.partyOverlayX = DEFAULT_X;
            cfg.party.partyOverlayY = DEFAULT_Y;
            cfg.party.hudScale = DEFAULT_SCALE;
            F8wqAddonsClient.config.saveToFile();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        F8wqAddonsClient.config.saveToFile();
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
