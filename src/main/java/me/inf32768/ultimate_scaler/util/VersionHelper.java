package me.inf32768.ultimate_scaler.util;

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Minecraft 版本号实用工具。提供版本号的比较、判断、解析等功能。
 */
@SuppressWarnings("unused")
public class VersionHelper {
    // 禁止实例化
    private VersionHelper() {}

    /**
     * 当前 Minecraft 版本号，格式为 "major.minor.patch-alpha.snapshot"。
     */
    public static final String CURRENT_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();

    /**
     * 判断当前版本号是否与指定版本号完全相同（精确到快照）。
     * @param version 要比较的版本号。
     * @return 两个版本号是否相同。
     */
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
        Integer[] version1Array = parseVersion(version1);
        Integer[] version2Array = parseVersion(version2);
        for (int i = 0; i < 4; i++) {
            int compare = Integer.compare(version1Array[i], version2Array[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    /**
     * 解析版本号字符串。
     * <p>
     * 将通过 {@code getFriendlyString()} 方法获取到的版本号字符串解析为 {@code {major, minor, patch, snapshot}} 格式的 {@code Integer} 数组，以便于比较。
     * <p>
     * 注意：对于正式版（原版本号字符串中没有 {@code -alpha} 部分），则数组的第四个元素将为 {@code 99}，这样可以使比较更方便。
     *
     * @param version 要解析的版本号字符串。
     * @return 解析后的版本号数组。
     */

    public static Integer[] parseVersion(String version) {
        ArrayList<Integer> split = new ArrayList<>(Stream.of(version.split("\\.|-alpha")).map(Integer::valueOf).toList());

        if (split.size() <= 3) {
            split.add(0);
        }

        if (split.size() <= 4) {
            split.add(99);
        }

        return split.toArray(new Integer[0]);
    }
}
