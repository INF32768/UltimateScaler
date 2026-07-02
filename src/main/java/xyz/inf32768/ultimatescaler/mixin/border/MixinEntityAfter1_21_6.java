package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link Entity} 类的 Mixin，仅在大于等于 1.21.6 的版本中生效。
 *
 * @see MixinEntityBefore1_21_6
 */
@Mixin(Entity.class)
public abstract class MixinEntityAfter1_21_6 {
    /**
     * 修改 {@link Entity#load} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，实体在被加载时（从 NBT 中读取坐标数据时），X/Z 坐标会被限制在 {@code [-30000512, 30000512]} 的范围内，Y 坐标会被限制在 {@code [-20000000, 20000000]} 的范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制（仅在启用了“扩展世界边界”选项时生效）。
     * <p>
     * 注：在 1.21.5 及以下版本的 Minecraft 中，这个方法叫做 {@code readNbt}，因此需要版本隔离。
     */
    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private static double modifyClampX(double value, double min, double max) {
        if (Config.impl.expandWorldBorder) {
            return value;
        } else {
            return Mth.clamp(value, min, max);
        }
    }
}
