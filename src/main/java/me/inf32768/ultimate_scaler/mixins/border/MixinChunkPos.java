package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link ChunkPos} 类的 Mixin。
 */
@Mixin(ChunkPos.class)
public abstract class MixinChunkPos {
    /**
     * 修改 {@link ChunkPos#MARKER} 字段。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，X/Z 坐标的绝对值超过 30001056 的区块不会生成。
     * <p>
     * <strong>解决方案：</strong>修改相关字段，解除上述限制，使区块可以在上述限制范围外生成（仅在启用了“扩展世界边界”选项时生效）。
     */
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkPos;toLong(II)J"))
    private static long modifyToLong(int x, int z) {
        return config.expandWorldBorder ? ChunkPos.toLong(134217728, 134217728) : ChunkPos.toLong(x, z);
    }
}
