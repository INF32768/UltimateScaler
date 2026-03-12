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
    /**
     * （仅客户端）在标题画面初始化时初始化配置
     * <p>
     * 在模组入口加载时，本地化系统还未初始化，此时写入配置文件会导致其中的注释变为原始的键名，因此配置初始化需要在标题画面初始化时进行（此时本地化系统已初始化）。
     * <p>
     * 而服务端中没有本地化系统，语言固定为英文，因此配置初始化可直接在模组入口加载时进行。
     * @throws IOException 配置文件读取出错
     */
    @Inject(method = "init", at = @At("HEAD"))
    private void modifyTitleScreen(CallbackInfo info) throws IOException {
            UltimateScalerOptions.saveConfig();
    }
}
