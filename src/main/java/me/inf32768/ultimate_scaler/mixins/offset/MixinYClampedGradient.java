package me.inf32768.ultimate_scaler.mixins.offset;

import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@code DensityFunctionTypes.YClampedGraident} 类的 Mixin，用于对密度函数 {@code minecraft:y_clamped_gradient} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.gen.densityfunction.DensityFunctionTypes$YClampedGradient")
public abstract class MixinYClampedGradient {
    /**
     * 若 {@code extraYOffset} 选项开启，则施加偏移与缩放。
     */
    @ModifyArgs(method = "sample", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clampedMap(DDDDD)D"))
    private void modifyArgs(Args args, DensityFunction.NoisePos pos) {
        if (config.extraYOffset) {
            double y = config.bigIntegerRewrite ? Util.getBigIntegerOffsetPos(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.getDoubleOffsetPos(pos.blockY(), Direction.Axis.Y);
            args.set(0, y);
        }
    }
}
