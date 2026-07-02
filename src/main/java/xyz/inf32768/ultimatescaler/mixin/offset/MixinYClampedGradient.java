package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@code DensityFunctionTypes.YClampedGraident} 类的 Mixin，用于对密度函数 {@code minecraft:y_clamped_gradient} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$YClampedGradient")
public abstract class MixinYClampedGradient {
    /**
     * 若 {@code extraYOffset} 选项开启，则施加偏移与缩放。
     */
    @ModifyArgs(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clampedMap(DDDDD)D"))
    private void modifyArgs(Args args, DensityFunction.FunctionContext pos) {
        if (Config.impl.extraYOffset) {
            double y = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y);
            args.set(0, y);
        }
    }
}
