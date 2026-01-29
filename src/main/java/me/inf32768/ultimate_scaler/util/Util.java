package me.inf32768.ultimate_scaler.util;

import net.minecraft.util.math.Direction;

import java.math.BigDecimal;
import java.math.BigInteger;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

public class Util {
    private Util() {}

    public static int average(int i , int j) {
        return (i & j) + ((i ^ j) >> 1);
    }

    @SuppressWarnings("unused")
    public static double getDoubleOffsetPos(double pos, Direction.Axis axis) {
        return pos * config.globalBigDecimalScale[axis.ordinal()].doubleValue() + config.globalBigDecimalOffset[axis.ordinal()].doubleValue();
    }

    public static double getDoubleOffsetPos(int pos, Direction.Axis axis) {
        return pos * config.globalBigDecimalScale[axis.ordinal()].doubleValue() + config.globalBigDecimalOffset[axis.ordinal()].doubleValue();
    }

    @SuppressWarnings("unused")
    public static BigInteger getBigIntegerOffsetPos(double pos, Direction.Axis axis) {
        return BigDecimal.valueOf(pos).multiply(config.globalBigDecimalScale[axis.ordinal()]).add(config.globalBigDecimalOffset[axis.ordinal()]).toBigInteger();
    }

    public static BigInteger getBigIntegerOffsetPos(int pos, Direction.Axis axis) {
        return BigDecimal.valueOf(pos).multiply(config.globalBigDecimalScale[axis.ordinal()]).add(config.globalBigDecimalOffset[axis.ordinal()]).toBigInteger();
    }
}
