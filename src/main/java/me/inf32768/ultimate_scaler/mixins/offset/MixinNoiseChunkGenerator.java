package me.inf32768.ultimate_scaler.mixins.offset;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * {@link NoiseChunkGenerator} 类的 Mixin，用于偏移海平面和地底熔岩层并替换默认流体和地底熔岩。
 */
@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator {
    /**
     * 若 {@code extraYOffset} 选项开启，则为海平面（默认根据噪声设置而定）和地底熔岩层（原版中硬编码为 -54）施加偏移与缩放。
     * <p>
     * 并根据 {@code replaceDefaultFluid} 和 {@code replaceUndergroundLava} 选项替换默认流体和地底熔岩为指定的方块。
     */
    @Inject(method = "createFluidLevelSampler", at = @At("HEAD"), cancellable = true)
    private static void createFluidLevelSampler(ChunkGeneratorSettings settings, CallbackInfoReturnable<AquiferSampler.FluidLevelSampler> cir) {
        cir.setReturnValue((x, y, z) -> {
            // 获取配置的数值
            double scale = config.globalBigDecimalScale[1].doubleValue();
            double offset = config.globalBigDecimalOffset[1].doubleValue();
            // 计算偏移量
            int lavaLevelY = config.extraYOffset ? (int) ((-54D - offset) / scale) : -54;
            int seaLevelY = config.extraYOffset ? (int) ((settings.seaLevel() - offset) / scale) : settings.seaLevel();
            // 替换流体
            AquiferSampler.FluidLevel lavaLevel = new AquiferSampler.FluidLevel(lavaLevelY, config.replaceUndergroundLava ? Registries.BLOCK.get(Identifier.of(config.replaceUndergroundLavaBlock)).getDefaultState() : Blocks.LAVA.getDefaultState());
            AquiferSampler.FluidLevel waterLevel = new AquiferSampler.FluidLevel(seaLevelY, config.replaceDefaultFluid ? Registries.BLOCK.get(Identifier.of(config.replaceDefaultFluidBlock)).getDefaultState() : settings.defaultFluid());
            // 应用偏移
            return y < Math.min(lavaLevelY, seaLevelY) ? lavaLevel : waterLevel;
        });
    }
}
