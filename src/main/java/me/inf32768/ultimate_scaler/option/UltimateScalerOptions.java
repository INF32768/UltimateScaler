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

/**
 * 本模组的配置类，集中了所有配置选项的定义和加载、读取配置文件的功能。
 */
public final class UltimateScalerOptions {
    //Don't let anyone instantiate this class
    private UltimateScalerOptions() {}

    /**
     * 配置实例，用于存储配置选项的值，运行时所有的配置选项的值都通过这个实例来访问和修改。
     */
    public static ConfigImpl config;

    /**
     * 配置文件路径，用于存储和读取配置文件。
     */
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ultimate_scaler.toml");

    /**
     * 配置文件版本，用于判断配置文件的版本是否与当前模组的版本兼容、是否需要更新。
     * 这一变量会被一并存入配置文件中。
     */
    public static final int CONFIG_VERSION = 3;

    /**
     * 所有配置选项的定义类，定义了所有配置选项的名称、类型和默认值。这样定义的好处是可以用 {@link Toml#to(Class)} 自动将配置文件转换为配置实例。
     * <p>
     * 有关配置选项的详细说明，请参考 Wiki 中的页面<a href="https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.zh">《配置全解》</a>。
     */
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
        // 类加载时尝试读取配置文件
        try {
            loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            UltimateScaler.LOGGER.error(e.getMessage());
        }
    }

    /**
     * 读取配置文件，并将读取到的配置选项的值赋值给 {@link #config} 实例。期间会检查并迁移旧版配置文件，并自动替换无效值。
     * @throws IOException 配置文件读取或写入时出错
     */
    public static void loadConfig() throws IOException {
        if (!CONFIG_PATH.toFile().exists()) {
            // 默认配置文件不存在
            if (Files.exists(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml"))) {
                // 存在旧版配置文件，迁移配置
                config = new Toml().read(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml").toFile()).to(ConfigImpl.class);
                Files.deleteIfExists(FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml"));
            }
            // 不存在任何配置文件，使用默认配置
            config = new ConfigImpl();
        } else {
            try {
                // 存在配置文件，尝试读取配置
                config = new Toml().read(CONFIG_PATH.toFile()).to(ConfigImpl.class);
            } catch (Exception e) {
                // 配置文件格式错误，由于不知道是哪里出错，因此只打印错误信息，并使用默认配置
                UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load config file, resetting to default values: {}", e.getMessage());
                config = new ConfigImpl();
                saveConfig();
                return;
            }
            // 执行到这，说明配置文件格式正确，接下来校验字段合法性
            try {
                for (Field entry : ConfigImpl.class.getFields()) {
                    if (entry.get(config) == null) {
                        // 字段值为空，使用默认值
                        UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load config entry, resetting to default value: {}", entry.getName());
                        entry.set(config, ConfigImpl.class.getField(entry.getName()).get(new ConfigImpl()));
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            try {
                // 确保配置文件中的方块 ID 有效
                Objects.requireNonNull(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceDefaultFluidBlock)));
                Objects.requireNonNull(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceUndergroundLavaBlock)));
            } catch (NullPointerException e) {
                // 方块 ID 无效，使用默认值
                UltimateScaler.LOGGER.error("[Ultimate Scaler] Failed to load block, resetting to default values: {}", e.getMessage());
                ConfigManager.writeEntry(CONFIG_PATH, "replaceDefaultFluidBlock", "minecraft:air", null);
                ConfigManager.writeEntry(CONFIG_PATH, "replaceUndergroundLavaBlock", "minecraft:air", null);
                config.replaceDefaultFluidBlock = "minecraft:air";
                config.replaceUndergroundLavaBlock = "minecraft:air";
            }
        }
    }

    /**
     * 将 {@link #config} 实例中的配置选项的值写入配置文件。若配置文件不存在，则会自动创建。
     * @throws IOException 配置文件读取或写入时出错
     * @see ConfigManager
     */
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

    /**
     * 定义“边境之地位置”选项的枚举类。
     */
    public enum FarLandsPos {
        BETA,
        RELEASE,
        DEFAULT,
        REMOVED,
        CUSTOM
    }
}