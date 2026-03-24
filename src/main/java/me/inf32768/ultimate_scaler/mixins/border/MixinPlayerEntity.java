package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link PlayerEntity} 类的 Mixin。
 */
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    /**
     * 修改 {@link PlayerEntity#tick} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，玩家的 X/Z 坐标会在更新时被限制在 {@code -29999999, 29999999} 的范围内，导致在边界处形成一堵“空气墙”。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，玩家可以到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D"))
    private double redirectClamp(double value, double min, double max) {
        if (config.expandWorldBorder) {
            return value;
        } else {
            return MathHelper.clamp(value, min, max);
        }
    }
}
