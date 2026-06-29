package xyz.inf32768.ultimatescaler.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import xyz.inf32768.ultimatescaler.ModMetadata;
import me.shedaniel.clothconfig2.api.Modifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
import xyz.inf32768.ultimatescaler.config.Config;

/**
 * 快捷键实用类，支持识别包含 {@code Ctrl}、{@code Shift} 和 {@code Alt} 的组合按键。
 */
@Environment(EnvType.CLIENT)
public class KeyBindings {
    /**
     * 获取当前 Ctrl 键是否正在被按下，键盘上的两个 Ctrl 键都算数。
     * @return Ctrl 键是否正在被按下。
     */
    private static boolean isCtrlPressed() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    /**
     * 获取当前 Shift 键是否正在被按下，键盘上的两个 Shift 键都算数。
     * @return Shift 键是否正在被按下。
     */
    private static boolean isShiftPressed() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    /**
     * 获取当前 Alt 键是否正在被按下，键盘上的两个 Alt 键都算数。
     * @return Alt 键是否正在被按下。
     */
    private static boolean isAltPressed() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
    }

    /**
     * 初始化并注册快捷键。目前仅有打开配置界面的快捷键。
     */
    public static void init() {
        if (ModMetadata.IS_CLOTH_CONFIG_PRESENT) { // 仅在 Cloth Config API 安装时才可打开配置界面
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                // 不知道为什么，这里的KeyBinding.isPressed()只能检测到一个按键，所以只能用InputConstants.isKeyDown()来判断组合按键是否被按下
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), Config.impl.optionMenuKeyCode) && Minecraft.getInstance().screen == null) {
                    if (isCtrlPressed() == Modifier.of(Config.impl.optionMenuModifierValue).hasControl() && isShiftPressed() == Modifier.of(Config.impl.optionMenuModifierValue).hasShift() && isAltPressed() == Modifier.of(Config.impl.optionMenuModifierValue).hasAlt()) {
                        Minecraft.getInstance().setScreen(ClothConfigBuilder.getConfigBuilder().build());
                    }
                }
            });
        }
    }
}