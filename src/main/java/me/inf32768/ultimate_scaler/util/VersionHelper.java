// 这是我们 Ultimate Scaler 质量最高的实用工具类（五
package me.inf32768.ultimate_scaler.util;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Minecraft 版本号实用工具。提供版本号的比较、判断、解析等功能。
 */
// TODO: 比较精确到快照
@SuppressWarnings("unused")
public class VersionHelper {
    // 禁止实例化
    private VersionHelper() {}

    /**
     * 当前 Minecraft 版本号，格式为 "major.minor.patch-alpha.snapshot"。
     */
    public static final String CURRENT_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();

    /**
     * 判断当前版本号是否与指定版本号相同。
     * @param version 要比较的版本号。
     * @return 两个版本号是否相同。
     */
    // FIXME: 快照版本号无法判断
    public static boolean isVersion(String version) {
        return CURRENT_VERSION.equals(version);
    }

    /**
     * 判断当前版本号是否大于或等于指定版本号（与当前版本相同或更新）。
     * @param version 要比较的版本号。
     * @return 当前版本号是否大于或等于指定版本号。
     */
    public static boolean isVersionAtLeast(String version) {
        return isVersionAtLeast(CURRENT_VERSION, version);
    }

    /**
     * 判断一个版本号是否大于或等于另一个版本号。
     * @param compareVersion 第一个要比较的版本号。
     * @param version 第二个要比较的版本号。
     * @return 第一个版本号是否大于或等于第二个版本号。
     */
    public static boolean isVersionAtLeast(String compareVersion, String version) {
        return compareVersions(compareVersion, version) >= 0;
    }

    /**
     * 判断一个版本号是否小于另一个版本号。
     * @param version 第一个要比较的版本号。
     * @param compareVersion 第二个要比较的版本号。
     * @return 第一个版本号是否小于第二个版本号。
     */
    // FIXME: 这个方法通过颠倒参数顺序来实现与上一个方法相反的功能，这很具有误导性。
    public static boolean isVersionBelow(String version, String compareVersion) {
        return compareVersions(compareVersion, version) > 0;
    }

    /**
     * 判断当前版本号是否小于指定版本号（与当前版本相同或更旧）。
     * @param version 要比较的版本号。
     * @return 当前版本号是否小于指定版本号。
     */
    // FIXME: 这个方法的功能与反转后的 isVersionAtLeast 相同，不必要重复实现。
    public static boolean isVersionBelow(String version) {
        return isVersionBelow(CURRENT_VERSION, version);
    }

    /**
     * 判断当前版本号是否在指定范围内。
     * @param minVersion 最小版本号。
     * @param maxVersion 最大版本号。
     * @return 当前版本号是否在指定范围内。
     */
    public static boolean isVersionInRange(String minVersion, String maxVersion) {
        return isVersionInRange(minVersion, maxVersion, CURRENT_VERSION);
    }

    /**
     * 判断指定版本号是否在指定范围内（大于等于 {@code minVersion} 且小于等于 {@code maxVersion}）。
     * @param minVersion 最小版本号。
     * @param maxVersion 最大版本号。
     * @param compareVersion 要比较的版本号。
     * @return 指定版本号是否在指定范围内。
     */
    public static boolean isVersionInRange(String minVersion, String maxVersion,String compareVersion) {
        return compareVersions(minVersion, compareVersion) <= 0 && compareVersions(compareVersion, maxVersion) <= 0;
    }

    /**
     * 比较两个版本号的大小（新旧）。
     * @param version1 第一个版本号。
     * @param version2 第二个版本号。
     * @return 如果 {@code version1} 版本号比 {@code version2} 版本号新，返回正数；如果 {@code version1} 版本号和 {@code version2} 版本号相同，返回 0；如果 {@code version1} 版本号比 {@code version2} 版本号旧，返回负数。
     */
    public static int compareVersions(String version1, String version2) {
        String[] version1Array = parseVersion(version1);
        String[] version2Array = parseVersion(version2);
        for (int i = 0; i < 3; i++) {
            int compare = Integer.compare(Integer.parseInt(version1Array[i]), Integer.parseInt(version2Array[i]));
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    /**
     * 解析版本号字符串。
     * <p>
     * 将通过 {@code getFriendlyString()} 方法获取到的版本号字符串解析为 {@code {major, minor, patch}} 格式的字符串数组，以便于比较。
     * <p>
     * <strong>注意：快照（alpha）部分会被舍去</strong>
     * @param version 要解析的版本号字符串。
     * @return 解析后的版本号数组。
     */
    // FIXME: 为什么不用整数数组
    public static String[] parseVersion(String version) {
        String[] preSplit = version.split("-alpha");
        String[] split = preSplit[0].split("\\.");
        if (split.length == 2) {
            String[] parsed = new String[3];
            parsed[0] = split[0];
            parsed[1] = split[1];
            parsed[2] = "0";
            return parsed;
        }
        return split;
    }
}
