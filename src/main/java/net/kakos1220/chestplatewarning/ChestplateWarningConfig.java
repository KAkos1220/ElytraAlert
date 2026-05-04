package net.kakos1220.chestplatewarning;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChestplateWarningConfig implements ModMenuApi, ConfigScreenFactory<Screen> {

    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return this;
    }

    @Override
    public Screen create(Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(Component.translatable("config.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));


        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.isModDisabled"), ChestplateWarning.isModDisabled
                )
                .setDefaultValue(false)
                .setSaveConsumer((newValue) -> ChestplateWarning.isModDisabled = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.isFlashingOn"), ChestplateWarning.isFlashingOn
                )
                .setDefaultValue(true)
                .setYesNoTextSupplier(newValue -> newValue
                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("config.off").withStyle(ChatFormatting.RED)
                )
                .setSaveConsumer((newValue) -> ChestplateWarning.isFlashingOn = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.isDamageWarningOn"), ChestplateWarning.isDamageWarningOn
                )
                .setTooltip(Component.translatable("config.isDamageWarningOn.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(newValue -> newValue
                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("config.off").withStyle(ChatFormatting.RED)
                )
                .setSaveConsumer((newValue) -> ChestplateWarning.isDamageWarningOn = newValue)
                .build());

        general.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("config.elytraDurabilityThreshold"), ChestplateWarning.elytraDurabilityThreshold, 5, 50
                )
                .setTooltip(Component.translatable("config.elytraDurabilityThreshold.tooltip"))
                .setDefaultValue(10)
                .setTextGetter(newValue -> Component.literal(newValue + "%"))
                .setSaveConsumer((newValue) -> ChestplateWarning.elytraDurabilityThreshold = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.considerUnbreaking"), ChestplateWarning.considerUnbreaking
                )
                .setDefaultValue(true)
                .setYesNoTextSupplier(newValue -> newValue
                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("config.off").withStyle(ChatFormatting.RED)
                )
                .setSaveConsumer((newValue) -> ChestplateWarning.considerUnbreaking = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.considerGameMode"), ChestplateWarning.considerGameMode
                )
                .setTooltip(Component.translatable("config.considerGameMode.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(newValue -> newValue
                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("config.off").withStyle(ChatFormatting.RED)
                )
                .setSaveConsumer((newValue) -> ChestplateWarning.considerGameMode = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("config.considerAdvancement"), ChestplateWarning.considerAdvancement
                )
                .setTooltip(Component.translatable("config.considerAdvancement.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(newValue -> newValue
                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("config.off").withStyle(ChatFormatting.RED)
                )
                .setSaveConsumer((newValue) -> ChestplateWarning.considerAdvancement = newValue)
                .build());


        List<AbstractConfigListEntry> worldEntries = new ArrayList<>();
        Path savesDir = FabricLoader.getInstance().getGameDir().resolve("saves");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    String worldName = path.getFileName().toString();
                    String key = ChestplateWarning.key(worldName);
                    ChestplateWarning.worldToggles.putIfAbsent(key, true);

                    worldEntries.add(entryBuilder
                            .startBooleanToggle(Component.literal(worldName), ChestplateWarning.worldToggles.get(key))
                            .setDefaultValue(true)
                            .setYesNoTextSupplier(newValue -> newValue
                                    ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                                    : Component.translatable("config.off").withStyle(ChatFormatting.RED))
                            .setSaveConsumer(newValue -> ChestplateWarning.worldToggles.put(key, newValue))
                            .build());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!worldEntries.isEmpty()) {
            general.addEntry(entryBuilder
                    .startSubCategory(Component.translatable("config.worlds"), worldEntries)
                    .setExpanded(false)
                    .build());
        }


        List<AbstractConfigListEntry> serverEntries = new ArrayList<>();
        Path serversDat = FabricLoader.getInstance().getGameDir().resolve("servers.dat");

        if (Files.exists(serversDat)) {
            try {
                CompoundTag root = NbtIo.read(serversDat);

                if (root != null && root.contains("servers")) {
                    ListTag servers = (ListTag) root.get("servers");

                    for (int i = 0; i < servers.size(); i++) {
                        CompoundTag srv = (CompoundTag) servers.get(i);
                        String name = srv.getString("name").orElse("Unnamed");
                        String ip   = srv.getString("ip").orElse("0.0.0.0");

                        String key = ChestplateWarning.key(name + "_" + ip);
                        ChestplateWarning.serverToggles.putIfAbsent(key, true);

                        serverEntries.add(entryBuilder
                                .startBooleanToggle(Component.literal(name + " (" + ip + ")"),
                                        ChestplateWarning.serverToggles.get(key))
                                .setDefaultValue(true)
                                .setYesNoTextSupplier(newValue -> newValue
                                        ? Component.translatable("config.on").withStyle(ChatFormatting.GREEN)
                                        : Component.translatable("config.off").withStyle(ChatFormatting.RED))
                                .setSaveConsumer(newValue -> ChestplateWarning.serverToggles.put(key, newValue))
                                .build());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!serverEntries.isEmpty()) {
            general.addEntry(entryBuilder
                    .startSubCategory(Component.translatable("config.servers"), serverEntries)
                    .setExpanded(false)
                    .build());
        }


        return builder.setSavingRunnable(() -> {
            ChestplateWarning.saveConfig(ChestplateWarning.CONFIG_FILE);
            ChestplateWarning.loadConfig(ChestplateWarning.CONFIG_FILE);
        }).build();
    }
}
