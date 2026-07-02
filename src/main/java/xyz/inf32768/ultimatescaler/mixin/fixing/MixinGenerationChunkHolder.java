package xyz.inf32768.ultimatescaler.mixin.fixing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link GenerationChunkHolder} 类的 Mixin。
 */
@Mixin(GenerationChunkHolder.class)
public abstract class MixinGenerationChunkHolder {
    /**
     * 修改 {@link GenerationChunkHolder} 类的构造方法。
     * <p>
     * <strong>原版问题：</strong>由于人为限制，尝试在 X/Z 坐标的绝对值超过 33552992 的区域创建区块会导致游戏崩溃。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，解除上述限制，可以在更远处创建区块（仅在启用了 {@code fixChunkGenerationOutOfBound} 选项时生效）。
     */
    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;getChessboardDistance(Lnet/minecraft/world/level/ChunkPos;)I"))
    private void modifyInit(Args args, ChunkPos pos) {
        if (Config.impl.fixChunkGenerationOutOfBound) {
            args.set(0, pos);
        }
    }
}