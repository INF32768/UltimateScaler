package xyz.inf32768.ultimatescaler.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.inf32768.ultimatescaler.config.ConfigManager;

/**
 * {@link PerlinNoise} 类的 Mixin，用于修改坐标变换算法，进而修改边境之地的位置。
 */
@Mixin(PerlinNoise.class)
public abstract class MixinPerlinNoise {
    /**
     * 根据配置项 {@code farLandsPos} 等的值修改算法。关于具体的工作原理和目的，请参见 Wiki 上的文章。
     *
     * @see <a href="https://github.com/INF32768/UltimateScaler/wiki/Reference.maintainPrecision.zh">《maintainPrecision 方法》</a>
     */
    @Inject(method = "wrap", at = @At("HEAD"), cancellable = true)
    private static void modifyMaintainPrecision(double value, CallbackInfoReturnable<Double> cir) {
        double result = switch (ConfigManager.getConfig().worldGen.farLandsControl.farLandsPos) {
            case BETA -> value;
            case RELEASE ->
                    Math.abs(value) > Long.MAX_VALUE ? value - Math.signum(value) * Long.MAX_VALUE : (value + 1.6777216E7D) % 3.3554432E7D - 1.6777216E7D;
            case REMOVED -> (value + 1.6777216E7D) % 3.3554432E7D - 1.6777216E7D;
            case DEFAULT ->
                    value - (double) Mth.lfloor(value / ConfigManager.getConfig().worldGen.farLandsControl.customDivisor + (double) 0.5F) * ConfigManager.getConfig().worldGen.farLandsControl.customDivisor;
        };
        if (ConfigManager.getConfig().worldGen.farLandsControl.limitReturnValue) {
            result = Math.log10(Math.abs(result)) > ConfigManager.getConfig().worldGen.farLandsControl.maxLogarithmValue ? Math.pow(10, Math.log10(Math.abs(result)) - Math.floor(Math.log10(Math.abs(result)) - ConfigManager.getConfig().worldGen.farLandsControl.maxLogarithmValue)) * Math.signum(result) : result;
        }
        cir.setReturnValue(result);
    }
}
