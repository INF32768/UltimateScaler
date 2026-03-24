package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link WorldBorder.Properties} 类的 Mixin。
 */
@Mixin(WorldBorder.Properties.class)
public abstract class MixinWorldBorderProperties {
    /**
     * 修改 {@link WorldBorder.Properties#fromDynamic} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在保存和加载世界边界属性时，会把中心坐标限制在 {@code [-29999984, 29999984]} 范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使保存和加载时世界边界的中心可以在任意位置（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "fromDynamic", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D"))
    private static double modifyClamp(double value, double min, double max) {
        if (config.expandWorldBorder) {
            return value;
        } else {
            return MathHelper.clamp(value, min, max);
        }
    }
}
