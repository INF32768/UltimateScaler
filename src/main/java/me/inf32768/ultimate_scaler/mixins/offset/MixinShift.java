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
 * {@code DensityFunctionTypes.Shift} 类的 Mixin，用于对密度函数 {@code minecraft:shift} 施加偏移和缩放。
 */
@Mixin(DensityFunctionTypes.Shift.class)
public abstract class MixinShift {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "sample", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunctionTypes$Shift;sample(DDD)D"))
    private void modifyArgs(Args args, DensityFunction.NoisePos pos) {
        double x = config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X);
        double y = config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y);
        double z = config.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z);
        args.set(0, x);
        args.set(1, y);
        args.set(2, z);
    }
}
