package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link WorldBorder.Settings} 类的 Mixin。
 */
@Mixin(WorldBorder.Settings.class)
public abstract class MixinWorldBorderSettings {
    /**
     * 修改 {@link WorldBorder.Settings#read} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在保存和加载世界边界属性时，会把中心坐标限制在 {@code [-29999984, 29999984]} 范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，使保存和加载时世界边界的中心可以在任意位置（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private static double modifyClamp(double value, double min, double max) {
        if (Config.impl.expandWorldBorder) {
            return value;
        } else {
            return Mth.clamp(value, min, max);
        }
    }
}
