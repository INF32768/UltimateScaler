package xyz.inf32768.ultimatescaler.mixin.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * {@link Level} 类的 Mixin。
 */
@Mixin(Level.class)
public abstract class MixinLevel {
    /**
     * 修改 {@link Level#isInSpawnableBounds} 方法。
     * <p>
     * <strong>原版问题：</strong>在生成末地城、末地折跃门等结构时，以及部分命令的坐标参数中，游戏会把 X/Z 不在 {@code [-30000000, 30000000)} 或 Y 不在 {@code [-20000000, 20000000)} 范围内的坐标视为无效坐标。
     * <p>
     * 若这些结构尝试在此范围之外生成，则生成失败；若命令的坐标参数超出此范围，则命令无法执行。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，移除坐标合法性检查，使得游戏不再视坐标为无效。
     */
    @Inject(method = "isInSpawnableBounds", at = @At("HEAD"), cancellable = true)
    private static void modifyIsValid(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Config.impl.expandWorldBorder) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 修改 {@code World#isValidHorizontally} 方法。
     * <p>
     * <strong>原版问题：</strong>游戏会将 X/Z 坐标不在 {@code [-30000000, 30000000)} 范围内的区域视为不可建造区域，实体（包括玩家）和命令均无法修改此区域中的方块。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，移除坐标合法性检查，使得此区域可被建造。
     */
    @Inject(method = "isInWorldBoundsHorizontal", at = @At("HEAD"), cancellable = true)
    private static void modifyIsValidHorizontally(CallbackInfoReturnable<Boolean> cir) {
        if (Config.impl.expandWorldBorder) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 修改 {@link Level#getHeight} 方法。
     * <p>
     * <strong>原版问题：</strong>在 X/Z 坐标不在 {@code [-30000000, 30000000)} 范围内的区域中，游戏不会正常计算高度图（当前在玩家当前X/Z坐标处存在的最高特定方块的Y坐标），而是直接返回海平面高度 + 1。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，移除坐标检查，使得世界边界外的高度图也能正确计算。
     */
    @ModifyConstant(method = "getHeight", constant = @Constant(intValue = -30000000))
    private int modifyMinCoordinate(int original) {
        return Config.impl.expandWorldBorder ? Integer.MIN_VALUE : original;
    }

    /**
     * @see MixinLevel#modifyMinCoordinate(int)
     */
    @ModifyConstant(method = "getHeight", constant = @Constant(intValue = 30000000))
    private int modifyMaxCoordinate(int original) {
        return Config.impl.expandWorldBorder ? Integer.MAX_VALUE : original;
    }
}
