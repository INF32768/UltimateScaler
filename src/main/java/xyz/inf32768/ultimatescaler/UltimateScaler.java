package xyz.inf32768.ultimatescaler;

import net.fabricmc.api.ModInitializer;
import xyz.inf32768.ultimatescaler.commands.LocatePosition;
import xyz.inf32768.ultimatescaler.config.ConfigManager;
import xyz.inf32768.ultimatescaler.config.ConfigReloader;

/**
 * 通用主入口，负责初始化模组的客户端和服务端通用功能。以下是初始化顺序：
 * <ol>
 *     <li>加载配置文件</li>
 *     <li>注册配置重载监听器</li>
 *     <li>注册 <code>locate pos</code> 命令</li>
 * </ol>
 */
public class UltimateScaler implements ModInitializer {
    @Override
    public void onInitialize() {
        // 提前加载配置，并在首次启动时生成默认配置文件
        ConfigManager.getConfig();

        if (ModMetadata.IS_FABRIC_API_PRESENT) {
            ConfigReloader.init();
            LocatePosition.init();
        } else {
            ModMetadata.LOGGER.warn("Fabric API is not present, some core features may not work properly!");
        }
    }
}
