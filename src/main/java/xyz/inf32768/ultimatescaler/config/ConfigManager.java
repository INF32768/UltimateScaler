package xyz.inf32768.ultimatescaler.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import xyz.inf32768.ultimatescaler.ModMetadata;
import xyz.inf32768.ultimatescaler.versionutil.RegistryAccessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 本模组的配置管理类，集中了配置文件的获取、加载和读取的功能。
 * <p>
 * 基于 {@link Toml} 实现。
 *
 * @see Config
 * @since 0.4.0
 */
public class ConfigManager {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ultimate_scaler.toml").toFile();
    private static final File CONFIG_FILE_V2 = FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml").toFile();
    // 配置实例，用于存储配置选项的值，运行时所有的配置选项的值都通过这个实例来访问和修改。
    private static volatile Config config;

    /**
     * 可供外部使用的获取配置实例的方法，
     *
     * @return 配置实例，可用于获取当前配置选项的值
     */
    public static Config getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * 读取配置文件，并将读取到的配置选项的值赋值给 {@link ConfigManager#getConfig()} 实例。
     * 期间会检查并迁移旧版配置文件，并自动替换无效值。若配置文件不存在，则会生成默认配置文件。
     * <p>
     * 每次调用都会重新读取文件，因此可用于配置热重载。
     *
     * @see Toml#to(Class)
     */
    public static void loadConfig() {
        boolean migratedFromV2 = false;
        boolean migratedFromV3 = false;
        Config loadedConfig;

        if (!CONFIG_FILE.exists()) {
            // 默认配置文件不存在
            try {
                loadedConfig = tryMigrateConfigV2(); // 尝试迁移 V2 配置文件
            } catch (Exception e) {
                // 旧版配置文件格式错误，保留原文件并改用默认配置
                ModMetadata.LOGGER.warn("Failed to migrate V2 config file, using default values: ", e);
                loadedConfig = null;
            }
            if (loadedConfig == null) {
                // V2 配置文件迁移失败或不存在，使用默认配置
                config = new Config();
                try {
                    saveConfig(); // 首次启动时生成默认配置文件
                } catch (IOException e) {
                    ModMetadata.LOGGER.error("Failed to save config file", e);
                }
                return;
            }
            migratedFromV2 = true;
            ModMetadata.LOGGER.info("Config file has been automatically updated from V2"); // 迁移成功，打印日志
        } else {
            // 存在配置文件，尝试读取配置
            try {
                Toml toml = new Toml().read(CONFIG_FILE);
                Long configVersion = toml.getLong("CONFIG_VERSION");
                loadedConfig = null;
                if (configVersion != null && configVersion == 3) {
                    loadedConfig = tryMigrateConfigV3(null); // 检测到配置文件为 V3，尝试迁移到 V4
                    if (loadedConfig != null) {
                        migratedFromV3 = true;
                        ModMetadata.LOGGER.info("Config file has been automatically updated from V3"); // 迁移成功，打印日志
                    }
                }
                if (loadedConfig == null)
                    loadedConfig = toml.to(Config.class); // 迁移失败或配置文件为 V4，直接读取（若读取失败抛出异常则会使用默认配置）
            } catch (Exception e) {
                // 配置文件格式错误，由于不知道是哪里出错，因此只打印错误信息，并使用默认配置
                ModMetadata.LOGGER.warn("Failed to load config file, using default values: ", e);
                config = new Config();
                try {
                    saveConfig();
                } catch (IOException ex) {
                    ModMetadata.LOGGER.error("Failed to save config file", ex);
                }
                return;
            }
        }
        // 执行到这，配置文件格式已经正确，接下来校验字段合法性
        try {
            for (Field entry : Config.class.getFields()) {
                if (entry.get(loadedConfig) == null) {
                    // 字段值为空，使用默认值
                    ModMetadata.LOGGER.warn("Failed to load config entry, resetting to default value: {}", entry.getName());
                    entry.set(loadedConfig, Config.class.getField(entry.getName()).get(new Config()));
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            // 确保配置文件中的方块 ID 有效
            if (!loadedConfig.worldGen.fluidReplace.defaultFluid.isEmpty())
                Objects.requireNonNull(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(loadedConfig.worldGen.fluidReplace.defaultFluid)));
            if (!loadedConfig.worldGen.fluidReplace.undergroundLava.isEmpty())
                Objects.requireNonNull(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(loadedConfig.worldGen.fluidReplace.undergroundLava)));
        } catch (NullPointerException e) {
            // 方块 ID 无效，使用默认值
            ModMetadata.LOGGER.warn("Invalid block id, resetting to default values", e);
            loadedConfig.worldGen.fluidReplace.defaultFluid = "";
            loadedConfig.worldGen.fluidReplace.undergroundLava = "";
        }

        config = loadedConfig;

        if (migratedFromV2 || migratedFromV3) {
            try {
                // 迁移结果必须立即写回，否则下次启动时迁移会丢失
                saveConfig();
                if (migratedFromV2) Files.deleteIfExists(CONFIG_FILE_V2.toPath());
            } catch (IOException e) {
                // 写入失败时保留旧版文件，避免用户配置丢失
                ModMetadata.LOGGER.error("Failed to save migrated config file, the legacy file has been kept", e);
            }
        }
    }

    /**
     * 尝试迁移旧版（V2）配置文件至最新版。会自动检查旧版配置文件是否存在，若存在则读取并迁移。
     * <p>
     * 注意：旧版文件不会在这里删除，而是由 {@link #loadConfig()} 在迁移结果成功写入新配置文件后删除。
     *
     * @return 迁移完成的配置文件。若不存在旧版配置文件则为 null。
     */
    @SuppressWarnings("deprecation")
    private static Config tryMigrateConfigV2() {
        if (!CONFIG_FILE_V2.exists()) return null;

        Config.ConfigImpl configV3 = new Toml().read(CONFIG_FILE_V2).to(Config.ConfigImpl.class);
        return tryMigrateConfigV3(configV3);
    }

    /**
     * 尝试迁移旧版（V3）配置文件至最新版。会自动把已移除的 {@code CUSTOM} 选项转换为“{@code DEFAULT} + 自定义除数”。
     *
     * @param configV3 可选的 V3 配置实例。若为 null，则尝试从配置文件中读取。
     * @return 迁移完成的配置文件。若不存在旧版配置文件则为 null。
     */
    @SuppressWarnings("deprecation")
    private static Config tryMigrateConfigV3(Config.ConfigImpl configV3) {
        if (configV3 == null) {
            if (!Files.exists(CONFIG_FILE.toPath())) return null;
            configV3 = new Toml().read(CONFIG_FILE).to(Config.ConfigImpl.class);
        }
        Config configV4 = new Config();
        configV4.general.configScreenKeybind = getModifiedKeyCode(configV3.optionMenuKeyCode, configV3.optionMenuModifierValue);
        configV4.general.publicTerrainPos = configV3.publicTerrainPos;
        configV4.general.showTerrainPos = configV3.showTerrainPos;
        configV4.worldGen.farLandsControl.farLandsPos = switch (configV3.farLandsPos) {
            case BETA -> Config.FarLandsPos.BETA;
            case RELEASE -> Config.FarLandsPos.RELEASE;
            case REMOVED -> Config.FarLandsPos.REMOVED;
            case DEFAULT, CUSTOM -> Config.FarLandsPos.DEFAULT;
        };
        configV4.worldGen.farLandsControl.limitReturnValue = configV3.limitReturnValue;
        // 旧版只有 CUSTOM 会使用自定义除数，DEFAULT 使用固定值 33554432。
        // 为保证迁移前后行为一致，仅 CUSTOM 保留自定义除数，其余情况重置为默认值。
        configV4.worldGen.farLandsControl.customDivisor = configV3.farLandsPos == Config.ConfigImpl.LegacyFarLandsPos.CUSTOM
                ? configV3.maintainPrecisionCustomDivisor : 33554432;
        configV4.worldGen.farLandsControl.maxLogarithmValue = configV3.maxNoiseLogarithmValue;
        configV4.worldGen.reposition.highPrecisionMode = configV3.bigIntegerRewrite;
        configV4.worldGen.reposition.extendedYAxisEffect = configV3.extraYOffset;
        configV4.worldGen.reposition.shift = configV3.globalBigDecimalOffset;
        configV4.worldGen.reposition.scale = configV3.globalBigDecimalScale;
        configV4.worldGen.fluidReplace.defaultFluid = configV3.replaceDefaultFluid ? configV3.replaceDefaultFluidBlock : "";
        configV4.worldGen.fluidReplace.undergroundLava = configV3.replaceUndergroundLava ? configV3.replaceUndergroundLavaBlock : "";
        configV4.fixesAndExpansion.worldBorder = configV3.expandWorldBorder;
        configV4.fixesAndExpansion.chunkGenerationOutOfBound = configV3.fixChunkGenerationOutOfBound;
        configV4.fixesAndExpansion.endRings = configV3.fixEndRings;
        configV4.fixesAndExpansion.mineShaftCannotGenerate = configV3.fixMineshaftCannotGenerate;
        configV4.utilities.expandDatapackValueRange = configV3.expandDatapackValueRange;
        return configV4;
    }

    /**
     * 将 {@link ConfigManager#getConfig()} 实例中的配置选项的值写入配置文件，并在文件开头添加链接。若配置文件不存在，则会自动创建。
     *
     * @throws IOException 配置文件读取或写入时出错
     * @see TomlWriter#write(Object, File)
     */
    public static void saveConfig() throws IOException {
        if (!CONFIG_FILE.exists()) {
            Files.createFile(CONFIG_FILE.toPath());
            ModMetadata.LOGGER.info("Created new config file at {}", CONFIG_FILE.getAbsolutePath());
        }
        new TomlWriter().write(Objects.requireNonNull(config), CONFIG_FILE);

        // 在文件开头添加 Wiki 引导
        List<String> lines = Files.readAllLines(CONFIG_FILE.toPath());
        List<String> newLines = new ArrayList<>();
        newLines.add("# Check out our GitHub Wiki for detailed explanations of all the configuration options:\n# https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.en\n\n");
        newLines.addAll(lines);
        Files.write(CONFIG_FILE.toPath(), newLines);
    }

    /**
     * 将 GLFW 键控代码和组合键修饰代码合并成一个整数，也就是用一个 {@code int} 数据表示一个组合键，便于存储和传输。
     * <p>
     * 原理：组合键修饰代码固定长度为三位，因此可以将一个整数的低三位用来存组合键，高位来存键控代码。
     *
     * @param keyCode  GLFW 键控代码
     * @param modifier 组合键修饰代码
     * @return 打包的整数
     */
    public static int getModifiedKeyCode(int keyCode, short modifier) {
        return (keyCode << 3) | modifier & 0b111;
    }

    /**
     * 将合并的 GLFW 键控代码和组合键修饰代码还原出键控代码。
     *
     * @param modifiedKeyCode 打包的整数
     * @return 解包得到的 GLFW 键控代码
     * @see #getModifiedKeyCode
     */
    public static int getKeyCode(int modifiedKeyCode) {
        return modifiedKeyCode >> 3;
    }

    /**
     * 将合并的 GLFW 键控代码和组合键修饰代码还原出组合键修饰代码。
     *
     * @param modifiedKeyCode 打包的整数
     * @return 解包得到的组合键修饰代码
     * @see #getModifiedKeyCode
     */
    public static short getKeyModifier(int modifiedKeyCode) {
        return (short) (modifiedKeyCode & 0b111);
    }
}
