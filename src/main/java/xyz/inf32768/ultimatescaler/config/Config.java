package xyz.inf32768.ultimatescaler.config;

import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;

/**
 * 所有配置选项的定义类，定义了所有配置选项的名称、类型和默认值。使用层次化的类结构组织，可以使生成的配置文件井井有条。
 * <p>
 * 有关配置选项的详细说明，请参考 Wiki 中的页面<a href="https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.zh">《配置全解》</a>。
 *
 * @see ConfigManager
 */
public final class Config {
    public int CONFIG_VERSION = 4;
    public General general = new General();
    public WorldGen worldGen = new WorldGen();
    public FixesAndExpansion fixesAndExpansion = new FixesAndExpansion();
    public Utilities utilities = new Utilities();

    /**
     * 定义“边境之地位置”选项的枚举类。
     */
    public enum FarLandsPos {
        BETA,
        RELEASE,
        DEFAULT,
        REMOVED,
    }

    /**
     * 旧版的配置选项定义类。目前仅用于旧版配置文件的升级。
     *
     * @deprecated 杂乱无章、难以持续。请使用新版 {@link Config} 代替。
     */
    @Deprecated(since = "0.4.0")
    public static class ConfigImpl {
        public int CONFIG_VERSION = 3;
        public BigDecimal[] globalBigDecimalOffset = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        public BigDecimal[] globalBigDecimalScale = {BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE};
        public int optionMenuKeyCode = GLFW.GLFW_KEY_U;
        public short optionMenuModifierValue = 2;
        public boolean showTerrainPos = true;
        public LegacyFarLandsPos farLandsPos = LegacyFarLandsPos.DEFAULT;
        public double maintainPrecisionCustomDivisor = 33554432;
        public boolean limitReturnValue = false;
        public int maxNoiseLogarithmValue = 7;
        public boolean extraYOffset = false;
        public boolean bigIntegerRewrite = false;
        public boolean fixEndRings = false;
        public boolean fixChunkGenerationOutOfBound = true;
        public boolean expandDatapackValueRange = true;
        public boolean expandWorldBorder = true;
        public boolean fixMineshaftCannotGenerate = true;
        public boolean replaceDefaultFluid = false;
        public String replaceDefaultFluidBlock = "minecraft:air";
        public boolean replaceUndergroundLava = false;
        public String replaceUndergroundLavaBlock = "minecraft:air";
        public boolean publicTerrainPos = true;

        /**
         * 旧版的“边境之地位置”枚举。保留了新版已移除的 {@code CUSTOM}，以便正确读取 V3 及更早的配置文件。
         *
         * @deprecated 仅供旧版配置迁移使用。请使用 {@link Config.FarLandsPos} 代替。
         */
        @Deprecated(since = "0.4.0")
        public enum LegacyFarLandsPos {
            BETA,
            RELEASE,
            DEFAULT,
            REMOVED,
            CUSTOM,
        }
    }

    public static class General {
        public int configScreenKeybind = (GLFW.GLFW_KEY_U << 3) | 2;
        public boolean showTerrainPos = true;
        public boolean publicTerrainPos = true;
    }

    public static class WorldGen {
        public Reposition reposition = new Reposition();
        public FluidReplace fluidReplace = new FluidReplace();
        public FarLandsControl farLandsControl = new FarLandsControl();

        public static class Reposition {
            // 这两个数组在 TOML 中以带引号的字符串保存，由 ConfigManager 负责转换，
            // 避免 toml4j 把裸数字解析成 Long/Double 时丢失精度。
            public BigDecimal[] shift = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
            public BigDecimal[] scale = {BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE};
            public boolean extendedYAxisEffect = false;
            public boolean highPrecisionMode = false;
        }

        public static class FluidReplace {
            public String defaultFluid = "";
            public String undergroundLava = "";
        }

        public static class FarLandsControl {
            public FarLandsPos farLandsPos = FarLandsPos.DEFAULT;
            public double customDivisor = 33554432;
            public boolean limitReturnValue = false;
            public int maxLogarithmValue = 7;
        }
    }

    public static class FixesAndExpansion {
        public boolean endRings = false;
        public boolean worldBorder = true;
        public boolean chunkGenerationOutOfBound = true;
        public boolean mineShaftCannotGenerate = true;
    }

    public static class Utilities {
        public boolean expandDatapackValueRange = true;
    }
}