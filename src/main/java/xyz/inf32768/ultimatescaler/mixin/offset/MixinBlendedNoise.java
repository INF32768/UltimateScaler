package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link BlendedNoise} 类的 Mixin，用于对密度函数 {@code minecraft:old_blended_noise} 施加偏移与缩放，并扩展其参数的取值范围。
 *
 * @see xyz.inf32768.ultimatescaler.config.Config.ConfigImpl#expandDatapackValueRange
 */
@Mixin(BlendedNoise.class)
public abstract class MixinBlendedNoise {
    /**
     * 修改 {@code InterpolatedNoiseSampler.SCALE_AND_FACTOR_RANGE} 字段。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，密度函数 {@code minecraft:old_blended_noise} 中的参数 {@code xz_scale}, {@code y_scale}, {@code xz_factor}, {@code y_factor} 的取值范围都被限制在了 {@code [0.001, 1000.0]} 之间。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使得参数范围可以为任意值（仅在启用了 {@code expandDatapackValueRange} 选项时生效）。
     */
    @ModifyArgs(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;doubleRange(DD)Lcom/mojang/serialization/Codec;"))
    private static void modifyScaleAndFactorRange(Args args) {
        if (Config.impl.expandDatapackValueRange) {
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
        if (Config.impl.expandDatapackValueRange) {
            args.set(0, Double.NEGATIVE_INFINITY);
            args.set(1, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * 为 x 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "compute", at = @At("STORE"), ordinal = 0)
    private double modifyBlockX(double x, DensityFunction.FunctionContext pos) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() * getScaledXzScale() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X) * getScaledXzScale();
    }

    /**
     * 为 y 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "compute", at = @At("STORE"), ordinal = 1)
    private double modifyBlockY(double y, DensityFunction.FunctionContext pos) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() * getScaledYScale() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y) * getScaledYScale();
    }

    /**
     * 为 z 坐标施加偏移与缩放
     */
    @ModifyVariable(method = "compute", at = @At("STORE"), ordinal = 2)
    private double modifyBlockZ(double z, DensityFunction.FunctionContext pos) {
        return Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() * getScaledXzScale() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z) * getScaledXzScale();
    }

    /**
     * {@code InterpolatedNoiseSampler.scaledXzScale} 字段的访问器，用于获取数据中定义的 xz 缩放比例（对应密度函数参数中的 {@code xz_scale}）以应用偏移与缩放。
     */
    @Accessor("xzMultiplier")
    abstract double getScaledXzScale();

    /**
     * {@code InterpolatedNoiseSampler.scaledYScale} 字段的访问器，用于获取数据中定义的 y 缩放比例（对应密度函数参数中的 {@code y_scale}）以应用偏移与缩放。
     */
    @Accessor("yMultiplier")
    abstract double getScaledYScale();
}
