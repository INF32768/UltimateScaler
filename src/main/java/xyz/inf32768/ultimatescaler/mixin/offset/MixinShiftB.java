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
 * {@code DensityFunctionTypes.ShiftB} 类的 Mixin，用于对密度函数 {@code minecraft:shift_b} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$ShiftB")
public abstract class MixinShiftB {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunctions$ShiftB;compute(DDD)D"))
    private void modifyArgs(Args args, DensityFunction.FunctionContext pos) {
        double x = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X);
        double z = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z);
        args.set(0, z);
        args.set(1, x);
    }
}
