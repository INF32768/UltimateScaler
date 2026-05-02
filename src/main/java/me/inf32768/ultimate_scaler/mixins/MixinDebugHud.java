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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link DebugHud} 类的 Mixin，用于在调试屏幕中添加偏移于缩放后的坐标 {@code TerrainPos}.
 */
@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
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
        // 计算坐标并添加到信息列表中
        // TODO: 可以将计算好的坐标缓存，仅在摄像机坐标或便宜设置变化时重新计算，以此提高性能
        if (config.showTerrainPos) {
            if (config.bigIntegerRewrite) {
                String x = Util.RepositionBigDecimal(pos.getX(), Direction.Axis.X).toString();
                String y = Util.RepositionBigDecimal(pos.getY(), Direction.Axis.Y).toString();
                String z = Util.RepositionBigDecimal(pos.getZ(), Direction.Axis.Z).toString();
                list.add(String.format(Locale.ROOT, "TerrainXYZ: %s %s %s", x, y, z));
                list.add(String.format(Locale.ROOT, "TerrainXYZ (double): %.0f %.0f %.0f", Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z)));
            } else {
                double x = Util.RepositionDouble(pos.getX(), Direction.Axis.X);
                double y = Util.RepositionDouble(pos.getY(), Direction.Axis.Y);
                double z = Util.RepositionDouble(pos.getZ(), Direction.Axis.Z);
                list.add(String.format(Locale.ROOT, "TerrainXYZ: %.0f %.0f %.0f", x, y, z));
            }
        }
    }
}