package me.inf32768.ultimate_scaler;

import me.inf32768.ultimate_scaler.commands.LocatePosition;
import me.inf32768.ultimate_scaler.option.ConfigReloader;
import me.inf32768.ultimate_scaler.option.KeyBindings;
import me.inf32768.ultimate_scaler.option.UltimateScalerOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 本模组的主类，负责初始化和存储常用变量。
 */
public class UltimateScaler implements ModInitializer {

    /**
     * 是否在客户端运行。
     * <p>
     * 本模组的一些功能仅适用于客户端，在服务端加载相关内容时会导致崩溃，如按键绑定、配置界面等。
     */
    public static final boolean IS_RUNNING_ON_CLIENT = FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);

    /**
     * 是否安装了 Cloth Config 模组，用于决定是否加载配置界面。
     */
    public static final boolean IS_CLOTH_CONFIG_PRESENT = FabricLoader.getInstance().isModLoaded("cloth-config2");

    /**
     * 是否安装了 Fabric API 模组。
     * <p>
     * 本模组技术上可以脱离 Fabric API 运行，但仅提供最低限度的功能。
     */
    public static final boolean IS_FABRIC_API_PRESENT = FabricLoader.getInstance().isModLoaded("fabric-api");

    /**
     * 日志记录器。
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("ultimate_scaler");

    /**
     * 主入口，负责初始化模组。以下是初始化顺序：
     * <ol>
     *     <li>注册按键绑定（仅客户端）</li>
     *     <li>加载配置文件（仅服务端）</li>
     *     <li>注册配置重载监听器</li>
     *     <li>注册 <code>locate pos</code> 命令</li>
     * </ol>
     * <p>
     * 注：客户端加载配置文件在加载主菜单时进行，详见 {@link me.inf32768.ultimate_scaler.mixins.MixinTitleScreen}。
     */
    @Override
    public void onInitialize() {
        if (IS_RUNNING_ON_CLIENT) {
            if (IS_FABRIC_API_PRESENT) {
                KeyBindings.init();
            }
        } else {
            // 执行到这说明模组运行在服务端
            try {
                UltimateScalerOptions.saveConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (IS_FABRIC_API_PRESENT) {
            ConfigReloader.init();
            LocatePosition.init();
        } else {
            LOGGER.warn("Fabric API is not present, some core features may not work properly!");
        }
    }
}