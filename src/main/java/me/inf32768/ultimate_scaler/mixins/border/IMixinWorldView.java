package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link WorldView} 接口的 Mixin。
 */
@Mixin(WorldView.class)
public interface IMixinWorldView {
    /**
     * 修改 {@link WorldView#getLightLevel(BlockPos, int)} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，X/Z 坐标超出区间 {@code [-30000000, 30000000)} 的区域不会正常计算光照强度，而是返回固定值 15，导致光照错误。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，使其在超出边界时正常计算并返回光照强度（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "getLightLevel(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    private void modifyLightLevel(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> cir) {
        if (config.expandWorldBorder) {
            cir.setReturnValue(((BlockRenderView) this).getBaseLightLevel(pos, ambientDarkness));
        }
    }
}
