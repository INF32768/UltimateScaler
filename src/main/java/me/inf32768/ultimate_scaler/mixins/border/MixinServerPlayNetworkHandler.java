package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link ServerPlayNetworkHandler} 类的 Mixin。
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    /**
     * 修改 {@code ServerPlayNetworkHandler#clampHorizontal} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在服务器中，在计算载具移动时，其 X/Z 坐标会被限制在 {@code [-30000000, 30000000]} 的范围内，导致如果玩家骑乘实体来到此边界之外，服务端仍认为其仍在边界内，导致不同步。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，载具可以正常到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "clampHorizontal", at = @At("HEAD"), cancellable = true)
    private static void modifyClampHorizontal(double d, CallbackInfoReturnable<Double> cir) {
        if (config.expandWorldBorder) {
            cir.setReturnValue(d);
        }
    }

    /**
     * 修改 {@code ServerPlayNetworkHandler#clampVertical} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在服务器中，在计算载具移动时，其 Y 坐标会被限制在 {@code [-20000000, 20000000]} 的范围内，导致如果玩家骑乘实体来到此边界之外，服务端仍认为其仍在边界内，出现不同步。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，载具可以正常到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "clampVertical", at = @At("HEAD"), cancellable = true)
    private static void modifyClampVertical(double d, CallbackInfoReturnable<Double> cir) {
        if (config.expandWorldBorder) {
            cir.setReturnValue(d);
        }
    }
}
