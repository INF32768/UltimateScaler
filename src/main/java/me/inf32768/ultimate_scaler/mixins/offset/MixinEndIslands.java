package me.inf32768.ultimate_scaler.mixins.offset;

import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.math.BigInteger;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link DensityFunctionTypes.EndIslands} 类的 Mixin，用于对密度函数 {@code minecraft:end_islands} 施加偏移和缩放，以及修复“末地环”。
 * <p>
 * 注：由于涉及整数重写，因此所有的偏移和缩放只有在启用 {@code bigIntegerRewrite} 选项时才应生效。
 */
@Mixin(DensityFunctionTypes.EndIslands.class)
public abstract class MixinEndIslands {
    /**
     * 修改 {@link DensityFunctionTypes.EndIslands#sample(DensityFunction.NoisePos)} 方法的参数，为后续施加偏移和缩放做准备。
     */
    @ModifyArgs(method = "sample(Lnet/minecraft/world/gen/densityfunction/DensityFunction$NoisePos;)D", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunctionTypes$EndIslands;sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F"))
    private void modifyNoisePos(Args args, DensityFunction.NoisePos pos) {
        if (config.bigIntegerRewrite) {
            // 在原版中，原始的坐标是除以 8 后再传入实际的采样方法的，这里为了方便后续的计算，我们将原始坐标直接传入，并在后续的计算中把除以 8 给补上
            args.set(1, pos.blockX());
            args.set(2, pos.blockZ());
        }
    }

    /**
     * 给第一个中间坐标 {@code int i = x / 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", at = @At("STORE"), ordinal = 2)
    private static int modifySampleX(int original, SimplexNoiseSampler sampler, int x, int z) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(16)).intValue() : original;
    }
    /**
     * 给第二个中间坐标 {@code int j = z / 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", at = @At("STORE"), ordinal = 3)
    private static int modifySampleZ(int original, SimplexNoiseSampler sampler, int x, int z) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(16)).intValue() : original;
    }
    /**
     * 给第三个中间坐标 {@code int k = x % 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", at = @At("STORE"), ordinal = 4)
    private static int modifySampleX1(int original, SimplexNoiseSampler sampler, int x, int z) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8)).remainder(BigInteger.TWO).intValue() : original;
    }
    /**
     * 给第四个中间坐标 {@code int l = z % 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", at = @At("STORE"), ordinal = 5)
    private static int modifySampleZ2(int original, SimplexNoiseSampler sampler, int x, int z) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8)).remainder(BigInteger.TWO).intValue() : original;
    }
    /**
     * 给 {@code MathHelper.sqrt} 方法传入的坐标 {@code (x * x + z * z)} 施加偏移和缩放，并根据 {@code fixEndRings} 选项决定是否修复末地环（修复整数溢出）。
     */
    @ModifyArgs(method = "sample(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sqrt(F)F", ordinal = 0))
    private static void modifySqrt(Args args, SimplexNoiseSampler sampler, int x, int z) {
        if (config.fixEndRings) {
            if (config.bigIntegerRewrite) {
                BigInteger offsetX = Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8));
                BigInteger offsetZ = Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8));
                args.set(0, offsetX.multiply(offsetX).add(offsetZ.multiply(offsetZ)).floatValue());
            } else {
                // FIXME: 在未启用 bigIntegerRewrite 时不应施加偏移和缩放
                double xDouble = Util.RepositionDouble(x, Direction.Axis.X);
                double zDouble = Util.RepositionDouble(z, Direction.Axis.Z);
                args.set(0, (float) (xDouble * xDouble + zDouble * zDouble));
            }
        } else if (config.bigIntegerRewrite) {
            int offsetX = Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8)).intValue();
            int offsetZ = Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8)).intValue();
            args.set(0, (float) (offsetX * offsetX + offsetZ * offsetZ));
        }
    }
}