package xyz.inf32768.ultimatescaler.versionutil;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class VersionUtil {
    private VersionUtil() {}

    /**
     * 当前环境中的 Minecraft 版本号
     */
    public static final Version CURRENT_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion();

    /**
     * 当前环境中的标识符映射名（{@code named} 或 {@code intermediary}）
     */
    public static final String CURRENT_RUNTIME_NAMESPACE = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();

    /**
     * 将字符串解析为 {@link Version} 对象。
     * <p>
     * 只是包装了 {@link Version#parse(String)}，但是若解析出错则直接抛 {@code RuntimeException}，某些情况下避免了 try/catch 的麻烦。
     * @param version 版本的字符串表示
     * @return 解析的版本
     * @throws RuntimeException 如果解析出错抛出 {@link VersionParsingException}
     */
    public static Version parse(String version) {
        try {
            return Version.parse(version);
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断当前版本号是否大于或等于指定版本号（与当前版本相同或更新）。
     * @param version 要比较的版本号
     * @return 当前版本号是否大于或等于指定版本号
     */
    public static boolean isVersionAtLeast(Version version) {
        return CURRENT_VERSION.compareTo(version) >= 0;
    }
}
