package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link ServerPropertiesHandler} 类的 Mixin。
 */
@Mixin(ServerPropertiesHandler.class)
public abstract class MixinServerPropertiesHandler {
    /**
     * 修改 {@link ServerPropertiesHandler#maxWorldSize} 字段。
     * <p>
     * <strong>原版问题：</strong>在读取服务器配置文件 {@code server.properties} 时，{@code max-world-size} 字段的取值被限制在了 {@code [1, 29999984]} 之间，导致世界边界的半径也被限制在了这个范围内。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，可自由设定世界边界的大小（仅在启用了“扩展世界边界”选项时生效）。
     */
    // TODO: 可以利用 @Redirect 注解重定向 transformedParseInt 方法为 getInt 方法，可避免 @ModifyConstant 注解的使用
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 29999984))
    private int modifyMaxWorldSize(int original) {
        return config.expandWorldBorder ? Integer.MAX_VALUE : original;
    }
}
