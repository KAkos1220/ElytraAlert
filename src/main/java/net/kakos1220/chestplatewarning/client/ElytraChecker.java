package net.kakos1220.chestplatewarning.client;

import net.kakos1220.chestplatewarning.ChestplateWarning;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.GameMode;

public class ElytraChecker {

    public static boolean caution = false;
    public static boolean noElytra = true;

    public static void checkPlayerState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null || client.world == null) {
            return;
        }

        if (client.interactionManager.getCurrentGameMode() != GameMode.SURVIVAL) {
            caution = false;
            return;
        }

        if (noElytra) {
            caution = false;
            return;
        }

        ItemStack chestSlot = client.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean noElytra = !chestSlot.isOf(Items.ELYTRA);
        boolean inEnd = client.world.getRegistryKey() == World.END;
        boolean isElytraDamaged = false;
        if (chestSlot.isOf(Items.ELYTRA)) {
            var unbreaking = client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
            int unbreakingLevel = EnchantmentHelper.getLevel(unbreaking, chestSlot);
            int durabilityMultiplier = 1;

            switch (unbreakingLevel) {
                case 0 -> durabilityMultiplier = 1;
                case 1 -> durabilityMultiplier = 2;
                case 2 -> durabilityMultiplier = 3;
                case 3 -> durabilityMultiplier = 4;
            }

            int durability;
            if (ChestplateWarning.considerUnbreaking) {
                durability = ((432 - chestSlot.getDamage()) * durabilityMultiplier);
            }
            else {
                durability = (432 - chestSlot.getDamage());
            }

            int threshold = (int)(4.32 * ChestplateWarning.elytraDurabilityThreshold);

            isElytraDamaged = durability <= threshold;
        }

        if (ChestplateWarning.isDamageWarningOn && isElytraDamaged) {
            caution = true;
        }
        else {
            caution = inEnd && noElytra;
        }
    }

    public static boolean hasAdvancement(ServerPlayerEntity player, String path) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        Identifier advancementId = Identifier.of(path);
        AdvancementEntry entry = server.getAdvancementLoader().get(advancementId);

        if (entry == null) return false;

        Advancement advancement = entry.value();
        AdvancementProgress progress = player.getAdvancementTracker().getProgress(entry);

        return progress.isDone();
    }
}
