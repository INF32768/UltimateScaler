package xyz.inf32768.ultimatescaler.mixin.offset;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.inf32768.ultimatescaler.Util;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@code DensityFunctionTypes.WeirdScaledSampler} 类的 Mixin，用于对密度函数 {@code minecraft:weird_scaled_sampler} 施加偏移和缩放。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$WeirdScaledSampler")
public abstract class MixinWeirdScaledSampler {
    /**
     * 应用偏移与缩放
     */
    @ModifyArgs(method = "transform", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunction$NoiseHolder;getValue(DDD)D"))
    private void modifyNoiseSampleArgs(Args args, DensityFunction.FunctionContext pos, double density, @Local(ordinal = 1) double rarity) {
        // 计算并应用偏移与缩放
        double x = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockX(), Direction.Axis.X).doubleValue() : Util.RepositionDouble(pos.blockX(), Direction.Axis.X);
        double y = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockY(), Direction.Axis.Y).doubleValue() : Util.RepositionDouble(pos.blockY(), Direction.Axis.Y);
        double z = Config.impl.bigIntegerRewrite ? Util.RepositionBigDecimal(pos.blockZ(), Direction.Axis.Z).doubleValue() : Util.RepositionDouble(pos.blockZ(), Direction.Axis.Z);
        args.set(0, x / rarity);
        args.set(1, y / rarity);
        args.set(2, z / rarity);
    }
}
