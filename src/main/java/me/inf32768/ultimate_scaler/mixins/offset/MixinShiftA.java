package me.inf32768.ultimate_scaler.mixins.offset;

import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@code DensityFunctionTypes.ShiftA} 类的 Mixin，用于对密度函数 {@code minecraft:shift_a} 施加偏移和缩放。
 */
@Mixin(DensityFunctionTypes.ShiftA.class)
public abstract class MixinShiftA {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "sample", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunctionTypes$ShiftA;sample(DDD)D"))
    private void modifyArgs(Args args, DensityFunction.NoisePos pos) {
        double x = config.bigIntegerRewrite ? Util.getBigIntegerOffsetPos(pos.blockX(), Direction.Axis.X).doubleValue() : Util.getDoubleOffsetPos(pos.blockX(), Direction.Axis.X);
        double z = config.bigIntegerRewrite ? Util.getBigIntegerOffsetPos(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.getDoubleOffsetPos(pos.blockZ(), Direction.Axis.Z);
        args.set(0, x);
        args.set(1, 0.0D); // TODO: 默认就是 0 了，不用改
        args.set(2, z);
    }
}
