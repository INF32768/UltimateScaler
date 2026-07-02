package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link LevelReader} 接口的 Mixin。
 */
@Mixin(LevelReader.class)
public interface IMixinLevelReader {
    /**
     * 修改 {@link LevelReader#getMaxLocalRawBrightness(BlockPos, int)} 方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，X/Z 坐标超出区间 {@code [-30000000, 30000000)} 的区域不会正常计算光照强度，而是返回固定值 15，导致光照错误。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，使其在超出边界时正常计算并返回光照强度（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Inject(method = "getMaxLocalRawBrightness(Lnet/minecraft/core/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    private void modifyLightLevel(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> cir) {
        if (Config.impl.expandWorldBorder) {
            cir.setReturnValue(((BlockAndTintGetter) this).getRawBrightness(pos, ambientDarkness));
        }
    }
}
