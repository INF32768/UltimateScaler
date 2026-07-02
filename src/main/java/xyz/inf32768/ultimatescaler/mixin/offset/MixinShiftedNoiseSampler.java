package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@code DensityFunctionTypes.ShiftedNoise} 类的 Mixin，用于对密度函数 {@code minecraft:shifted_noise} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$ShiftedNoise")
public abstract class MixinShiftedNoiseSampler {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunction$NoiseHolder;getValue(DDD)D"))
    private void modifyNoiseSampleArgs(Args args, DensityFunction.FunctionContext pos) {
        double d = (Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X)) * this.getXzScale() + this.getShiftX().compute(pos);
        double e = (Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y)) * this.getYScale() + this.getShiftY().compute(pos);
        double f = (Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z)) * this.getXzScale() + this.getShiftZ().compute(pos);

        args.set(0, d);
        args.set(1, e);
        args.set(2, f);
    }
    /**
     * {@code ShiftedNoise.xzScale} 字段的访问器，用于获取数据中定义的 xz 缩放比例（对应密度函数参数中的 {@code xz_scale}）以应用偏移与缩放。
     */
    @Accessor("xzScale")
    public abstract double getXzScale();
    /**
     * {@code ShiftedNoise.yScale} 字段的访问器，用于获取数据中定义的 y 缩放比例（对应密度函数参数中的 {@code y_scale}）以应用偏移与缩放。
     */
    @Accessor("yScale")
    public abstract double getYScale();
    /**
     * {@code ShiftedNoise.shiftX} 字段的访问器，用于获取数据中定义的 x 偏移量（对应密度函数参数中的 {@code shift_x}）以应用偏移与缩放。
     */
    @Accessor
    public abstract DensityFunction getShiftX();
    /**
     * {@code ShiftedNoise.shiftY} 字段的访问器，用于获取数据中定义的 y 偏移量（对应密度函数参数中的 {@code shift_y}）以应用偏移与缩放。
     */
    @Accessor
    public abstract DensityFunction getShiftY();
    /**
     * {@code ShiftedNoise.shiftY} 字段的访问器，用于获取数据中定义的 z 偏移量（对应密度函数参数中的 {@code shift_z}）以应用偏移与缩放。
     */
    @Accessor
    public abstract DensityFunction getShiftZ();
}
