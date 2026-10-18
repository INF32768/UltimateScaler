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
    // 配置实例，用于存储配置选项的值，运行时所有的配置选项的值都通过这个实例来访问和修改。
    private static Config config;

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
     * 读取配置文件，并将读取到的配置选项的值赋值给 {@link ConfigManager#getConfig()} 实例。期间会检查并迁移旧版配置文件，并自动替换无效值。
     *
     * @see Toml#to(Class)
     */
    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            // 默认配置文件不存在
            config = tryMigrateConfigV2(); // 尝试迁移 V2 配置文件
            if (config == null) {
                config = new Config(); // V2 配置文件迁移失败或不存在，使用默认配置
                return;
            } else ModMetadata.LOGGER.info("Config file has been automatically updated from V2"); // 迁移成功，打印日志
        } else {
            // 存在配置文件，尝试读取配置
            try {
                if (new Toml().read(CONFIG_FILE).getLong("CONFIG_VERSION") == 3)
                    config = tryMigrateConfigV3(null); // 检测到配置文件为 V3，尝试迁移到 V4
                if (config == null)
                    config = new Toml().read(CONFIG_FILE).to(Config.class); // 迁移失败或配置文件为 V4，直接读取（若读取失败抛出异常则会使用默认配置）
                else ModMetadata.LOGGER.info("Config file has been automatically updated from V3"); // 迁移成功，打印日志
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
                if (entry.get(config) == null) {
                    // 字段值为空，使用默认值
                    ModMetadata.LOGGER.warn("Failed to load config entry, resetting to default value: {}", entry.getName());
                    entry.set(config, Config.class.getField(entry.getName()).get(new Config()));
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            // 确保配置文件中的方块 ID 有效
            if (!config.worldGen.fluidReplace.defaultFluid.isEmpty())
                Objects.requireNonNull(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.defaultFluid)));
            if (!config.worldGen.fluidReplace.undergroundLava.isEmpty())
                Objects.requireNonNull(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.undergroundLava)));
        } catch (NullPointerException e) {
            // 方块 ID 无效，使用默认值
            ModMetadata.LOGGER.warn("Invalid block id, resetting to default values", e);
            config.worldGen.fluidReplace.defaultFluid = "";
            config.worldGen.fluidReplace.undergroundLava = "";
        }
    }

    /**
     * 尝试迁移旧版（V2）配置文件至最新版。会自动检查旧版配置文件是否存在，若存在则自动迁移。
     *
     * @return 迁移完成的配置文件。若不存在旧版配置文件则为 null。
     */
    @SuppressWarnings("deprecation")
    private static Config tryMigrateConfigV2() {
        final File configFileV2 = FabricLoader.getInstance().getConfigDir().resolve("ultimatescaler.toml").toFile();

        if (!configFileV2.exists()) return null;

        Config.ConfigImpl configV3 = new Toml().read(configFileV2).to(Config.ConfigImpl.class);
        try {
            Files.deleteIfExists(configFileV2.toPath());
        } catch (IOException ignored) {
        } // 文件删不掉，不管它

        return tryMigrateConfigV3(configV3);
    }

    /**
     * 尝试迁移旧版（V3）配置文件至最新版。
     *
     * @param configV3 可选的 V2 配置实例。若为 null，则尝试从配置文件中读取。
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
        configV4.worldGen.farLandsControl.farLandsPos = configV3.farLandsPos;
        configV4.worldGen.farLandsControl.limitReturnValue = configV3.limitReturnValue;
        configV4.worldGen.farLandsControl.customDivisor = configV3.maintainPrecisionCustomDivisor;
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
