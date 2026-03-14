/*
* 感谢DeepSeek开源
*/
package me.inf32768.ultimate_scaler.option;

import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 配置文件管理器，用于向配置文件内写入、修改、删除和读取单个字段。带有注释管理功能。
 */
@SuppressWarnings("unused")
public class ConfigManager {
    private static final String COMMENT_PREFIX = "# ";
    private static final String ARRAY_START = "[";
    private static final String ARRAY_END = "]";
    private static final String EQ = " = ";

    /* ========== 写入/修改操作 ========== */

    /**
     * 向配置文件写入一个配置项。
     *<p>
     * 此方法将指定的键值对写入 TOML 配置文件。值可以是基本类型、字符串或字符串数组。
     *
     * <p><strong>支持的参数类型：</strong></p>
     * <ul>
     *   <li>int - 整数</li>
     *   <li>double - 浮点数</li>
     *   <li>boolean - 布尔值</li>
     *   <li>String - 字符串</li>
     *   <li>List&lt;String&gt; - 字符串数组</li>
     * </ul>
     *
     * @param filePath 配置文件路径
     * @param key 配置键名
     * @param value 配置值，支持上述多种类型
     * @param comments 可选的注释行数组，将写入配置项上方并覆盖原有注释。若为空，则清除原有注释；若为 null，则保持原有注释不变。
     * @throws IOException 文件写入失败
     */
    public static void writeEntry(Path filePath, String key, String value, String[] comments) throws IOException {
        writeValue(filePath, key, "\"" + value + "\"", comments);
    }

    /**
     * @see #writeEntry(Path, String, String, String[])
     */
    public static void writeEntry(Path filePath, String key, int value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    /**
     * @see #writeEntry(Path, String, String, String[])
     */
    public static void writeEntry(Path filePath, String key, double value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    /**
     * @see #writeEntry(Path, String, String, String[])
     */
    public static void writeEntry(Path filePath, String key, boolean value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    /**
     * @see #writeEntry(Path, String, String, String[])
     */
    public static void writeArrayEntry(Path filePath, String key, List<String> values, String[] comments) throws IOException {
        String arrayValue = ARRAY_START + String.join(", ", values.stream().map(v -> "\"" + v + "\"").toList()) + ARRAY_END;
        writeValue(filePath, key, arrayValue, comments);
    }

    /**
     * 向配置文件写入一个配置项。通常应由 {@link #writeEntry(Path, String, String, String[])} 等方法调用。
     * @param filePath 配置文件路径
     * @param key 配置键名
     * @param valueStr 配置值字符串，将直接写入配置文件，不进行任何处理
     * @param comments 可选的注释行数组
     * @throws IOException 文件写入失败
     */
    private static void writeValue(Path filePath, String key, String valueStr, String[] comments) throws IOException {
        List<String> lines = Files.exists(filePath) ? Files.readAllLines(filePath) : new ArrayList<>();

        boolean found = false; // 指定的键是否已存在
        int commentLineCount = 0; // 原有注释行数
        String[] originalComments; // 原有注释行数组
        for (int i = 0; i < lines.size(); i++) {
            // 从上到下遍历配置文件，直到找到指定键或到达文件末尾
            String line = lines.get(i).trim();
            if (line.startsWith(COMMENT_PREFIX)) { // 此行是注释行
                commentLineCount++;
            } else if (line.startsWith(key + EQ)) { // 此行是指定键的定义行
                // 删除原有注释和字段
                originalComments = new String[commentLineCount];
                int start = i - commentLineCount;
                for (int j = 0; j < commentLineCount + 1; j++) {
                    if (j < commentLineCount) {
                        originalComments[j] = lines.get(start).trim();
                    }
                    lines.remove(start);
                }
                // 插入新注释和字段
                insertEntry(lines, start, key, valueStr, comments, originalComments);
                found = true;
                break;
            } else {
                // 此行是无关行（可能是空行或标签行），清除原有注释行数重新计数
                commentLineCount = 0;
            }
        }

        if (!found) {
            // 指定的键不存在，将新字段插入到文件末尾
            insertEntry(lines, lines.size(), key, valueStr, comments, null);
        }

        Files.write(filePath, lines);
    }

    /**
     * 在指定位置插入行。通常应由 {@link #writeValue(Path, String, String, String[])} 调用。
     * @param lines 原始配置文件的所有行
     * @param position 插入位置
     * @param key 键名
     * @param value 值字符串，将直接写入配置文件，不进行任何处理
     * @param comments 新注释行数组。若不为 null，则插入此数组中的注释行；若为 null，则插入原有注释行
     * @param originalComments 原有注释行数组
     */
    private static void insertEntry(List<String> lines, int position, String key, String value, String[] comments, String[] originalComments) {
        List<String> newLines = new ArrayList<>();
        if (comments!= null) {
            // 新注释行数组不为 null，插入新注释行（若为空则什么也不会插入）
            for (String comment : comments) {
                comment = comment.replace("\n", "\n" + COMMENT_PREFIX);
                newLines.add(COMMENT_PREFIX + comment);
            }
        } else {
            // 新注释行数组为 null，插入原有注释行
            Collections.addAll(newLines, originalComments);
        }
        newLines.add(key + EQ + value); // 插入字段

        lines.addAll(position, newLines);
    }

    /* ========== 删除操作 ========== */

    /**
     * 删除配置文件中的指定字段，包括其注释行。
     * @param filePath 配置文件路径
     * @param key 要删除的字段名
     * @throws IOException 文件写入失败
     */
    public static void deleteKey(Path filePath, String key) throws IOException {
        // 逻辑和上面的 writeValue(Path, String, String, String[]) 方法差不多
        List<String> lines = Files.readAllLines(filePath);
        int commentLineCount = 0;
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith(COMMENT_PREFIX)) {
                commentLineCount++;
            } else if (line.startsWith(key + EQ)) {
                int start = i - commentLineCount;
                for (int j = 0; j < commentLineCount + 1; j++) {
                    lines.remove(start);
                }
                found = true;
                break;
            } else {
                commentLineCount = 0;
            }
        }

        if (found) {
            Files.write(filePath, lines);
        }
    }

    /* ========== 读取操作 ========== */

    /**
     * 读取配置文件中的指定字段。支持的数据类型与 {@link #writeEntry(Path, String, String, String[])} 一致，但不支持 int 类型而只支持 long 类型。
     * @param filePath 配置文件路径
     * @param key 要读取的字段名
     * @return 字段值，类型由字段值决定
     * @see #writeEntry(Path, String, String, String[])
     */
    public static String readString(Path filePath, String key) {
        return parseToml(filePath).getString(key);
    }

    /**
     * @see #readString(Path, String)
     */
    public static long readLong(Path filePath, String key) {
        return parseToml(filePath).getLong(key);
    }

    /**
     * @see #readString(Path, String)
     */
    public static double readDouble(Path filePath, String key) {
        return parseToml(filePath).getDouble(key);
    }

    /**
     * @see #readString(Path, String)
     */
    public static boolean readBoolean(Path filePath, String key) {
        return parseToml(filePath).getBoolean(key);
    }

    /**
     * @see #readString(Path, String)
     */
    public static List<String> readStringArray(Path filePath, String key) {
        return parseToml(filePath).getList(key);
    }

    /**
     * 解析 TOML 配置文件。
     * @param filePath 配置文件路径
     * @return 解析结果的 {@link Toml} 对象
     * @throws IllegalStateException 解析失败
     */
    private static Toml parseToml(Path filePath) {
        return new Toml().read(filePath.toFile());
    }
}
