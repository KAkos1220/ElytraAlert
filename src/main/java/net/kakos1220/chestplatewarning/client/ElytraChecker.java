package net.kakos1220.chestplatewarning.client;

import net.kakos1220.chestplatewarning.ChestplateWarning;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import java.lang.reflect.Field;
import java.util.Map;

public class ElytraChecker {

    public static boolean caution = false;
    public static boolean noAdvancement = false;

    public static void checkPlayerState() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null || client.level == null) {
            return;
        }

        if (client.gameMode.getPlayerMode() != GameType.SURVIVAL && ChestplateWarning.considerGameMode) {
            caution = false;
            return;
        }

        if (noAdvancement && ChestplateWarning.considerAdvancement) {
            caution = false;
            return;
        }

        ItemStack chestSlot = client.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean noElytra = !chestSlot.is(Items.ELYTRA);
        boolean inEnd = client.level.dimension() == Level.END;
        boolean isElytraDamaged = false;
        if (chestSlot.is(Items.ELYTRA)) {
            var unbreaking = client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
            int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(unbreaking, chestSlot);
            int durabilityMultiplier = 1;

            switch (unbreakingLevel) {
                case 0 -> durabilityMultiplier = 1;
                case 1 -> durabilityMultiplier = 2;
                case 2 -> durabilityMultiplier = 3;
                case 3 -> durabilityMultiplier = 4;
            }

            int durability;
            if (ChestplateWarning.considerUnbreaking) {
                durability = ((432 - chestSlot.getDamageValue()) * durabilityMultiplier);
            }
            else {
                durability = (432 - chestSlot.getDamageValue());
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

    public static boolean hasAdvancement(String path) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener networkHandler = client.getConnection();
        if (client.player == null || networkHandler == null) return true;

        ClientAdvancements advancementManager = networkHandler.getAdvancements();
        Identifier advancementId = Identifier.parse(path);
        AdvancementHolder entry = advancementManager.get(advancementId);
        if (entry == null) return false;

        try {
            Field field = ClientAdvancements.class.getDeclaredField("progress");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<AdvancementHolder, AdvancementProgress> progressMap = (Map<AdvancementHolder, AdvancementProgress>) field.get(advancementManager);
            AdvancementProgress progress = progressMap.get(entry);
            return progress != null && progress.isDone();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return true;
        }
    }
}
