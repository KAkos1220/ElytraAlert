package net.kakos1220.chestplatewarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.kakos1220.chestplatewarning.client.ElytraChecker;
import net.kakos1220.chestplatewarning.client.WarningHudOverlay;

public class ChestplateWarningClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ElytraChecker.noAdvancement = !ElytraChecker.hasAdvancement("minecraft:end/elytra");

            ElytraChecker.checkPlayerState();

            if (client.hasSingleplayerServer() && client.getSingleplayerServer() != null) {
                String raw = client.getSingleplayerServer().getWorldData().getLevelName();
                String key = ChestplateWarning.key(raw);
                ChestplateWarning.isCurrentWorldDisabled =
                        !ChestplateWarning.worldToggles.getOrDefault(key, true);
            } else {
                ChestplateWarning.isCurrentWorldDisabled = false;
            }

            if (!client.hasSingleplayerServer() && client.getCurrentServer() != null) {
                var entry = client.getCurrentServer();
                String key = ChestplateWarning.key(entry.name + "_" + entry.ip);
                ChestplateWarning.isCurrentServerDisabled =
                        !ChestplateWarning.serverToggles.getOrDefault(key, true);
            } else {
                ChestplateWarning.isCurrentServerDisabled = false;
            }
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                WarningHudOverlay.WARNING,
                WarningHudOverlay::render
        );
    }
}
