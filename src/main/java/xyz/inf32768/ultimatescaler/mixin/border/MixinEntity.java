package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link Entity} 类的 Mixin。
 */
@Mixin(Entity.class)
public abstract class MixinEntity {
    /**
     * 修改 {@link Entity#absSnapTo} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，实体的 X/Z 坐标会在更新时被限制在 [-30000000, 30000000] 的范围内，导致在边界处形成一堵“空气墙”。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，实体可以到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "absSnapTo(DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private double modifyClamp(double value, double min, double max) {
        if (Config.impl.expandWorldBorder) {
            return value;
        } else {
            return Mth.clamp(value, min, max);
        }
    }
}
