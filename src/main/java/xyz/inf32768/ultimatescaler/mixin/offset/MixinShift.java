package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.ConfigManager;

/**
 * {@code DensityFunctionTypes.Shift} 类的 Mixin，用于对密度函数 {@code minecraft:shift} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$Shift")
public abstract class MixinShift {
    /**
     * 施加偏移与缩放。
     */
    @ModifyArgs(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunctions$Shift;compute(DDD)D"))
    private void modifyArgs(Args args, DensityFunction.FunctionContext pos) {
        double x = ConfigManager.getConfig().worldGen.reposition.highPrecisionMode ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X);
        double y = ConfigManager.getConfig().worldGen.reposition.highPrecisionMode ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y);
        double z = ConfigManager.getConfig().worldGen.reposition.highPrecisionMode ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z);
        args.set(0, x);
        args.set(1, y);
        args.set(2, z);
    }
}
