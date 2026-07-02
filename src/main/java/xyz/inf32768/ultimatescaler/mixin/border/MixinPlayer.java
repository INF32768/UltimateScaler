package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link Player} 类的 Mixin。
 */
@Mixin(Player.class)
public abstract class MixinPlayer {
    /**
     * 修改 {@link Player#tick} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，玩家的 X/Z 坐标会在更新时被限制在 {@code -29999999, 29999999} 的范围内，导致在边界处形成一堵“空气墙”。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，玩家可以到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private double redirectClamp(double value, double min, double max) {
        if (Config.impl.expandWorldBorder) {
            return value;
        } else {
            return Mth.clamp(value, min, max);
        }
    }
}
