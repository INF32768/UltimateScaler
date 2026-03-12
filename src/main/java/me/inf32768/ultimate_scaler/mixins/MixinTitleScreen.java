package me.inf32768.ultimate_scaler.mixins;

import me.inf32768.ultimate_scaler.option.UltimateScalerOptions;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen {
    //客户端更新配置文件
    @Inject(method = "init", at = @At("HEAD"))
    private void modifyTitleScreen(CallbackInfo info) throws IOException {
            UltimateScalerOptions.saveConfig();
    }
}
