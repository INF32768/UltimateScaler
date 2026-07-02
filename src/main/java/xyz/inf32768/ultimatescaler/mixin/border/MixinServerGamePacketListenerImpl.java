package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link ServerGamePacketListenerImpl} 类的 Mixin。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
    /**
     * 修改 {@code ServerGamePacketListenerImpl#clampHorizontal} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在服务器中，在计算载具移动时，其 X/Z 坐标会被限制在 {@code [-30000000, 30000000]} 的范围内，导致如果玩家骑乘实体来到此边界之外，服务端仍认为其仍在边界内，导致不同步。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，载具可以正常到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "clampHorizontal", at = @At("HEAD"), cancellable = true)
    private static void modifyClampHorizontal(double d, CallbackInfoReturnable<Double> cir) {
        if (Config.impl.expandWorldBorder) {
            cir.setReturnValue(d);
        }
    }

    /**
     * 修改 {@code ServerGamePacketListenerImpl#clampVertical} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，在服务器中，在计算载具移动时，其 Y 坐标会被限制在 {@code [-20000000, 20000000]} 的范围内，导致如果玩家骑乘实体来到此边界之外，服务端仍认为其仍在边界内，出现不同步。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，载具可以正常到达边界之外（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "clampVertical", at = @At("HEAD"), cancellable = true)
    private static void modifyClampVertical(double d, CallbackInfoReturnable<Double> cir) {
        if (Config.impl.expandWorldBorder) {
            cir.setReturnValue(d);
        }
    }
}
