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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private static final String CONFIG_FILE_NAME = "ultimate_scaler.toml";
    private static final String CONFIG_FILE_NAME_V2 = "ultimatescaler.toml";
    private static final String CONFIG_BACKUP_SUFFIX = ".bak";
    private static final String WIKI_HEADER = "# Check out our GitHub Wiki for detailed explanations of all the configuration options:\n# https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.en\n\n";
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME).toFile();
    private static final File CONFIG_FILE_V2 = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME_V2).toFile();
    // 保护配置的加载与保存，避免两者并发执行时互相覆盖。
    private static final Object CONFIG_LOCK = new Object();
    // 配置实例，用于存储配置选项的值，运行时所有的配置选项的值都通过这个实例来访问和修改。
    private static volatile Config config;

    /**
     * 可供外部使用的获取配置实例的方法，
     *
     * @return 配置实例，可用于获取当前配置选项的值
     */
    public static Config getConfig() {
        Config current = config;
        if (current == null) {
            synchronized (CONFIG_LOCK) {
                current = config;
                if (current == null) {
                    loadConfigLocked();
                    current = config;
                }
            }
        }
        return current;
    }

    /**
     * 读取配置文件，并将读取到的配置选项的值赋值给 {@link ConfigManager#getConfig()} 实例。
     * 期间会检查并迁移旧版配置文件，并自动替换无效值。若配置文件不存在，则会生成默认配置文件。
     * <p>
     * 只有在新配置被完整读取并校验成功后，才会替换当前正在使用的配置。若加载失败，会优先继续使用
     * 重载前的配置；只有在之前没有成功加载过配置（例如启动时的第一次加载）时，才使用默认配置。
     * <p>
     * 每次调用都会重新读取文件，因此可用于配置热重载。
     *
     * @see Toml#to(Class)
     */
    public static void loadConfig() {
        synchronized (CONFIG_LOCK) {
            loadConfigLocked();
        }
    }

    /**
     * 在持有 {@link #CONFIG_LOCK} 的情况下执行实际的加载流程。
     */
    private static void loadConfigLocked() {
        Config loadedConfig = null;
        boolean migratedFromV2 = false;
        boolean migratedFromV3 = false;

        try {
            if (CONFIG_FILE.exists()) {
                // 存在配置文件，尝试读取配置
                Toml toml = new Toml().read(CONFIG_FILE);
                Long configVersion = toml.getLong("CONFIG_VERSION");
                if (configVersion != null && configVersion == 3) {
                    loadedConfig = tryMigrateConfigV3(null); // 检测到配置文件为 V3，尝试迁移到 V4
                    if (loadedConfig != null) {
                        migratedFromV3 = true;
                        ModMetadata.LOGGER.info("Config file has been automatically updated from V3"); // 迁移成功，打印日志
                    }
                }
                if (loadedConfig == null) {
                    loadedConfig = toml.to(Config.class); // 迁移失败或配置文件为 V4，直接读取
                }
            } else {
                // 默认配置文件不存在，尝试迁移 V2 配置文件
                loadedConfig = tryMigrateConfigV2();
                if (loadedConfig != null) {
                    migratedFromV2 = true;
                    ModMetadata.LOGGER.info("Config file has been automatically updated from V2"); // 迁移成功，打印日志
                }
            }

            if (loadedConfig == null) {
                // 配置文件不存在，且没有可迁移的旧版配置
                fallbackToPreviousOrDefault("Config file does not exist", null);
                return;
            }

            validateConfig(loadedConfig);
        } catch (Exception e) {
            // 读取或迁移失败。不要立即使用默认配置，而是优先沿用重载前的配置。
            fallbackToPreviousOrDefault("Failed to load config file", e);
            return;
        }

        // 加载和校验全部成功，此时才替换当前正在使用的配置，避免失败时覆盖原有配置。
        config = loadedConfig;

        if (migratedFromV2 || migratedFromV3) {
            boolean saved = false;
            try {
                // 迁移结果必须立即写回，否则下次启动时迁移会丢失
                saveConfig(loadedConfig);
                saved = true;
            } catch (IOException | RuntimeException e) {
                // 写入失败时保留旧版文件，避免用户配置丢失
                ModMetadata.LOGGER.error("Failed to save migrated config file, the legacy file has been kept", e);
            }
            if (saved && migratedFromV2) {
                try {
                    Files.deleteIfExists(CONFIG_FILE_V2.toPath());
                } catch (IOException | RuntimeException e) {
                    ModMetadata.LOGGER.warn("Failed to delete the legacy V2 config file", e);
                }
            }
        }
    }

    /**
     * 加载失败时的兜底逻辑：优先沿用重载前的配置；若此前没有成功加载过配置，才使用默认配置。
     * <p>
     * 兜底配置会写回磁盘，以便修复损坏的配置文件。写入前会先备份原文件，避免用户配置彻底丢失。
     *
     * @param reason 失败原因
     * @param cause  导致失败的异常，若并非异常导致则为 {@code null}
     */
    private static void fallbackToPreviousOrDefault(String reason, Exception cause) {
        Config fallback = config;
        if (fallback == null) {
            if (cause == null) {
                ModMetadata.LOGGER.info(reason + "; using default values");
            } else {
                ModMetadata.LOGGER.warn(reason + "; no previous configuration is available, using default values", cause);
            }
            fallback = new Config();
        } else if (cause == null) {
            ModMetadata.LOGGER.warn(reason + "; keeping the previously loaded configuration");
        } else {
            ModMetadata.LOGGER.warn(reason + "; keeping the previously loaded configuration", cause);
        }

        // 覆盖原文件前先备份；备份失败时不覆盖，以免造成不可逆的配置丢失。
        if (!backupConfigFile()) {
            ModMetadata.LOGGER.error("The config file has not been overwritten because it could not be backed up");
            config = fallback;
            return;
        }

        config = fallback;
        try {
            saveConfig(fallback);
        } catch (IOException | RuntimeException e) {
            ModMetadata.LOGGER.error("Failed to save the fallback config file", e);
        }
    }

    /**
     * 在覆盖现有配置文件前创建备份。
     *
     * @return 备份成功，或原文件不存在、无需备份时返回 {@code true}；备份失败时返回 {@code false}
     */
    private static boolean backupConfigFile() {
        Path source = CONFIG_FILE.toPath();
        Path backup = source.resolveSibling(source.getFileName().toString() + CONFIG_BACKUP_SUFFIX);
        try {
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            ModMetadata.LOGGER.warn("Backed up the existing config file to {}", backup);
            return true;
        } catch (NoSuchFileException e) {
            // 原文件不存在，无需备份
            return true;
        } catch (IOException | RuntimeException e) {
            ModMetadata.LOGGER.error("Failed to back up the config file", e);
            return false;
        }
    }

    /**
     * 校验并修正加载得到的配置，自动把无效值替换为默认值。
     *
     * @param loadedConfig 刚读取到、尚未生效的配置
     */
    private static void validateConfig(Config loadedConfig) {
        try {
            Config defaults = new Config();
            for (Field entry : Config.class.getFields()) {
                if (entry.get(loadedConfig) == null) {
                    // 字段值为空，使用默认值
                    ModMetadata.LOGGER.warn("Failed to load config entry, resetting to default value: {}", entry.getName());
                    entry.set(loadedConfig, entry.get(defaults));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to validate config entries", e);
        }

        // 确保配置文件中的方块 ID 有效
        loadedConfig.worldGen.fluidReplace.defaultFluid = validateBlockId("default fluid", loadedConfig.worldGen.fluidReplace.defaultFluid);
        loadedConfig.worldGen.fluidReplace.undergroundLava = validateBlockId("underground lava", loadedConfig.worldGen.fluidReplace.undergroundLava);
    }

    /**
     * 校验方块 ID。空字符串表示不替换，视为有效。
     *
     * @param name    配置项名称，用于日志
     * @param blockId 方块 ID
     * @return 有效的方块 ID；无效时返回空字符串
     */
    private static String validateBlockId(String name, String blockId) {
        if (blockId == null || blockId.isEmpty()) return "";

        ResourceLocation id;
        try {
            id = ResourceLocation.parse(blockId);
        } catch (RuntimeException e) {
            ModMetadata.LOGGER.warn("Invalid block id '{}' for {}, resetting to default value", blockId, name, e);
            return "";
        }
        if (RegistryAccessor.get(BuiltInRegistries.BLOCK, id) == null) {
            ModMetadata.LOGGER.warn("Invalid block id '{}' for {}, resetting to default value", blockId, name);
            return "";
        }
        return blockId;
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
     * <p>
     * 写入采用“临时文件 + 替换”的方式，避免写入中途出错导致原有配置文件被截断。
     *
     * @throws IOException 配置文件读取或写入时出错
     * @see TomlWriter#write(Object, File)
     */
    public static void saveConfig() throws IOException {
        synchronized (CONFIG_LOCK) {
            saveConfig(Objects.requireNonNull(config, "Config has not been loaded yet"));
        }
    }

    /**
     * 将指定配置实例写入配置文件。
     *
     * @param configToSave 要写入的配置实例
     * @throws IOException 配置文件读取或写入时出错
     */
    private static void saveConfig(Config configToSave) throws IOException {
        Objects.requireNonNull(configToSave, "config");

        Path target = CONFIG_FILE.toPath();
        Path directory = target.toAbsolutePath().getParent();
        if (directory == null) directory = Path.of(".");
        Files.createDirectories(directory);

        boolean existed = Files.exists(target);
        Path temporary = Files.createTempFile(directory, "ultimate_scaler-", ".toml.tmp");
        try {
            new TomlWriter().write(configToSave, temporary.toFile());

            // 在文件开头添加 Wiki 引导
            List<String> lines = Files.readAllLines(temporary);
            List<String> newLines = new ArrayList<>(lines.size() + 1);
            newLines.add(WIKI_HEADER);
            newLines.addAll(lines);
            Files.write(temporary, newLines);

            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException | UnsupportedOperationException e) {
                // 部分文件系统（例如某些 Android 环境）不支持原子移动或原子替换，退化为普通替换
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException e) {
                ModMetadata.LOGGER.warn("Failed to delete temporary config file {}", temporary, e);
            }
        }

        if (!existed) {
            ModMetadata.LOGGER.info("Created new config file at {}", CONFIG_FILE.getAbsolutePath());
        }
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
