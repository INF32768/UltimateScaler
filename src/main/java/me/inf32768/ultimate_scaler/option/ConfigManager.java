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

public class ConfigManager {
    private static final String COMMENT_PREFIX = "# ";
    private static final String ARRAY_START = "[";
    private static final String ARRAY_END = "]";
    private static final String EQ = " = ";

    /* ========== 写入/修改操作 ========== */

    public static void writeEntry(Path filePath, String key, String value, String[] comments) throws IOException {
        writeValue(filePath, key, "\"" + value + "\"", comments);
    }

    public static void writeEntry(Path filePath, String key, int value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    public static void writeEntry(Path filePath, String key, double value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    public static void writeEntry(Path filePath, String key, boolean value, String[] comments) throws IOException {
        writeValue(filePath, key, String.valueOf(value), comments);
    }

    public static void writeArrayEntry(Path filePath, String key, List<String> values, String[] comments) throws IOException {
        String arrayValue = ARRAY_START + String.join(", ", values.stream().map(v -> "\"" + v + "\"").toList()) + ARRAY_END;
        writeValue(filePath, key, arrayValue, comments);
    }

    private static void writeValue(Path filePath, String key, String valueStr, String[] comments) throws IOException {
        List<String> lines = Files.exists(filePath) ? Files.readAllLines(filePath) : new ArrayList<>();

        boolean found = false;
        int commentLineCount = 0;
        String[] originalComments;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith(COMMENT_PREFIX)) {
                commentLineCount++;
            } else if (line.startsWith(key + EQ)) {
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
                commentLineCount = 0;
            }
        }

        if (!found) {
            insertEntry(lines, lines.size(), key, valueStr, comments, null);
        }

        Files.write(filePath, lines);
    }

    private static void insertEntry(List<String> lines, int position, String key, String value, String[] comments, String[] originalComments) {
        List<String> newLines = new ArrayList<>();
        if (comments!= null) {
            for (String comment : comments) {
                comment = comment.replace("\n", "\n" + COMMENT_PREFIX);
                newLines.add(COMMENT_PREFIX + comment);
            }
        } else {
            Collections.addAll(newLines, originalComments);
        }
        newLines.add(key + EQ + value);

        lines.addAll(position, newLines);
    }

    /* ========== 删除操作 ========== */

    public static void deleteKey(Path filePath, String key) throws IOException {
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

    public static String readString(Path filePath, String key) {
        return parseToml(filePath).getString(key);
    }

    public static long readLong(Path filePath, String key) {
        return parseToml(filePath).getLong(key);
    }

    public static double readDouble(Path filePath, String key) {
        return parseToml(filePath).getDouble(key);
    }

    public static boolean readBoolean(Path filePath, String key) {
        return parseToml(filePath).getBoolean(key);
    }

    public static List<String> readStringArray(Path filePath, String key) {
        return parseToml(filePath).getList(key);
    }

    private static Toml parseToml(Path filePath) {
        return new Toml().read(filePath.toFile());

    }
}
