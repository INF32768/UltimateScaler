package me.inf32768.ultimate_scaler.mixins.offset;

import me.inf32768.ultimate_scaler.option.UltimateScalerOptions;
import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link InterpolatedNoiseSampler} 类的 Mixin，用于对密度函数 {@code minecraft:old_blended_noise} 施加偏移与缩放，并扩展其参数的取值范围。
 *
 * @see me.inf32768.ultimate_scaler.option.UltimateScalerOptions.ConfigImpl#expandDatapackValueRange
 */
@Mixin(InterpolatedNoiseSampler.class)
public abstract class MixinInterpolatedNoiseSampler {
    /**
     * 修改 {@code InterpolatedNoiseSampler.SCALE_AND_FACTOR_RANGE} 字段。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，密度函数 {@code minecraft:old_blended_noise} 中的参数 {@code xz_scale}, {@code y_scale}, {@code xz_factor}, {@code y_factor} 的取值范围都被限制在了 {@code [0.001, 1000.0]} 之间。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使得参数范围可以为任意值（仅在启用了 {@code expandDatapackValueRange} 选项时生效）。
     */
    @ModifyArgs(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;doubleRange(DD)Lcom/mojang/serialization/Codec;"))
    private static void modifyScaleAndFactorRange(Args args) {
        if (UltimateScalerOptions.config.expandDatapackValueRange) {
            args.set(0, Double.NEGATIVE_INFINITY);
            args.set(1, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * 修改 {@code InterpolatedNoiseSampler.MAP_CODED} 字段。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，密度函数 {@code minecraft:old_blended_noise} 中的参数 {@code smear_scale_multiplier} 的取值范围都被限制在了 {@code [1.0, 8.0]} 之间。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使得参数范围可以为任意值（仅在启用了 {@code expandDatapackValueRange} 选项时生效）。
     */
    @ModifyArgs(method = "method_42385", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;doubleRange(DD)Lcom/mojang/serialization/Codec;"))
    private static void modifySmearScaleMultiplierRange(Args args) {
        if (UltimateScalerOptions.config.expandDatapackValueRange) {
            args.set(0, Double.NEGATIVE_INFINITY);
            args.set(1, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * 为 x 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "sample", at = @At("STORE"), ordinal = 0)
    private double modifyBlockX(double x, DensityFunction.NoisePos pos) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() * getScaledXzScale() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X) * getScaledXzScale();
    }

    /**
     * 为 y 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "sample", at = @At("STORE"), ordinal = 1)
    private double modifyBlockY(double y, DensityFunction.NoisePos pos) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() * getScaledYScale() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y) * getScaledYScale();
    }

    /**
     * 为 z 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "sample", at = @At("STORE"), ordinal = 2)
    private double modifyBlockZ(double z, DensityFunction.NoisePos pos) {
        return config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() * getScaledXzScale() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z) * getScaledXzScale();
    }

    /**
     * {@code InterpolatedNoiseSampler.scaledXzScale} 字段的访问器，用于获取数据中定义的 xz 缩放比例（对应密度函数参数中的 {@code xz_scale}）以应用偏移与缩放。
     */
    @Accessor("scaledXzScale")
    abstract double getScaledXzScale();

    /**
     * {@code InterpolatedNoiseSampler.scaledYScale} 字段的访问器，用于获取数据中定义的 y 缩放比例（对应密度函数参数中的 {@code y_scale}）以应用偏移与缩放。
     */
    @Accessor("scaledYScale")
    abstract double getScaledYScale();
}
