package me.inf32768.ultimate_scaler.mixins;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link OctavePerlinNoiseSampler} 类的 Mixin，用于修改坐标变换算法，进而修改边境之地的位置。
 */
@Mixin(OctavePerlinNoiseSampler.class)
public abstract class MixinOctavePerlinNoiseSampler {
    /**
     * 根据配置项 {@code farLandsPos} 等的值修改算法。关于具体的工作原理和目的，请参见 Wiki 上的文章。
     * @see <a href="https://github.com/INF32768/UltimateScaler/wiki/Reference.maintainPrecision.zh">《maintainPrecision 方法》</a>
     */
   @Inject(method = "maintainPrecision", at = @At("HEAD"), cancellable = true)
   private static void modifyMaintainPrecision(double value, CallbackInfoReturnable<Double> cir) {
        double result =  switch (config.farLandsPos) {
            case BETA -> value;
            case RELEASE -> Math.abs(value) > Long.MAX_VALUE ? value - Math.signum(value) * Long.MAX_VALUE : (value + 1.6777216E7D) % 3.3554432E7D - 1.6777216E7D;
            case REMOVED -> (value + 1.6777216E7D) % 3.3554432E7D - 1.6777216E7D;
            case CUSTOM -> value - (double) MathHelper.lfloor(value / config.maintainPrecisionCustomDivisor + (double) 0.5F) * config.maintainPrecisionCustomDivisor;
            default ->
                    value - (double) MathHelper.lfloor(value / (double) 3.3554432E7F + (double) 0.5F) * (double) 3.3554432E7F;
        };
        if (config.limitReturnValue) {
            result = Math.log10(Math.abs(result)) > config.maxNoiseLogarithmValue ? Math.pow(10, Math.log10(Math.abs(result)) - Math.floor(Math.log10(Math.abs(result)) - config.maxNoiseLogarithmValue)) * Math.signum(result) : result;
        }
        cir.setReturnValue(result);
    }
}
