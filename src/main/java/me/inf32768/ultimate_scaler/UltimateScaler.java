package me.inf32768.ultimate_scaler;

import me.inf32768.ultimate_scaler.commands.LocatePosition;
import me.inf32768.ultimate_scaler.option.ConfigReloader;
import me.inf32768.ultimate_scaler.option.KeyBindings;
import me.inf32768.ultimate_scaler.option.UltimateScalerOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UltimateScaler implements ModInitializer {

public static final boolean IS_RUNNING_ON_CLIENT = FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);
public static final boolean IS_CLOTH_CONFIG_PRESENT = FabricLoader.getInstance().isModLoaded("cloth-config2");
public static final Logger LOGGER = LoggerFactory.getLogger("ultimate_scaler");

    @Override
    public void onInitialize() {
        if (IS_RUNNING_ON_CLIENT) {
            KeyBindings.init();
        } else {
            try {
                UltimateScalerOptions.saveConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        LocatePosition.init();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConfigReloader());
    }
}