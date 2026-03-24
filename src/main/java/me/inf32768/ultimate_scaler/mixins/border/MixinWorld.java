package me.inf32768.ultimate_scaler.mixins.border;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link World} 类的 Mixin。
 */
@Mixin(World.class)
public abstract class MixinWorld {
    /**
     * 修改 {@link World#isValid} 方法。
     * <p>
     * <strong>原版问题：</strong>在生成末地城、末地折跃门等结构时，以及部分命令的坐标参数中，游戏会把 X/Z 不在 {@code [-30000000, 30000000)} 或 Y 不在 {@code [-20000000, 20000000)} 范围内的坐标视为无效坐标。
     * <p>
     * 若这些结构尝试在此范围之外生成，则生成失败；若命令的坐标参数超出此范围，则命令无法执行。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，移除坐标合法性检查，使得游戏不再视坐标为无效。
     */
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private static void modifyIsValid(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (config.expandWorldBorder) {
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
    @Inject(method = "isValidHorizontally", at = @At("HEAD"), cancellable = true)
    private static void modifyIsValidHorizontally(CallbackInfoReturnable<Boolean> cir) {
        if (config.expandWorldBorder) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 修改 {@link World#getTopY} 方法。
     * <p>
     * <strong>原版问题：</strong>在 X/Z 坐标不在 {@code [-30000000, 30000000)} 范围内的区域中，游戏不会正常计算高度图（当前在玩家当前X/Z坐标处存在的最高特定方块的Y坐标），而是直接返回海平面高度 + 1。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，移除坐标检查，使得世界边界外的高度图也能正确计算。
     */
    @ModifyConstant(method = "getTopY", constant = @Constant(intValue = -30000000))
    private int modifyMinCoordinate(int original) {
        return config.expandWorldBorder ? Integer.MIN_VALUE : original;
    }

    /**
     * @see MixinWorld#modifyMinCoordinate(int)
     */
    @ModifyConstant(method = "getTopY", constant = @Constant(intValue = 30000000))
    private int modifyMaxCoordinate(int original) {
        return config.expandWorldBorder ? Integer.MAX_VALUE : original;
    }
}
