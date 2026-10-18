package xyz.inf32768.ultimatescaler.config;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import xyz.inf32768.ultimatescaler.UltimateScaler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 配置文件热重载器，用于监听游戏内数据重载（包括进入世界或执行 /reload 命令），并重新加载配置文件。
 *
 * @see Config
 */
public class ConfigReloader implements SimpleSynchronousResourceReloadListener {
    /**
     * 监听器 ID。
     */
    private static final ResourceLocation LISTENER_ID = ResourceLocation.fromNamespaceAndPath("ultimate_scaler", "config_reloader");

    /**
     * 初始化方法，在游戏启动时调用，注册重载的监听器。
     *
     * @see UltimateScaler#onInitialize()
     */
    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ConfigReloader());
    }

    /**
     * 获取监听器 ID 的方法，这是 {@link SimpleSynchronousResourceReloadListener} 接口的要求。
     */
    @Override
    public ResourceLocation getFabricId() {
        return LISTENER_ID;
    }

    /**
     * 重新加载配置文件的方法，在 {@link #init()} 方法中注册为监听器后，游戏内重载数据时会调用此方法重新加载配置文件。
     *
     * @throws RuntimeException 在配置文件加载失败（或其他少数情况）时抛出
     * @see ConfigManager#loadConfig()
     */
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        ConfigManager.loadConfig();
        return SimpleSynchronousResourceReloadListener.super.reload(preparationBarrier, resourceManager, executor, executor2);
    }

    @Override
    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
    }
}
