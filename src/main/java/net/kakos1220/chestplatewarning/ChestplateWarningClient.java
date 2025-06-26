package net.kakos1220.chestplatewarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.kakos1220.chestplatewarning.client.ElytraChecker;
import net.kakos1220.chestplatewarning.client.WarningHudOverlay;


public class ChestplateWarningClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ElytraChecker.checkPlayerState();

            if (client.isIntegratedServerRunning() && client.getServer() != null) {
                String raw = client.getServer().getSaveProperties().getLevelName();
                String key = ChestplateWarning.key(raw);
                ChestplateWarning.isCurrentWorldDisabled =
                        !ChestplateWarning.worldToggles.getOrDefault(key, true);
            } else {
                ChestplateWarning.isCurrentWorldDisabled = false;
            }

            if (!client.isIntegratedServerRunning() && client.getCurrentServerEntry() != null) {
                var entry = client.getCurrentServerEntry();
                String key = ChestplateWarning.key(entry.name + "_" + entry.address);
                ChestplateWarning.isCurrentServerDisabled =
                        !ChestplateWarning.serverToggles.getOrDefault(key, true);
            } else {
                ChestplateWarning.isCurrentServerDisabled = false;
            }
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
            layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, WarningHudOverlay.WARNING, WarningHudOverlay::render));
    }
}
