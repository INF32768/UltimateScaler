package xyz.inf32768.ultimatescaler.mixin.offset;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.inf32768.ultimatescaler.config.ConfigManager;
import xyz.inf32768.ultimatescaler.versionutil.RegistryAccessor;

/**
 * {@link NoiseBasedChunkGenerator} 类的 Mixin，用于偏移海平面和地底熔岩层并替换默认流体和地底熔岩。
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator {
    /**
     * 若 {@code extraYOffset} 选项开启，则为海平面（默认根据噪声设置而定）和地底熔岩层（原版中硬编码为 -54）施加偏移与缩放。
     * <p>
     * 并根据 {@code replaceDefaultFluid} 和 {@code replaceUndergroundLava} 选项替换默认流体和地底熔岩为指定的方块。
     */
    @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true)
    private static void createFluidLevelSampler(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        cir.setReturnValue((x, y, z) -> {
            // 获取配置的数值
            double scale = ConfigManager.getConfig().worldGen.reposition.scale[1].doubleValue();
            double offset = ConfigManager.getConfig().worldGen.reposition.shift[1].doubleValue();
            // 计算偏移量
            int lavaLevelY = ConfigManager.getConfig().worldGen.reposition.extendedYAxisEffect ? (int) ((-54D - offset) / scale) : -54;
            int seaLevelY = ConfigManager.getConfig().worldGen.reposition.extendedYAxisEffect ? (int) ((settings.seaLevel() - offset) / scale) : settings.seaLevel();
            // 替换流体
            // 注：由于 1.21.2 前，这里 get 方法的 intermediary 映射名有变化，因此需要使用兼容层。
            Aquifer.FluidStatus lavaLevel = new Aquifer.FluidStatus(lavaLevelY, ConfigManager.getConfig().worldGen.fluidReplace.undergroundLava.isEmpty() ? Blocks.LAVA.defaultBlockState() : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(ConfigManager.getConfig().worldGen.fluidReplace.undergroundLava)).defaultBlockState());
            Aquifer.FluidStatus waterLevel = new Aquifer.FluidStatus(seaLevelY, ConfigManager.getConfig().worldGen.fluidReplace.defaultFluid.isEmpty() ? settings.defaultFluid() : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(ConfigManager.getConfig().worldGen.fluidReplace.defaultFluid)).defaultBlockState());
            // 应用偏移
            return y < Math.min(lavaLevelY, seaLevelY) ? lavaLevel : waterLevel;
        });
    }
}
