package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.Config;

import java.math.BigInteger;

/**
 * {@code DensityFunctions.EndIslandDensityFunction} 类的 Mixin，用于对密度函数 {@code minecraft:end_islands} 施加偏移和缩放，以及修复“末地环”。
 * <p>
 * 注：由于涉及整数重写，因此所有的偏移和缩放只有在启用 {@code bigIntegerRewrite} 选项时才应生效。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$EndIslandDensityFunction")
public abstract class MixinEndIslandDensityFunction {
    /**
     * 修改 {@code DensityFunctions.EndIslandDensityFunction#compute(DensityFunction.FunctionContext)} 方法的参数，为后续施加偏移和缩放做准备。
     */
    @ModifyArgs(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunctions$EndIslandDensityFunction;getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F"))
    private void modifyNoisePos(Args args, DensityFunction.FunctionContext pos) {
        if (Config.impl.bigIntegerRewrite) {
            // 在原版中，原始的坐标是除以 8 后再传入实际的采样方法的，这里为了方便后续的计算，我们将原始坐标直接传入，并在后续的计算中把除以 8 给补上
            args.set(1, pos.blockX());
            args.set(2, pos.blockZ());
        }
    }

    /**
     * 给第一个中间坐标 {@code int i = x / 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F", at = @At("STORE"), ordinal = 2)
    private static int modifySampleX(int original, SimplexNoise sampler, int x, int z) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(16)).intValue() : original;
    }
    /**
     * 给第二个中间坐标 {@code int j = z / 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F", at = @At("STORE"), ordinal = 3)
    private static int modifySampleZ(int original, SimplexNoise sampler, int x, int z) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(16)).intValue() : original;
    }
    /**
     * 给第三个中间坐标 {@code int k = x % 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F", at = @At("STORE"), ordinal = 4)
    private static int modifySampleX1(int original, SimplexNoise sampler, int x, int z) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8)).remainder(BigInteger.TWO).intValue() : original;
    }
    /**
     * 给第四个中间坐标 {@code int l = z % 2} 施加偏移和缩放。
     */
    @ModifyVariable(method = "getHeightValue(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)F", at = @At("STORE"), ordinal = 5)
    private static int modifySampleZ2(int original, SimplexNoise sampler, int x, int z) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8)).remainder(BigInteger.TWO).intValue() : original;
    }
    /**
     * 给 {@code MathHelper.sqrt} 方法传入的坐标 {@code (x * x + z * z)} 施加偏移和缩放，并根据 {@code fixEndRings} 选项决定是否修复末地环（修复整数溢出）。
     */
    @ModifyArgs(method = "getHeightValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sqrt(F)F", ordinal = 0))
    private static void modifySqrt(Args args, SimplexNoise sampler, int x, int z) {
        if (Config.impl.fixEndRings) {
            if (Config.impl.bigIntegerRewrite) {
                BigInteger offsetX = Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8));
                BigInteger offsetZ = Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8));
                args.set(0, offsetX.multiply(offsetX).add(offsetZ.multiply(offsetZ)).floatValue());
            } else {
                args.set(0, (float) ((double) x * (double) x + (double) z * (double) z));
            }
        } else if (Config.impl.bigIntegerRewrite) {
            int offsetX = Util.RepositionBigDecimal(x, Direction.Axis.X).toBigInteger().divide(BigInteger.valueOf(8)).intValue();
            int offsetZ = Util.RepositionBigDecimal(z, Direction.Axis.Z).toBigInteger().divide(BigInteger.valueOf(8)).intValue();
            args.set(0, (float) (offsetX * offsetX + offsetZ * offsetZ));
        }
    }
}