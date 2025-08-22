package net.kakos1220.chestplatewarning;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                .setTitle(Text.translatable("config.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));


        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.isModDisabled"), ChestplateWarning.isModDisabled
                )
                .setDefaultValue(false)
                .setSaveConsumer((value) -> ChestplateWarning.isModDisabled = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.isFlashingOn"), ChestplateWarning.isFlashingOn
                )
                .setDefaultValue(true)
                .setYesNoTextSupplier(value -> value
                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                        : Text.translatable("config.off").formatted(Formatting.RED)
                )
                .setSaveConsumer((value) -> ChestplateWarning.isFlashingOn = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.isDamageWarningOn"), ChestplateWarning.isDamageWarningOn
                )
                .setTooltip(Text.translatable("config.isDamageWarningOn.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(value -> value
                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                        : Text.translatable("config.off").formatted(Formatting.RED)
                )
                .setSaveConsumer((value) -> ChestplateWarning.isDamageWarningOn = value)
                .build());

        general.addEntry(entryBuilder
                .startIntSlider(
                        Text.translatable("config.elytraDurabilityThreshold"), ChestplateWarning.elytraDurabilityThreshold, 5, 50
                )
                .setTooltip(Text.translatable("config.elytraDurabilityThreshold.tooltip"))
                .setDefaultValue(10)
                .setTextGetter(value -> Text.literal(value + "%"))
                .setSaveConsumer((value) -> ChestplateWarning.elytraDurabilityThreshold = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.considerUnbreaking"), ChestplateWarning.considerUnbreaking
                )
                .setDefaultValue(true)
                .setYesNoTextSupplier(value -> value
                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                        : Text.translatable("config.off").formatted(Formatting.RED)
                )
                .setSaveConsumer((value) -> ChestplateWarning.considerUnbreaking = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.considerGameMode"), ChestplateWarning.considerGameMode
                )
                .setTooltip(Text.translatable("config.considerGameMode.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(value -> value
                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                        : Text.translatable("config.off").formatted(Formatting.RED)
                )
                .setSaveConsumer((value) -> ChestplateWarning.considerGameMode = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.considerAdvancement"), ChestplateWarning.considerAdvancement
                )
                .setTooltip(Text.translatable("config.considerAdvancement.tooltip"))
                .setDefaultValue(true)
                .setYesNoTextSupplier(value -> value
                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                        : Text.translatable("config.off").formatted(Formatting.RED)
                )
                .setSaveConsumer((value) -> ChestplateWarning.considerAdvancement = value)
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
                            .startBooleanToggle(Text.literal(worldName), ChestplateWarning.worldToggles.get(key))
                            .setDefaultValue(true)
                            .setYesNoTextSupplier(value -> value
                                    ? Text.translatable("config.on").formatted(Formatting.GREEN)
                                    : Text.translatable("config.off").formatted(Formatting.RED))
                            .setSaveConsumer(value -> ChestplateWarning.worldToggles.put(key, value))
                            .build());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!worldEntries.isEmpty()) {
            general.addEntry(entryBuilder
                    .startSubCategory(Text.translatable("config.worlds"), worldEntries)
                    .setExpanded(false)
                    .build());
        }


        List<AbstractConfigListEntry> serverEntries = new ArrayList<>();
        Path serversDat = FabricLoader.getInstance().getGameDir().resolve("servers.dat");

        if (Files.exists(serversDat)) {
            try {
                NbtCompound root = NbtIo.read(serversDat);

                if (root != null && root.contains("servers")) {
                    NbtList servers = (NbtList) root.get("servers");

                    for (int i = 0; i < servers.size(); i++) {
                        NbtCompound srv = (NbtCompound) servers.get(i);
                        String name = srv.getString("name").orElse("Unnamed");
                        String ip   = srv.getString("ip").orElse("0.0.0.0");

                        String key = ChestplateWarning.key(name + "_" + ip);
                        ChestplateWarning.serverToggles.putIfAbsent(key, true);

                        serverEntries.add(entryBuilder
                                .startBooleanToggle(Text.literal(name + " (" + ip + ")"),
                                        ChestplateWarning.serverToggles.get(key))
                                .setDefaultValue(true)
                                .setYesNoTextSupplier(value -> value
                                        ? Text.translatable("config.on").formatted(Formatting.GREEN)
                                        : Text.translatable("config.off").formatted(Formatting.RED))
                                .setSaveConsumer(value -> ChestplateWarning.serverToggles.put(key, value))
                                .build());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!serverEntries.isEmpty()) {
            general.addEntry(entryBuilder
                    .startSubCategory(Text.translatable("config.servers"), serverEntries)
                    .setExpanded(false)
                    .build());
        }


        return builder.setSavingRunnable(() -> {
            ChestplateWarning.saveConfig(ChestplateWarning.CONFIG_FILE);
            ChestplateWarning.loadConfig(ChestplateWarning.CONFIG_FILE);
        }).build();
    }
}
