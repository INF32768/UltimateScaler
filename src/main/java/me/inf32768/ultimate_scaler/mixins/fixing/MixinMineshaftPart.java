package me.inf32768.ultimate_scaler.mixins.fixing;

import com.llamalad7.mixinextras.sugar.Local;
import me.inf32768.ultimate_scaler.util.Util;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@code MineshaftPart} 类的 Mixin。
 */
@Mixin(targets = "net.minecraft.structure.MineshaftGenerator$MineshaftPart")
public abstract class MixinMineshaftPart {
    /**
     * 修改 {@code MineshaftPart.cannotGenerate} 方法。
     * <p>
     * <strong>原版问题：</strong>尝试在 X/Y/Z 坐标的绝对值大于 1073741824 的位置生成废弃矿井会导致游戏崩溃，这是因为传统的平均数算法出现了整数溢出。
     * <p>
     * <strong>解决方案：</strong>修改相关方法，改用基于位运算的平均数算法，使废弃矿井可以在更远处生成（仅在启用了 {@code fixMineshaftCannotGenerate} 选项时生效）。
     * @see Util#average(int, int)
     */
    @ModifyVariable(method = "cannotGenerate", at = @At("STORE"))
    private static BlockPos.Mutable modifyPos(
        BlockPos.Mutable pos,
        @Local(type = Integer.class, ordinal = 0) int i,
        @Local(type = Integer.class, ordinal = 1) int j,
        @Local(type = Integer.class, ordinal = 2) int k,
        @Local(type = Integer.class, ordinal = 3) int l,
        @Local(type = Integer.class, ordinal = 4) int m,
        @Local(type = Integer.class, ordinal = 5) int n
    ) {
        return config.fixMineshaftCannotGenerate ? new BlockPos.Mutable(Util.average(i, l), Util.average(j, m), Util.average(k, n)) : new BlockPos.Mutable((i + l) / 2, (j + m) / 2, (k + n) / 2);
    }
}
