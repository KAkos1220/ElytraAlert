package net.kakos1220.chestplatewarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kakos1220.chestplatewarning.client.ElytraChecker;
import net.kakos1220.chestplatewarning.client.WarningHudOverlay;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChestplateWarningClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ElytraChecker.noElytra = !ElytraChecker.hasAdvancement(player, "minecraft:end/elytra");
        });

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
