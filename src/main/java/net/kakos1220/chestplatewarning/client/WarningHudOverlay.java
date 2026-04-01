package net.kakos1220.chestplatewarning.client;

import net.kakos1220.chestplatewarning.ChestplateWarning;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class WarningHudOverlay{
    public static final Identifier WARNING = Identifier.fromNamespaceAndPath(ChestplateWarning.MOD_ID, "textures/elytraalert.png");

    public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        if (!ElytraChecker.caution) {
            return;
        }

        if (!ChestplateWarning.isModDisabled && !ChestplateWarning.isCurrentWorldDisabled && !ChestplateWarning.isCurrentServerDisabled) {
            if (ChestplateWarning.isFlashingOn){
                int ticks = client.gui.getGuiTicks();
                if ((ticks / 3) % 2 == 0) {

                    int screenWidth = client.getWindow().getGuiScaledWidth();
                    int x = screenWidth - 52;
                    int y = 10;

                    context.blit(
                            RenderPipelines.GUI_TEXTURED,
                            WARNING,
                            x, y,
                            0, 0,
                            42, 36,
                            42, 36,
                            42, 36
                    );
                }
            }
            else {
                int screenWidth = client.getWindow().getGuiScaledWidth();
                int x = screenWidth - 52;
                int y = 10;

                context.blit(
                        RenderPipelines.GUI_TEXTURED,
                        WARNING,
                        x, y,
                        0, 0,
                        42, 36,
                        42, 36,
                        42, 36
                );
            }
        }
    }

}

