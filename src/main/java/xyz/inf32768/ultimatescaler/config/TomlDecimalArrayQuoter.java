package xyz.inf32768.ultimatescaler.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修正 toml4j 对 {@link BigDecimal} 数组的读写行为。
 * <p>
 * toml4j 写入 {@link BigDecimal} 时会直接输出裸数字；读取裸数字时又会先解析为 {@code Long} 或
 * {@code Double}，因此超过 {@code long} 范围的整数会溢出，小数和科学计数法会损失精度。
 * 本类只负责在 TOML 文本中把指定的十进制数组元素改写为带双引号的字符串，使 toml4j 按字符串读取，
 * 之后由 Gson 精确地转换为 {@link BigDecimal}。
 * <p>
 * 它不是 TOML 解析器：只识别 {@code [worldGen.reposition]} 下的 {@code shift}/{@code scale}，
 * 以及根级的 {@code globalBigDecimalOffset}/{@code globalBigDecimalScale}。遇到无法确定的情况会
 * 原样返回输入，绝不让配置变得更糟。
 */
final class TomlDecimalArrayQuoter {
    /**
     * 匹配一行开头的数组赋值，并定位到左方括号。键名支持裸键、双引号键和单引号键。
     */
    private static final Pattern ARRAY_ASSIGNMENT = Pattern.compile(
            "(?m)^[ \\t]*(?:\"([^\"\\r\\n]+)\"|'([^'\\r\\n]+)'|([A-Za-z0-9_-]+))[ \\t]*=[ \\t]*\\["
    );
    /**
     * 匹配一行开头的表头，用于判断当前数组属于哪个表。
     */
    private static final Pattern TABLE_HEADER = Pattern.compile(
            "(?m)^[ \\t]*\\[([^\\[\\]\\r\\n]+)\\]"
    );
    private static final String REPOSITION_TABLE = "worldGen.reposition";

    private TomlDecimalArrayQuoter() {
    }

    /**
     * 将文本中需要保留精度的十进制数组元素改写为双引号字符串。
     *
     * @param toml 原始 TOML 文本
     * @return 改写后的文本；若无需改写或遇到无法确定的情况，则返回原文本
     */
    static String quoteDecimalArrays(String toml) {
        if (toml == null || toml.isEmpty()) return toml;

        StringBuilder result = new StringBuilder(toml.length() + 32);
        Matcher matcher = ARRAY_ASSIGNMENT.matcher(toml);
        int last = 0;
        while (matcher.find(last)) {
            int openBracket = matcher.end() - 1;
            int closeBracket = findClosingBracket(toml, openBracket);
            if (closeBracket < 0) {
                // 数组没有闭合，保持原样，交给 toml4j 报告语法错误
                break;
            }

            String key = keyOf(matcher);
            result.append(toml, last, matcher.start());
            if (isTarget(key, tableAt(toml, matcher.start()))) {
                String body = toml.substring(openBracket + 1, closeBracket);
                result.append(toml, matcher.start(), openBracket + 1)
                        .append(quoteArrayBody(body))
                        .append(']');
            } else {
                result.append(toml, matcher.start(), closeBracket + 1);
            }
            last = closeBracket + 1;
        }
        result.append(toml, last, toml.length());
        return result.toString();
    }

    private static String keyOf(Matcher matcher) {
        if (matcher.group(1) != null) return matcher.group(1);
        if (matcher.group(2) != null) return matcher.group(2);
        return matcher.group(3);
    }

    private static boolean isTarget(String key, String table) {
        if ("shift".equals(key) || "scale".equals(key)) {
            return REPOSITION_TABLE.equals(table);
        }
        if ("globalBigDecimalOffset".equals(key) || "globalBigDecimalScale".equals(key)) {
            return table.isEmpty();
        }
        return false;
    }

    /**
     * 找出指定位置之前最后一个表头。根表返回空字符串。
     */
    private static String tableAt(String toml, int end) {
        Matcher matcher = TABLE_HEADER.matcher(toml);
        matcher.region(0, end);
        String table = "";
        while (matcher.find()) {
            table = normalizeTableName(matcher.group(1));
        }
        return table;
    }

    private static String normalizeTableName(String name) {
        return name.trim()
                .replace("\"", "")
                .replace("'", "")
                .replaceAll("\\s*\\.\\s*", ".");
    }

    /**
     * 从 {@code [} 开始找到与之匹配的 {@code ]}，忽略字符串、注释和嵌套数组中的括号。
     *
     * @return 右方括号的下标；找不到时返回 {@code -1}
     */
    private static int findClosingBracket(String toml, int openBracket) {
        int depth = 0;
        boolean basicString = false;
        boolean literalString = false;
        boolean comment = false;
        boolean escaped = false;

        for (int i = openBracket; i < toml.length(); i++) {
            char c = toml.charAt(i);
            if (comment) {
                if (c == '\n') comment = false;
                continue;
            }
            if (basicString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    basicString = false;
                }
                continue;
            }
            if (literalString) {
                if (c == '\'') literalString = false;
                continue;
            }
            if (c == '#') {
                comment = true;
            } else if (c == '"') {
                basicString = true;
            } else if (c == '\'') {
                literalString = true;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * 把数组内部的元素改写为字符串。任一元素无法解析为 {@link BigDecimal} 时返回原内容。
     */
    private static String quoteArrayBody(String body) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean basicString = false;
        boolean literalString = false;
        boolean comment = false;
        boolean escaped = false;

        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (comment) {
                if (c == '\n') comment = false;
                continue;
            }
            if (basicString) {
                token.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    basicString = false;
                }
                continue;
            }
            if (literalString) {
                token.append(c);
                if (c == '\'') literalString = false;
                continue;
            }
            if (c == '#') {
                comment = true;
            } else if (c == '"') {
                basicString = true;
                token.append(c);
            } else if (c == '\'') {
                literalString = true;
                token.append(c);
            } else if (c == ',') {
                addToken(tokens, token);
            } else {
                token.append(c);
            }
        }
        addToken(tokens, token);

        // 允许末尾逗号，但中间的空元素视为无效，避免悄悄改变数组长度
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).isEmpty()) {
            tokens.remove(tokens.size() - 1);
        }
        if (tokens.isEmpty()) return body;

        List<String> values = new ArrayList<>(tokens.size());
        for (String tokenValue : tokens) {
            if (tokenValue.isEmpty()) return body;
            String value = unquote(tokenValue);
            if (value == null) return body;
            value = value.replace("_", "").trim();
            if (value.isEmpty()) return body;
            try {
                new BigDecimal(value);
            } catch (NumberFormatException e) {
                return body;
            }
            values.add(value);
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) result.append(", ");
            result.append('"').append(escapeBasicString(values.get(i))).append('"');
        }
        return result.toString();
    }

    private static void addToken(List<String> tokens, StringBuilder token) {
        tokens.add(token.toString().trim());
        token.setLength(0);
    }

    /**
     * 去掉元素两侧的引号。无法安全处理（例如带转义序列）时返回 {@code null}。
     */
    private static String unquote(String token) {
        if (token.length() >= 2) {
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);
            if (first == '"' && last == '"') {
                String value = token.substring(1, token.length() - 1);
                // 数值不需要转义；出现反斜杠说明不是普通数字，保持原样更安全
                return value.indexOf('\\') >= 0 ? null : value;
            }
            if (first == '\'' && last == '\'') {
                return token.substring(1, token.length() - 1);
            }
        }
        if (token.indexOf('"') >= 0 || token.indexOf('\'') >= 0) return null;
        return token;
    }

    private static String escapeBasicString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
