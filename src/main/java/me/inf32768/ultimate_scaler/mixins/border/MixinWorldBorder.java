package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link WorldBorder} 类的 Mixin。
 */
@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {
    /**
     * {@link WorldBorder#setMaxRadius} 方法的影子，用于在 {@link #onInit} 方法中修改最大半径。
     */
    @Shadow
    public abstract void setMaxRadius(int maxRadius);

    /**
     * 修改 {@link WorldBorder} 类的构造方法。
     * <p>
     * <strong>原版问题：</strong>世界边界在创建时，默认的最大半径为 29999984，这会导致无法通过命令等方式设置更大的边界。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，使默认的最大边界半径由 29999984 改为 {@link Integer#MAX_VALUE}（仅在启用了“扩展世界边界”选项时生效）。
     * <p>
     * 注：通常情况下，内置服务端在启动时会把世界边界最大半径设置为 29999984，并覆盖默认值，这个在 {@link MixinMinecraftServer} 中已经修改。
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (config.expandWorldBorder) {
            this.setMaxRadius(Integer.MAX_VALUE);
        }
    }
}