package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link DedicatedServerProperties} 类的 Mixin。
 */
@Mixin(DedicatedServerProperties.class)
public abstract class MixinDedicatedServerProperties {
    /**
     * 修改 {@link DedicatedServerProperties#maxWorldSize} 字段。
     * <p>
     * <strong>原版问题：</strong>在读取服务器配置文件 {@code server.properties} 时，{@code max-world-size} 字段的取值被限制在了 {@code [1, 29999984]} 之间，导致世界边界的半径也被限制在了这个范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，可自由设定世界边界的大小（仅在启用了“扩展世界边界”选项时生效）。
     */
    @ModifyArgs(method = "method_16715", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private static void modifyMaxWorldSize(Args args) {
        if (Config.impl.expandWorldBorder) {
            args.set(1, Integer.MIN_VALUE);
            args.set(2, Integer.MAX_VALUE);
        }
    }
}
