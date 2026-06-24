package xyz.inf32768.ultimatescaler;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMetadata {
    // 私有化构造方法，防止实例化
    private ModMetadata() {}

    /**
     * 是否安装了 Cloth Config 模组，用于决定是否加载配置界面。
     */
    public static final boolean IS_CLOTH_CONFIG_PRESENT;

    /**
     * 是否安装了 Fabric API 模组。
     * <p>
     * 本模组技术上可以脱离 Fabric API 运行，但仅提供最低限度的功能。
     */
    public static final boolean IS_FABRIC_API_PRESENT;

    /**
     * 模组的命名空间 ID
     */
    public static final String MOD_ID = "ultimate-scaler";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    /**
     * 日志记录器。
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {
        FabricLoader instance = FabricLoader.getInstance();
        IS_CLOTH_CONFIG_PRESENT = instance.isModLoaded("cloth-config2");
        IS_FABRIC_API_PRESENT = instance.isModLoaded("fabric-api");
    }

    /**
     * 将原始路径解析为带有此模组命名空间 ID 的资源路径。
     * @param path 原始字符串路径
     * @return 本模组 ID 下的资源路径
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
