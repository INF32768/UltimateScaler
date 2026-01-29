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

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4), method = "getLeftText")
    protected void getLeftText(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockPos pos = null;
        if (mc.getCameraEntity() != null) {
            pos = mc.getCameraEntity().getBlockPos();
        }
        if (pos == null) {
            return;
        }

        if (config.showTerrainPos) {
            if (config.bigIntegerRewrite) {
                String x = Util.getBigIntegerOffsetPos(pos.getX(), Direction.Axis.X).toString();
                String y = Util.getBigIntegerOffsetPos(pos.getY(), Direction.Axis.Y).toString();
                String z = Util.getBigIntegerOffsetPos(pos.getZ(), Direction.Axis.Z).toString();
                list.add(String.format(Locale.ROOT, "TerrainXYZ: %s %s %s", x, y, z));
                list.add(String.format(Locale.ROOT, "TerrainXYZ (double): %.0f %.0f %.0f", Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z)));
            } else {
                double x = Util.getDoubleOffsetPos(pos.getX(), Direction.Axis.X);
                double y = Util.getDoubleOffsetPos(pos.getY(), Direction.Axis.Y);
                double z = Util.getDoubleOffsetPos(pos.getZ(), Direction.Axis.Z);
                list.add(String.format(Locale.ROOT, "TerrainXYZ: %.0f %.0f %.0f", x, y, z));
            }
        }
    }
}