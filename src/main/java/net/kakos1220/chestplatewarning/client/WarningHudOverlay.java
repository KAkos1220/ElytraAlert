package net.kakos1220.chestplatewarning.client;

import net.kakos1220.chestplatewarning.ChestplateWarning;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class WarningHudOverlay{
    public static final Identifier WARNING = Identifier.of(ChestplateWarning.MOD_ID, "textures/elytraalert.png");

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }
        if (!ElytraChecker.caution) {
            return;
        }

        if (!ChestplateWarning.isModDisabled && !ChestplateWarning.isCurrentWorldDisabled && !ChestplateWarning.isCurrentServerDisabled) {
            if (ChestplateWarning.isFlashingOn){
                int ticks = client.inGameHud.getTicks();
                if ((ticks / 3) % 2 == 0) {

                    int screenWidth = client.getWindow().getScaledWidth();
                    int x = screenWidth - 52;
                    int y = 10;

                    context.drawTexture(
                            RenderLayer::getGuiTextured,
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
                int screenWidth = client.getWindow().getScaledWidth();
                int x = screenWidth - 52;
                int y = 10;

                context.drawTexture(
                        RenderLayer::getGuiTextured,
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

