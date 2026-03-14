package me.inf32768.ultimate_scaler.option;

import me.inf32768.ultimate_scaler.UltimateScaler;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;

/**
 * 配置文件热重载器，用于监听游戏内数据重载（包括进入世界或执行 /reload 命令），并重新加载配置文件。
 *
 * @see UltimateScalerOptions
 */
public class ConfigReloader implements SimpleSynchronousResourceReloadListener {
    /**
     * 监听器 ID。
     */
    private static final Identifier LISTENER_ID = Identifier.of("ultimate_scaler", "config_reloader");

    /**
     * 获取监听器 ID 的方法，这是 {@link SimpleSynchronousResourceReloadListener} 接口的要求。
     */
    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    /**
     * 重新加载配置文件的方法，在 {@link #init()} 方法中注册为监听器后，游戏内重载数据时会调用此方法重新加载配置文件。
     *
     * @param manager 资源管理器，（暂时）没用到
     * @throws RuntimeException 在配置文件加载失败（或其他少数情况）时抛出
     * @see UltimateScalerOptions#loadConfig()
     */
    @Override
    public void reload(ResourceManager manager) {
        try {
            UltimateScalerOptions.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UltimateScaler.LOGGER.info("[Ultimate Scaler] Config reloaded");
    }

    /**
     * 初始化方法，在游戏启动时调用，注册重载的监听器。
     *
     * @see UltimateScaler#onInitialize()
     */
    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConfigReloader());
    }
}
