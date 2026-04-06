package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;


@Mixin(Entity.class)
public abstract class MixinEntityBefore1_21_6 {
    /**
     * 修改 {@link Entity#readData} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，实体在被加载时（从 NBT 中读取坐标数据时），X/Z 坐标会被限制在 {@code [-30000512, 30000512]} 的范围内，Y 坐标会被限制在 {@code [-20000000, 20000000]} 的范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制（仅在启用了“扩展世界边界”选项时生效）。
     * <p>
     * 注：在 1.21.6 及以上版本的 Minecraft 中，这个方法改名叫做 {@code readData}，因此需要版本隔离。
     */
    @Redirect(method = "method_5651", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3532;method_15350(DDD)D", remap = false), remap = false)
    private static double modifyClampX(double value, double min, double max) {
        if (config.expandWorldBorder) {
            return value;
        } else {
            return MathHelper.clamp(value, min, max);
        }
    }
}
