package me.inf32768.ultimate_scaler.option;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.inf32768.ultimate_scaler.UltimateScaler;

/**
 * Mod Menu 的集成类，用于在模组菜单内添加配置界面的入口。
 */
public class ModMenuIntegration implements ModMenuApi {
    /**
     * 定义配置界面入口。
     * @return 配置界面工厂，供 Mod Menu 使用。
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (UltimateScaler.IS_CLOTH_CONFIG_PRESENT) {
            // 仅在 Cloth Config API 安装时才可打开配置界面
            return (screen) -> ClothConfigBuilder.getConfigBuilder().setParentScreen(screen).build();
        } else {
            return null;
        }
    }
}
