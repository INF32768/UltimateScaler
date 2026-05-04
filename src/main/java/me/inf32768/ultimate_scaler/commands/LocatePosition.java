package me.inf32768.ultimate_scaler.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code locate pos} 命令的实现类，包含了此命令的实现、注册等逻辑。
 */
public class LocatePosition {
    /**
     * 异常类型：无效的缩放比例，当缩放比例小于等于 0 时抛出。
     */
    private static final SimpleCommandExceptionType SCALE_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("ultimate_scaler.commands.locate.pos.scale_invalid"));

    /**
     * 初始化方法，提供了命令语法的定义并注册命令。
     * <p>
     * 语法：{@code /locate pos <originalPos> <scale> <offset> [range]}
     */
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("locate")
            .then(literal("pos")
                .then(argument("originalPos", DoubleArgumentType.doubleArg())
                    .then(argument("scale", DoubleArgumentType.doubleArg())
                        .then(argument("offset", DoubleArgumentType.doubleArg())
                            .executes(context -> {calculate(DoubleArgumentType.getDouble(context, "originalPos"), DoubleArgumentType.getDouble(context, "scale"), DoubleArgumentType.getDouble(context, "offset"), context); return 1;}
                            )
                        )
                    )
                )
            )
        ));
    }

    /**
     * 命令的执行逻辑，负责检验参数、调用二分查找器并反馈结果。
     * @param pos 原始位置坐标
     * @param scale 缩放比例，不可为 0
     * @param offset 偏移量
     * @param context 命令上下文，用于在聊天栏中反馈结果，在游戏中执行时会自动填入。
     */
    public static void calculate(double pos, double scale, double offset, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (scale == 0) throw SCALE_INVALID_EXCEPTION.create();
        BigInteger ans = smallestWrappedInteger(pos, scale, offset);
        context.getSource().sendFeedback(
                () -> Text.translatable("ultimate_scaler.commands.locate.pos.success", ans.toString()),
                false
        );
    }

    /**
     * 最小坐标查找器：通过二分查找找到一个坐标经过偏移和缩放后的对应坐标，也就是找到唯一的 {@code BigInteger} 整数 {@code n}，满足 {@code n.doubleValue() * scale + offset >= pos && n.subtract(BigIntger.ONE).doubleValue() * scale + offset < pos}，当 {@code scale < 0} 时不等号取反。
     * @param pos 原始位置坐标
     * @param scale 缩放比例，不可为 0
     * @param offset 偏移量
     * @return 目标整数 {@code n}
     */
    @SuppressWarnings("UnusedReturnValue")
    private static BigInteger smallestWrappedInteger(double pos, double scale, double offset) {
        // 由 Gemini 生成（不是广告）
        // 0. 参数转换和初始化
        BigDecimal bx = new BigDecimal(pos);
        BigDecimal by = new BigDecimal(scale);
        BigDecimal bz = new BigDecimal(offset);

        BigInteger low, high;

        // 1. 建立数学锚点 (Math Anchor)
        // 估测答案。即使有极端舍入误差，这个锚点也能大幅减少迭代次数
        BigInteger nBase = bx.subtract(bz)
                .divide(by, MathContext.DECIMAL128)
                .toBigInteger();

        // 2. 指数探路 (Galloping Search / Exponential Search) 确定严格上下界
        // 后续的二分查找只在确定的区间内执行，同样减少迭代次数
        if (test(nBase, pos, scale, offset)) {
            // 锚点已经满足条件，说明答案在锚点左侧或就是锚点。
            // 我们需要向左寻找一个不满足条件的 lower bound
            high = nBase;
            BigInteger step = BigInteger.ONE;
            low = nBase.subtract(step);

            while (test(low, pos, scale, offset)) {
                high = low; // 既然 low 也满足，它可以作为更紧的 upper bound
                step = step.shiftLeft(1); // 步长翻倍: step *= 2
                low = low.subtract(step);
            }
            // 循环结束时，low 必定不满足，high 必定满足，区间锁定在 (low, high]
        } else {
            // 锚点不满足条件，说明真实答案在锚点右侧
            // 我们需要向右寻找一个满足条件的 upper bound
            low = nBase.add(BigInteger.ONE);
            BigInteger step = BigInteger.ONE;
            high = low.add(step);

            while (!test(high, pos, scale, offset)) {
                low = high.add(BigInteger.ONE); // 既然 high 不满足，比它大的才可能满足
                step = step.shiftLeft(1); // 步长翻倍: step *= 2
                high = high.add(step);
            }
            // 循环结束时，low-1 必定不满足，high 必定满足，区间锁定在 [low, high]
        }

        // 3. 经典二分查找精确收敛 (Binary Search)
        // 在极其有限的 [low, high] 范围内寻找第一个满足条件的 BigInteger
        BigInteger ans = high; // high 是一定满足条件的保底答案

        while (low.compareTo(high) <= 0) {
            BigInteger mid = low.add(high).shiftRight(1); // (low + high) / 2

            if (test(mid, pos, scale, offset)) {
                ans = mid; // mid 满足条件，记录下来，并尝试寻找更小的
                high = mid.subtract(BigInteger.ONE);
            } else {
                // mid 不满足条件，说明答案一定在右边
                low = mid.add(BigInteger.ONE);
            }
        }
        // 循环结束时，若不出意外则 ans 就是正确答案
        return ans;
    }

    // 判定一个 BigInteger 是否满足原业务条件
    private static boolean test(BigInteger n, double pos, double scale, double offset) {
        double res = n.doubleValue() * scale + offset;
        return scale > 0 ? res >= pos : res <= pos;
    }
}
