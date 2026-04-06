package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.server.command.WorldBorderCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link WorldBorderCommand} 类的 Mixin。
 */
@Mixin(WorldBorderCommand.class)
public abstract class MixinWorldBorderCommand {
    /**
     * 修改 {@link WorldBorderCommand#register} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，使用 {@code /worldborder add} 和 {@code /worldborder set} 命令时，如果指定的半径参数的绝对值大于 59999968，则会报错。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使世界边界的半径可以任意大（仅在启用了“扩展世界边界”选项时生效）。
     */
    @ModifyArgs(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/arguments/DoubleArgumentType;doubleArg(DD)Lcom/mojang/brigadier/arguments/DoubleArgumentType;", remap = false))
    private static void modifyDoubleArg(Args args) {
        if (config.expandWorldBorder) {
            args.set(0,Double.NEGATIVE_INFINITY);
            args.set(1,Double.POSITIVE_INFINITY);
        }
    }

    /**
     * 修改 {@code WorldBorderCommand#executeCenter} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，使用 {@code /worldborder center} 命令设置世界边界的中心时，如果指定的坐标参数的绝对值大于 29999984，则会报错。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使世界边界的中心可以设置到任意位置（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "executeCenter", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(F)F"))
    private static float modifyAbs(float value) {
        return config.expandWorldBorder ? 0F : Math.abs(value);
    }

    /**
     * 修改 {@code WorldBorderCommand#executeSet} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，使用 {@code /worldborder add} 和 {@code /worldborder set} 命令时，如果将要把世界边界的半径设置为大于 59999968 或者小于 1 ，则会报错。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使世界边界的半径可以任意大（仅在启用了“扩展世界边界”选项时生效）。
     */
    @ModifyConstant(method = "executeSet", constant = @Constant(doubleValue = 1.0))
    private static double modifyConstant(double original) {
        return config.expandWorldBorder ? 0D : original;
    }

    /**
     * @see MixinWorldBorderCommand#modifyConstant(double)
     */
    @ModifyConstant(method = "executeSet", constant = @Constant(doubleValue = 59999968))
    private static double modifyConstant2(double original) {
        return config.expandWorldBorder ? Double.POSITIVE_INFINITY : original;
    }
}
