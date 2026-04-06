package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.server.command.ForceLoadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link ForceLoadCommand} 类的 Mixin。
 */
@Mixin(ForceLoadCommand.class)
public abstract class MixinForceLoadCommand {
    /**
     * 修改 {@code ForceLoadCommand.executeChange} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在使用 {@code forceload} 命令时，如果指定的 X/Z 坐标超出区间 {@code [-30000000, 30000000)}，则会执行失败，提示“该位置已超出此世界！”。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，可以在任意位置设置强制加载区块（仅在启用了“扩展世界边界”选项时生效）。
     */
    @ModifyConstant(method = "executeChange", constant = @Constant(intValue = -30000000))
    private static int modifyMinCoordinate(int original) {
        return config.expandWorldBorder ? Integer.MIN_VALUE : original;
    }

    /**
     * @see MixinForceLoadCommand#modifyMinCoordinate(int)
     */
    @ModifyConstant(method = "executeChange", constant = @Constant(intValue = 30000000))
    private static int modifyMaxCoordinate(int original) {
        return config.expandWorldBorder ? Integer.MAX_VALUE : original;
    }
}
