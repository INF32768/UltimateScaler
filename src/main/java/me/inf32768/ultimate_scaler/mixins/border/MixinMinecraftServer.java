package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link MinecraftServer} 类的 Mixin。
 */
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    /**
     * 修改 {@link  MinecraftServer#getMaxWorldBorderRadius} 方法。
     * <p>
     * <strong>原版问题：</strong>单人游戏中，内置服务端在启动时会把世界边界的最大半径设置为 29999984，这会导致无法通过命令等方式设置更大的边界。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，使其设置的最大边界半径由 29999984 改为 {@link Integer#MAX_VALUE}（仅在启用了“扩展世界边界”选项时生效）。
     * <p>
     * 注：即使内置服务端没有设置，世界边界对象在创建时，最大半径的默认值也为 29999984，这个在 {@link MixinWorldBorder} 中已经修改。
     */
    @Inject(method = "getMaxWorldBorderRadius", at = @At("HEAD"), cancellable = true)
    private void modifyMaxWorldBorderRadius(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(config.expandWorldBorder ? Integer.MAX_VALUE : 29999984);
    }
}
