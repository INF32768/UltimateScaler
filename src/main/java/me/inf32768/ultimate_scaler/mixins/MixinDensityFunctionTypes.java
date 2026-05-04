package me.inf32768.ultimate_scaler.mixins;

import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link DensityFunctionTypes} 类的 Mixin，用于扩展数据包密度函数定义中的常数值的取值范围。
 */
@Mixin(DensityFunctionTypes.class)
public abstract class MixinDensityFunctionTypes {
    /**
     * 修改 {@code DensityFunctionTypes.CONSTANT_RANGE} 字段。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，密度函数中常数项的取值范围都被限制在了 {@code [-1000000, 1000000]} 之间。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使得参数范围可以为任意值（仅在启用了 {@code expandDatapackValueRange} 选项时生效）。
     */
    @ModifyArgs(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;doubleRange(DD)Lcom/mojang/serialization/Codec;"))
    private static void modifyConstantRange(Args args) {
        if (config.expandDatapackValueRange) {
            args.set(0, Double.NEGATIVE_INFINITY);
            args.set(1, Double.POSITIVE_INFINITY);
        }
    }
}
