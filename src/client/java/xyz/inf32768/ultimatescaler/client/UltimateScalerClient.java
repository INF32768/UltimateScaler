package xyz.inf32768.ultimatescaler.client;

import net.fabricmc.api.ClientModInitializer;
import xyz.inf32768.ultimatescaler.ModMetadata;
import xyz.inf32768.ultimatescaler.client.config.KeyBindings;

/**
 * 客户端主入口，负责初始化模组的仅客户端功能。
 * <p>
 * 目前仅用于注册按键绑定。
 * @see xyz.inf32768.ultimatescaler.UltimateScaler 通用主入口
 */
public class UltimateScalerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (ModMetadata.IS_FABRIC_API_PRESENT) {
			KeyBindings.init();
		}
	}
}