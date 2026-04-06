package me.inf32768.ultimate_scaler.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code locate pos} 命令的实现类，包含了此命令的实现、注册等逻辑。
 */
public class LocatePosition {
    /**
     * 异常类型：无效的十进制数，在以字符串形式输入的数值参数无法被解析为有效的十进制数时抛出。
     */
    private static final SimpleCommandExceptionType INVALID_DECIMAL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.invalid_decimal"));
    /**
     * 异常类型：无效的缩放比例，当缩放比例小于等于 0 时抛出。
     */
    private static final SimpleCommandExceptionType SCALE_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.scale_invalid"));
    /**
     * 异常类型：未找到目标位置，当二分查找未能找到目标位置时（或是目标位置在搜索范围之外）抛出。
     */
    private static final SimpleCommandExceptionType NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.not_found"));
    /**
     * 异常类型：搜索范围为负数，当搜索范围参数为负数时抛出。
     */
    private static final SimpleCommandExceptionType RANGE_NEGATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.range_negative"));
    /**
     * 异常类型：搜索范围过大，当搜索范围参数过大（大于 {@code Double.MAX_VALUE}）时抛出。
     */
    private static final SimpleCommandExceptionType RANGE_TOO_LARGE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.range_too_large"));

    /**
     * 初始化方法，提供了命令语法的定义并注册命令。
     * <p>
     * 语法：{@code /locate pos <originalPos> <scale> <offset> [range]}
     */
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("locate")
            .then(literal("pos")
                .then(argument("originalPos", StringArgumentType.string())
                    .then(argument("scale", DoubleArgumentType.doubleArg())
                        .then(argument("offset", DoubleArgumentType.doubleArg())
                            .executes(context -> (int) calculate(StringArgumentType.getString(context, "originalPos"), DoubleArgumentType.getDouble(context, "scale"), DoubleArgumentType.getDouble(context, "offset"), "0", context)) // 未指定搜索范围时使用默认范围
                            .then(argument("range", StringArgumentType.string())
                                .executes(context -> (int) calculate(StringArgumentType.getString(context, "originalPos"), DoubleArgumentType.getDouble(context, "scale"), DoubleArgumentType.getDouble(context, "offset"), StringArgumentType.getString(context, "range"), context)))))))));
    }

    private static final BigInteger TWO = BigInteger.valueOf(2);
    /**
     * 二分查找最大迭代次数，超过此次数则认为未找到目标位置，抛出 {@link #NOT_FOUND_EXCEPTION} 异常。
     */
    private static final BigInteger MAX_ITERATIONS = BigInteger.valueOf(100000);

    /**
     * 查找的入口，包含了参数校验和数值转换，以及调用二分查找、结果处理方法的逻辑。
     * @param originalPos 原始位置坐标，必须能被 {@link #parseBigDecimal(String)} 解析为有效的十进制数
     * @param scale 缩放比例
     * @param offset 偏移量
     * @param rangeArg 搜索范围
     * @param context 命令上下文，用于反馈结果，在游戏中执行时会自动填入。测试时可设为 {@code null}，此时不会将结果发送到聊天栏。
     * @return 找到的坐标
     * @throws CommandSyntaxException 参数无效或未找到目标位置时抛出，详见异常类型定义。
     */
    public static double calculate(String originalPos, double scale, double offset, String rangeArg,
                                   CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // 参数校验
        if (scale <= 0) throw SCALE_INVALID_EXCEPTION.create();

        // 精确数值转换
        BigDecimal originalPosBigDecimal = parseBigDecimal(originalPos);
        BigDecimal scaleBigDecimal = new BigDecimal(scale);
        BigDecimal offsetBigDecimal = new BigDecimal(offset);
        BigDecimal searchRange = parseRange(rangeArg, originalPosBigDecimal, scaleBigDecimal, offsetBigDecimal);

        // 二分查找逻辑
        BigInteger[] result = binarySearch(originalPosBigDecimal, scaleBigDecimal, offsetBigDecimal, searchRange);

        // 结果处理
        return handleResult(result, context);
    }

    /**
     * 将输入的搜索范围参数解析为内部实际的查找范围，并将其转换为 {@link BigDecimal} 类型。
     * @param rangeArg 搜索范围参数，可以是 {@code "0"} 表示使用默认的搜索范围，也可以是任意十进制数
     * @param target （未使用，我也不知道是什么）
     * @param scale 缩放比例
     * @param offset 偏移量
     * @return 搜索范围 {@link BigDecimal} 类型
     * @throws CommandSyntaxException 搜索范围参数无效（太大、不是整数或无法解析）时抛出
     */
    private static BigDecimal parseRange(String rangeArg, BigDecimal target, BigDecimal scale, BigDecimal offset)
            throws CommandSyntaxException {
        BigDecimal range = parseBigDecimal(rangeArg);

        if (range.compareTo(BigDecimal.ZERO) == 0) {
            // 使用默认搜索范围：Doule.MAX_VALUE / scale - offset
            return new BigDecimal(Double.MAX_VALUE)
                    .divide(scale, 100, RoundingMode.HALF_UP)
                    .subtract(offset);
        }

        if (range.compareTo(BigDecimal.ZERO) < 0) throw RANGE_NEGATIVE_EXCEPTION.create();
        if (range.compareTo(new BigDecimal("1E+100")) > 0) throw RANGE_TOO_LARGE_EXCEPTION.create(); // 我：1E+100 = Double.MAX_VALUE

        return range;
    }

    /**
     * 将输入的字符串参数解析为 {@link BigDecimal} 类型，带有异常处理。
     * @param rangeArg 要解析的字符串
     * @return 解析结果 {@link BigDecimal} 类型
     * @throws CommandSyntaxException 字符串参数无法解析为有效的十进制数时抛出
     */
    private static BigDecimal parseBigDecimal(String rangeArg) throws CommandSyntaxException {
        try {
            return new BigDecimal(rangeArg);
        } catch (Exception e) {
            throw LocatePosition.INVALID_DECIMAL_EXCEPTION.create();
        }
    }

    /**
     * 二分查找算法。
     * @param target 查找的目标位置
     * @param scale 缩放比例
     * @param offset 偏移量
     * @param range 搜索范围
     * @return 查找结果，是一个长度为 2 的数组，分别表示搜索范围的下界和上界
     * @throws CommandSyntaxException 未找到目标位置时抛出
     */
    private static BigInteger[] binarySearch(BigDecimal target, BigDecimal scale, BigDecimal offset, BigDecimal range)
            throws CommandSyntaxException {
        BigInteger low = range.negate().toBigInteger();
        BigInteger high = range.toBigInteger();
        BigInteger iterations = BigInteger.ZERO;

        while (high.subtract(low).compareTo(BigInteger.ONE) > 0) {
            if (iterations.compareTo(MAX_ITERATIONS) > 0) {
                throw RANGE_TOO_LARGE_EXCEPTION.create(); // FIXME: 这里的异常类型不对，应该是 NOT_FOUND_EXCEPTION
            }

            BigInteger mid = low.add(high).divide(TWO);
            BigDecimal midValue = new BigDecimal(mid)
                    .multiply(scale)
                    .add(offset)
                    .setScale(target.scale(), RoundingMode.HALF_UP);

            if (midValue.compareTo(target) >= 0) {
                high = mid;
            } else {
                low = mid;
            }

            iterations = iterations.add(BigInteger.ONE);
        }

        return new BigInteger[]{low, high};
    }

    /**
     * 处理二分查找结果，并将结果发送到聊天栏。
     * @param result 二分查找结果，是一个长度为 2 的数组，分别表示搜索范围的下界和上界
     * @param context 命令上下文，用于反馈结果，在游戏中执行时会自动填入。测试时可设为 {@code null}，此时不会将结果发送到聊天栏。
     * @return 最终的搜索结果（{@code double} 类型）
     * @throws CommandSyntaxException 未找到目标位置时抛出
     */
    private static double handleResult(BigInteger[] result, CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        // 验证最终结果
        if (result[1].compareTo(result[0].add(BigInteger.ONE)) != 0) {
            throw NOT_FOUND_EXCEPTION.create();
        }

        // 反馈结果
        if (context != null) {
            context.getSource().sendFeedback(
                    () -> Text.translatable("ultimate_scaler.commands.locate.pos.success", result[1].toString()),
                    false
            );
        }
        return result[1].doubleValue(); // FIXME: 为什么要转换为 double 类型？
    }
}
