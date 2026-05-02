package me.inf32768.ultimate_scaler.util;

import net.minecraft.util.math.Direction;

import java.math.BigDecimal;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * 实用工具类，包含了本模组一些核心或常用的方法。
 */
public class Util {
    // 私有构造方法，防止实例化
    private Util() {}

    /**
     * 计算两个整数的平均值。
     * <p>
     * 使用位运算来计算，避免了传统的除法算法中整数溢出的问题，且通常性能更好。
     * @param i 第一个整数
     * @param j 第二个整数
     * @return 两个整数的平均值（向下取整）
     */
    public static int average(int i , int j) {
        return (i & j) + ((i ^ j) >> 1);
    }

    /**
     * 获取一个一维 {@code double} 坐标经过偏移和缩放后的对应 {@code double} 坐标（存在精度损失），使用当前配置的偏移量和缩放比例。
     * @param pos 原坐标
     * @param axis 此坐标在空间中对应的轴
     * @return 偏移和缩放后的坐标
     * @see me.inf32768.ultimate_scaler.option.UltimateScalerOptions.ConfigImpl
     */
    @SuppressWarnings("unused")
    public static double RepositionDouble(double pos, Direction.Axis axis) {
        return pos * config.globalBigDecimalScale[axis.ordinal()].doubleValue() + config.globalBigDecimalOffset[axis.ordinal()].doubleValue();
    }

    /**
     * 获取一个一维 {@code int} 坐标经过偏移和缩放后的对应 {@code double} 坐标（存在精度损失），使用当前配置的偏移量和缩放比例。
     * @param pos 原坐标
     * @param axis 此坐标在空间中对应的轴
     * @return 偏移和缩放后的坐标
     * @see me.inf32768.ultimate_scaler.option.UltimateScalerOptions.ConfigImpl
     */
    public static double RepositionDouble(int pos, Direction.Axis axis) {
        return pos * config.globalBigDecimalScale[axis.ordinal()].doubleValue() + config.globalBigDecimalOffset[axis.ordinal()].doubleValue();
    }

    /**
     * 获取一个一维 {@code double} 坐标经过偏移和缩放后的对应 {@code BigDecimal} 坐标（不存在精度损失），使用当前配置的偏移量和缩放比例。
     * <p>
     * 注：运算速度较慢。
     * @param pos 原坐标
     * @param axis 此坐标在空间中对应的轴
     * @return 偏移和缩放后的坐标
     * @see #RepositionDouble(double, Direction.Axis)
     */
    @SuppressWarnings("unused")
    public static BigDecimal RepositionBigDecimal(double pos, Direction.Axis axis) {
        return BigDecimal.valueOf(pos).multiply(config.globalBigDecimalScale[axis.ordinal()]).add(config.globalBigDecimalOffset[axis.ordinal()]);
    }

    /**
     * 获取一个一维 {@code int} 坐标经过偏移和缩放后的对应 {@code BigDecimal} 坐标（不存在精度损失），使用当前配置的偏移量和缩放比例。
     * <p>
     * 注：运算速度较慢。
     * @param pos 原坐标
     * @param axis 此坐标在空间中对应的轴
     * @return 偏移和缩放后的坐标
     * @see #RepositionDouble(int, Direction.Axis)
     */
    public static BigDecimal RepositionBigDecimal(int pos, Direction.Axis axis) {
        return BigDecimal.valueOf(pos).multiply(config.globalBigDecimalScale[axis.ordinal()]).add(config.globalBigDecimalOffset[axis.ordinal()]);
    }
}
