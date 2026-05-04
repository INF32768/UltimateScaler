package me.inf32768.ultimate_scaler.mixins.offset;

import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@code DensityFunctionTypes.Noise} 类的 Mixin，用于对密度函数 {@code minecraft:noise} 施加偏移和缩放。
 */
@Mixin(DensityFunctionTypes.Noise.class)
public abstract class MixinNoiseSampler {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "sample", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunction$Noise;sample(DDD)D"))
    private void modifyNoiseSampleArgs(Args args, DensityFunction.NoisePos pos) {
        double x = (config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X)) * this.getXzScale();
        double y = (config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y)) * this.getYScale();
        double z = (config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z)) * this.getXzScale();

        args.set(0, x);
        args.set(1, y);
        args.set(2, z);
    }

    /**
     * {@code Noise.xzScale} 字段的访问器，用于获取数据中定义的 xz 缩放比例（对应密度函数参数中的 {@code xz_scale}）以应用偏移与缩放。
     */
    @Accessor("xzScale")
    public abstract double getXzScale();

    /**
     * {@code Noise.yScale} 字段的访问器，用于获取数据中定义的 y 缩放比例（对应密度函数参数中的 {@code y_scale}）以应用偏移与缩放。
     */
    @Accessor("yScale")
    public abstract double getYScale();
}
