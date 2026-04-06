package me.inf32768.ultimate_scaler.mixins;

import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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
    // TODO: 下面两项都可以改用 @ModifyArgs 或 @Redirect 注解，避免使用 @ModifyConstant 注解
    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 1000000.0))
    private static double modifyMaxConstantRange(double original) {
        return config.expandDatapackValueRange ? Double.POSITIVE_INFINITY : original;
    }

    /**
     * @see #modifyMaxConstantRange(double)
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = -1000000.0))
    private static double modifyMinConstantRange(double original) {
        return config.expandDatapackValueRange ? Double.NEGATIVE_INFINITY : original;
    }
}
