package me.inf32768.ultimate_scaler.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import me.inf32768.ultimate_scaler.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link DebugHud} 类的 Mixin，用于在调试屏幕中添加偏移于缩放后的坐标 {@code TerrainPos}.
 */
@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    // 缓存变量，仅在任意变量变化时重新计算，可大幅提高静息性能
    @Unique
    private static BlockPos previousCamPos;
    @Unique
    private static BigDecimal[] previousOffset;
    @Unique
    private static BigDecimal[] previousScale;
    @Unique
    private static String[] terrainPosLines;

    /**
     * 添加调试信息。
     */
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4), method = "getLeftText")
    protected void getLeftText(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        // 获取摄像机所在的方块的坐标。之所以不获取玩家实体的坐标，是因为在使用某模组的“灵魂出窍”（FreeCam）功能移动时，仅有摄像机坐标变化而玩家坐标不会变化
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockPos pos = null;
        if (mc.getCameraEntity() != null) {
            pos = mc.getCameraEntity().getBlockPos();
        }
        if (pos == null) {
            return;
        }

        // 计算坐标
        if (config.showTerrainPos) {
            if (!pos.equals(previousCamPos) || !Arrays.equals(config.globalBigDecimalOffset, previousOffset) || !Arrays.equals(config.globalBigDecimalScale, previousScale)) {
                // 缓存变量与当前实际不一致，需重新计算并更新缓存
                previousCamPos = pos;
                previousOffset = config.globalBigDecimalOffset;
                previousScale = config.globalBigDecimalScale;

                if (config.bigIntegerRewrite) {
                    String x = Util.RepositionBigDecimal(pos.getX(), Direction.Axis.X).toString();
                    String y = Util.RepositionBigDecimal(pos.getY(), Direction.Axis.Y).toString();
                    String z = Util.RepositionBigDecimal(pos.getZ(), Direction.Axis.Z).toString();
                    terrainPosLines = new String[]{String.format(Locale.ROOT, "TerrainXYZ: %s %s %s", x, y, z), String.format(Locale.ROOT, "TerrainXYZ (double): %.0f %.0f %.0f", Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z))};
                } else {
                    double x = Util.RepositionDouble(pos.getX(), Direction.Axis.X);
                    double y = Util.RepositionDouble(pos.getY(), Direction.Axis.Y);
                    double z = Util.RepositionDouble(pos.getZ(), Direction.Axis.Z);
                    terrainPosLines = new String[]{String.format(Locale.ROOT, "TerrainXYZ: %.0f %.0f %.0f", x, y, z), null};
                }
            }

            // 将条目添加到信息列表中
            if (config.bigIntegerRewrite) {
                list.add(terrainPosLines[0]);
                list.add(terrainPosLines[1]);
            } else {
                list.add(terrainPosLines[0]);
            }
        }
    }
}