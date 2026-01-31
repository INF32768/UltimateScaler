package me.inf32768.ultimate_scaler.option;

import me.inf32768.ultimate_scaler.UltimateScaler;
import me.inf32768.ultimate_scaler.util.RegistryAccessor;
import me.inf32768.ultimate_scaler.util.VersionHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public final class UltimateScalerOptions {
    //Don't let anyone instantiate this class
    private UltimateScalerOptions() {}

    public static ConfigImpl config;
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ultimate_scaler.toml");
    public static final int CONFIG_VERSION = 3;

    public static class ConfigImpl {
        public BigDecimal[] globalBigDecimalOffset = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        public BigDecimal[] globalBigDecimalScale = {BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE};
        public int optionMenuKeyCode = GLFW.GLFW_KEY_U;
        public short optionMenuModifierValue = 2;
        public boolean showTerrainPos = true;
        public FarLandsPos farLandsPos = FarLandsPos.DEFAULT;
        public double maintainPrecisionCustomDivisor = 33554432;
        public boolean limitReturnValue = false;
        public int maxNoiseLogarithmValue = 7;
        public boolean extraYOffset = false;
        public boolean bigIntegerRewrite = false;
        public boolean fixEndRings = false;
        public boolean fixChunkGenerationOutOfBound = true;
        public boolean expandDatapackValueRange = true;
        public boolean expandWorldBorder = true;
        public boolean fixMineshaftCannotGenerate = true;
        public boolean replaceDefaultFluid = false;
        public String replaceDefaultFluidBlock = "minecraft:air";
        public boolean replaceUndergroundLava = false;
        public String replaceUndergroundLavaBlock = "minecraft:air";
        public boolean publicTerrainPos = true;
    }

    static {
        try {
            loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            UltimateScaler.LOGGER.error(e.getMessage());
        }
    }

    public static void loadConfig() throws IOException {
        if (!CONFIG_PATH.toFile().exists()) {
            if (Files.exists(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml"))) {
                config = new Toml().read(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml").toFile()).to(ConfigImpl.class);
                Files.deleteIfExists(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml"));
            }
            config = new ConfigImpl();
        } else {
            try {
                config = new Toml().read(CONFIG_PATH.toFile()).to(ConfigImpl.class);
            } catch (Exception e) {
                UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load config file, resetting to default values: {}", e.getMessage());
                config = new ConfigImpl();
                saveConfig();
                return;
            }
            try {
                for (Field entry : ConfigImpl.class.getFields()) {
                    if (entry.get(config) == null) {
                        UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load config entry, resetting to default value: {}", entry.getName());
                        entry.set(config, ConfigImpl.class.getField(entry.getName()).get(new ConfigImpl()));
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            try {
                Objects.requireNonNull(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceDefaultFluidBlock)));
                Objects.requireNonNull(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceUndergroundLavaBlock)));
            } catch (NullPointerException e) {
                UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load block, resetting to default values: {}", e.getMessage());
                ConfigManager.writeEntry(CONFIG_PATH, "replaceDefaultFluidBlock", "minecraft:air", null);
                ConfigManager.writeEntry(CONFIG_PATH, "replaceUndergroundLavaBlock", "minecraft:air", null);
                config.replaceDefaultFluidBlock = "minecraft:air";
                config.replaceUndergroundLavaBlock = "minecraft:air";
            }
        }
    }

    public static void saveConfig() throws IOException {
        if (!CONFIG_PATH.toFile().exists()) {
            if (config == null) {
                loadConfig();
            }
            Files.createFile(CONFIG_PATH);
            UltimateScaler.LOGGER.info("[Ultimate Scaler] Created new config file at {}", CONFIG_PATH);
        }
        ConfigManager.writeEntry(CONFIG_PATH, "CONFIG_VERSION", CONFIG_VERSION, new String[] {Text.translatable("ultimate_scaler.config.version_comment").getString()});
        ConfigManager.writeArrayEntry(CONFIG_PATH, "globalBigDecimalOffset", Arrays.stream(config.globalBigDecimalOffset).map(BigDecimal::toString).toList(), new String[] {Text.translatable("ultimate_scaler.options.worldgen.offset.globalOffset").getString(), Text.translatable("ultimate_scaler.options.parsableDecimal.tooltip").getString()});
        ConfigManager.writeArrayEntry(CONFIG_PATH, "globalBigDecimalScale", Arrays.stream(config.globalBigDecimalScale).map(BigDecimal::toString).toList(), new String[] {Text.translatable("ultimate_scaler.options.worldgen.offset.globalScale").getString(), Text.translatable("ultimate_scaler.options.parsableDecimal.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "optionMenuKeyCode", config.optionMenuKeyCode, new String[] {Text.translatable("ultimate_scaler.options.general.optionMenuKey").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "optionMenuModifierValue", config.optionMenuModifierValue, new String[]{});
        ConfigManager.writeEntry(CONFIG_PATH, "showTerrainPos", config.showTerrainPos, new String[] {Text.translatable("ultimate_scaler.options.general.showTerrainPos").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "farLandsPos", config.farLandsPos.name(), new String[] {
                Text.translatable("ultimate_scaler.options.worldgen.farLandsPos").getString(),
                "BETA : " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.BETA").getString() + ", " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.BETA.tooltip").getString(),
                "RELEASE : " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.RELEASE").getString() + ", " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.RELEASE.tooltip").getString(),
                "DEFAULT : " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.DEFAULT").getString() + ", " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.DEFAULT.tooltip").getString(),
                "Removed : " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.REMOVED").getString() + ", " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.REMOVED.tooltip").getString(),
                "CUSTOM : " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.CUSTOM").getString() + ", " + Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos.CUSTOM.tooltip").getString()
        });
        ConfigManager.writeEntry(CONFIG_PATH, "maintainPrecisionCustomDivisor", config.maintainPrecisionCustomDivisor, new String[] {Text.translatable("ultimate_scaler.options.worldgen.maintainPrecisionCustomDivisor").getString(), Text.translatable("ultimate_scaler.options.worldgen.maintainPrecisionCustomDivisor.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "limitReturnValue", config.limitReturnValue, new String[] {Text.translatable("ultimate_scaler.options.worldgen.limitReturnValue").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "maxNoiseLogarithmValue", config.maxNoiseLogarithmValue, new String[] {Text.translatable("ultimate_scaler.options.worldgen.maxNoiseLogarithmValue").getString(), Text.translatable("ultimate_scaler.options.worldgen.maxNoiseLogarithmValue.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "replaceDefaultFluid", config.replaceDefaultFluid, new String[] {Text.translatable("ultimate_scaler.options.worldgen.replaceDefaultFluid").getString(), Text.translatable("ultimate_scaler.options.worldgen.replaceDefaultFluid.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "replaceDefaultFluidBlock", config.replaceDefaultFluidBlock, new String[] {});
        ConfigManager.writeEntry(CONFIG_PATH, "replaceUndergroundLava", config.replaceUndergroundLava, new String[] {Text.translatable("ultimate_scaler.options.worldgen.replaceUndergroundLava").getString(), Text.translatable("ultimate_scaler.options.worldgen.replaceUndergroundLava.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "replaceUndergroundLavaBlock", config.replaceUndergroundLavaBlock, new String[] {});
        ConfigManager.writeEntry(CONFIG_PATH, "extraYOffset", config.extraYOffset, new String[] {Text.translatable("ultimate_scaler.options.worldgen.extraYOffset").getString(), Text.translatable("ultimate_scaler.options.worldgen.extraYOffset.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "bigIntegerRewrite", config.bigIntegerRewrite, new String[] {Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite").getString(), Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.1").getString() + Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.2").getString() + Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.3").getString() + Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.4").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "fixEndRings", config.fixEndRings, new String[] {Text.translatable("ultimate_scaler.options.worldgen.fixEndRings").getString(), Text.translatable("ultimate_scaler.options.worldgen.fixEndRings.tooltip").getString()});
        if (VersionHelper.isVersionAtLeast("1.21.2")) {
            ConfigManager.writeEntry(CONFIG_PATH, "fixChunkGenerationOutOfBound", config.fixChunkGenerationOutOfBound, new String[] {Text.translatable("ultimate_scaler.options.tweaks.fixChunkGenerationOutOfBound").getString(), Text.translatable("ultimate_scaler.options.tweaks.fixChunkGenerationOutOfBound.tooltip").getString()});
        }
        ConfigManager.writeEntry(CONFIG_PATH, "expandWorldBorder", config.expandWorldBorder, new String[] {Text.translatable("ultimate_scaler.options.tweaks.expandWorldBorder").getString(), Text.translatable("ultimate_scaler.options.tweaks.expandWorldBorder.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "expandDatapackValueRange", config.expandDatapackValueRange, new String[] {Text.translatable("ultimate_scaler.options.tweaks.expandDatapackValueRange").getString(), Text.translatable("ultimate_scaler.options.tweaks.expandDatapackValueRange.tooltip").getString() + Text.translatable("ultimate_scaler.options.require_restart").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "fixMineshaftCannotGenerate", config.fixMineshaftCannotGenerate, new String[] {Text.translatable("ultimate_scaler.options.tweaks.fixMineshaftCannotGenerate").getString(), Text.translatable("ultimate_scaler.options.tweaks.fixMineshaftCannotGenerate.tooltip").getString()});
        ConfigManager.writeEntry(CONFIG_PATH, "publicTerrainPos", config.publicTerrainPos, new String[] {Text.translatable("ultimate_scaler.options.server.publicTerrainPos").getString()});
    }

    public enum FarLandsPos {
        BETA,
        RELEASE,
        DEFAULT,
        REMOVED,
        CUSTOM
    }
}