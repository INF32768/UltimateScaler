package xyz.inf32768.ultimatescaler.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import xyz.inf32768.ultimatescaler.ModMetadata;
import xyz.inf32768.ultimatescaler.config.Config;
import xyz.inf32768.ultimatescaler.config.ConfigManager;
import xyz.inf32768.ultimatescaler.versionutil.RegistryAccessor;
import xyz.inf32768.ultimatescaler.versionutil.TextEventFactory;
import xyz.inf32768.ultimatescaler.versionutil.VersionUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cloth Config API 的集成类，提供了构造配置界面的方法。
 *
 * @deprecated 杂乱无章、难以持续，且部分选项描述不清、极具误导性。请使用新版 {@link NewClothConfigBuilder} 代替。考虑到某些老用户可能不熟悉新版界面故保留作为过渡，未来将会被移除。
 */
@Environment(EnvType.CLIENT)
@Deprecated(since = "0.4.0", forRemoval = true)
public class ClothConfigBuilder {
    /**
     * 定义配置界面，包括其中的配置项和常见问题的部分，以及保存的逻辑。
     *
     * @return 配置界面构建器，共 Cloth Config API 使用。
     * @see ConfigManager#getConfig()
     */
    @SuppressWarnings("UnstableApiUsage")
    public static ConfigBuilder getConfigBuilder() {
        // 配置项
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Component.translatable("ultimatescaler.config"));
        builder.setGlobalized(true);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory hints = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.deprecated").withStyle(ChatFormatting.DARK_RED));
        TextListEntry deprecated = entryBuilder.startTextDescription(
                Component.translatable("ultimatescaler.config.deprecated.tooltip").withStyle(ChatFormatting.RED)
        ).build();

        hints.addEntry(deprecated);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.general"));
        Config config = ConfigManager.getConfig();
        KeyCodeEntry optionMenuEntry = entryBuilder.startModifierKeyCodeField(Component.translatable("ultimatescaler.config.general.optionMenuKey"), ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(config.general.configScreenKeybind >> 3), Modifier.of((short) (config.general.configScreenKeybind & 0b111))))
                .setDefaultValue(ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U), Modifier.of(false, true, false)))
                .build();
        BooleanListEntry showTerrainPosEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.general.showTerrainPos"), config.general.showTerrainPos)
                .setDefaultValue(true)
                .build();

        general.addEntry(optionMenuEntry);
        general.addEntry(showTerrainPosEntry);

        ConfigCategory worldGen = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.worldgen"));

        TextListEntry worldGenHeader = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.worldgen.header").withStyle(s -> s.withBold(true).withColor(ChatFormatting.YELLOW))).build();
        StringListListEntry globalOffsetEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.worldgen.offset.globalOffset"), Arrays.stream(config.worldGen.reposition.shift).map(BigDecimal::toString).toList())
                .setTooltip(Component.translatable("ultimatescaler.config.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("0", "0", "0"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(NewClothConfigBuilder.DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        StringListListEntry globalScaleEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.worldgen.offset.globalScale"), Arrays.stream(config.worldGen.reposition.scale).map(BigDecimal::toString).toList())
                .setTooltip(Component.translatable("ultimatescaler.config.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("1", "1", "1"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(NewClothConfigBuilder.DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        try {
            // farLandsPos 项在配置文件中以字符串形式存储，可能无法对应到枚举中的项，因此需要处理空指针异常防止无法构建
            entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.worldgen.farLandsPos"), Config.FarLandsPos.class, config.worldGen.farLandsControl.farLandsPos);
        } catch (NullPointerException e) {
            config.worldGen.farLandsControl.farLandsPos = Config.FarLandsPos.DEFAULT;
        }
        EnumListEntry<FarLandsPos> farLandsPosEntry = entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.worldgen.farLandsPos"), FarLandsPos.class, fromNewFarLandsPos(config.worldGen.farLandsControl.farLandsPos, config.worldGen.farLandsControl.customDivisor != 33554432D))
                .setDefaultValue(FarLandsPos.DEFAULT)
                .setEnumNameProvider((farLandsPos) -> Component.translatable("ultimatescaler.config.worldgen.FarLandsPos." + farLandsPos.name()))
                .setTooltipSupplier((FarLandsPos) -> Optional.of(new Component[]{Component.translatable("ultimatescaler.config.worldgen.FarLandsPos." + FarLandsPos.name() + ".tooltip")}))
                .build();
        DoubleListEntry maintainPrecisionCustomDivisorEntry = entryBuilder.startDoubleField(Component.translatable("ultimatescaler.config.worldgen.maintainPrecisionCustomDivisor"), config.worldGen.farLandsControl.customDivisor)
                .setDefaultValue(33554432)
                .setMin(0.0)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.maintainPrecisionCustomDivisor.tooltip"))
                .setDisplayRequirement(Requirement.all(() -> farLandsPosEntry.getValue().equals(FarLandsPos.CUSTOM)))
                .build();
        BooleanListEntry limitReturnValueEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.limitReturnValue"), config.worldGen.farLandsControl.limitReturnValue)
                .setDefaultValue(false)
                .build();
        IntegerListEntry maxNoiseValueEntry = entryBuilder.startIntField(Component.translatable("ultimatescaler.config.worldgen.maxNoiseLogarithmValue"), config.worldGen.farLandsControl.maxLogarithmValue)
                .setDefaultValue(7)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.maxNoiseLogarithmValue.tooltip"))
                .setDisplayRequirement(Requirement.all(limitReturnValueEntry::getValue))
                .build();
        BooleanListEntry replaceDefaultFluidEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.replaceDefaultFluid"), !config.worldGen.fluidReplace.defaultFluid.isEmpty())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.replaceDefaultFluid.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceDefaultFluidBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(config.worldGen.fluidReplace.defaultFluid.isEmpty() ? Blocks.AIR : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.defaultFluid))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(BuiltInRegistries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
                .setDisplayRequirement(Requirement.all(replaceDefaultFluidEntry::getValue))
                .build();

        worldGen.addEntry(worldGenHeader);
        worldGen.addEntry(farLandsPosEntry);
        worldGen.addEntry(maintainPrecisionCustomDivisorEntry);
        worldGen.addEntry(limitReturnValueEntry);
        worldGen.addEntry(maxNoiseValueEntry);
        worldGen.addEntry(globalOffsetEntry);
        worldGen.addEntry(globalScaleEntry);
        worldGen.addEntry(replaceDefaultFluidEntry);
        worldGen.addEntry(replaceDefaultFluidBlockEntry);

        SubCategoryBuilder experimental = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.worldgen.experimental"));
        TextListEntry experimentalHeader = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.worldgen.experimental.header").withStyle(s -> s.withBold(true).withColor(ChatFormatting.RED))).build();
        BooleanListEntry bigIntegerRewriteEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite"), config.worldGen.reposition.highPrecisionMode)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.1")
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.2").withStyle(s -> s.withColor(ChatFormatting.YELLOW)))
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.3"))
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.4").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
                )
                .build();
        BooleanListEntry extraYOffsetEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.extraYOffset"), config.worldGen.reposition.extendedYAxisEffect)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.extraYOffset.tooltip"))
                .build();
        BooleanListEntry replaceUndergroundLavaEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.replaceUndergroundLava"), !config.worldGen.fluidReplace.undergroundLava.isEmpty())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.replaceUndergroundLava.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceUndergroundLavaBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(config.worldGen.fluidReplace.undergroundLava.isEmpty() ? Blocks.AIR : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.undergroundLava))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(BuiltInRegistries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
                .setDisplayRequirement(Requirement.all(replaceUndergroundLavaEntry::getValue))
                .build();

        experimental.add(experimentalHeader);
        experimental.add(bigIntegerRewriteEntry);
        experimental.add(extraYOffsetEntry);
        experimental.add(replaceUndergroundLavaEntry);
        experimental.add(replaceUndergroundLavaBlockEntry);
        worldGen.addEntry(experimental.build());

        ConfigCategory tweaks = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.tweaks"));
        BooleanListEntry fixEndRingsEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.fixEndRings"), config.fixesAndExpansion.endRings)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.fixEndRings.tooltip"))
                .build();
        BooleanListEntry fixChunkGenerationOutOfBoundEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.fixChunkGenerationOutOfBound"), config.fixesAndExpansion.chunkGenerationOutOfBound)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.fixChunkGenerationOutOfBound.tooltip"))
                .build();
        BooleanListEntry expandDatapackValueRangeEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.expandDatapackValueRange"), config.utilities.expandDatapackValueRange)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.expandDatapackValueRange.tooltip").append(Component.translatable("ultimatescaler.config.require_restart").withStyle(s -> s.withColor(ChatFormatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry expandWorldBorderEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.expandWorldBorder"), config.fixesAndExpansion.worldBorder)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.expandWorldBorder.tooltip").append(Component.translatable("ultimatescaler.config.require_restart").withStyle(s -> s.withColor(ChatFormatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry fixMineshaftCannotGenerateEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.fixMineshaftCannotGenerate"), config.fixesAndExpansion.mineShaftCannotGenerate)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.fixMineshaftCannotGenerate.tooltip"))
                .build();
        tweaks.addEntry(fixEndRingsEntry);
        if (VersionUtil.isVersionAtLeast(VersionUtil.parse("1.21.2"))) {
            tweaks.addEntry(fixChunkGenerationOutOfBoundEntry);
        }
        tweaks.addEntry(expandDatapackValueRangeEntry);
        tweaks.addEntry(expandWorldBorderEntry);
        tweaks.addEntry(fixMineshaftCannotGenerateEntry);

        ConfigCategory faq = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.faq"));

        TextListEntry migrated = entryBuilder.startTextDescription(
                Component.translatable("ultimatescaler.config.faq.migrated.1")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("ultimatescaler.config.faq.migrated.2")
                                .withStyle(s -> s
                                        .withUnderlined(true)
                                        .withClickEvent(TextEventFactory.createOpenUrlEvent("https://github.com/INF32768/UltimateScaler/wiki/UserGuide.FAQ.en"))
                                )
                        ).append(Component.translatable("ultimatescaler.config.faq.migrated.3"))
        ).build();

        faq.addEntry(migrated);

        // 保存逻辑。将输入值保存到 Config.config 实例中，再尝试保存到文件中
        builder.setSavingRunnable(() -> {
            config.worldGen.reposition.shift = globalOffsetEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.worldGen.reposition.scale = globalScaleEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.general.configScreenKeybind = (optionMenuEntry.getValue().getKeyCode().getValue() << 3) | optionMenuEntry.getValue().getModifier().getValue();
            config.general.showTerrainPos = showTerrainPosEntry.getValue();
            config.worldGen.farLandsControl.farLandsPos = toNewFarLandsPos(farLandsPosEntry.getValue());
            config.worldGen.farLandsControl.customDivisor = maintainPrecisionCustomDivisorEntry.getValue();
            config.worldGen.farLandsControl.limitReturnValue = limitReturnValueEntry.getValue();
            config.worldGen.farLandsControl.maxLogarithmValue = maxNoiseValueEntry.getValue();
            config.worldGen.fluidReplace.defaultFluid = replaceDefaultFluidEntry.getValue() ? BuiltInRegistries.BLOCK.getKey(replaceDefaultFluidBlockEntry.getValue()).toString() : "";
            config.worldGen.fluidReplace.undergroundLava = replaceUndergroundLavaEntry.getValue() ? BuiltInRegistries.BLOCK.getKey(replaceUndergroundLavaBlockEntry.getValue()).toString() : "";
            config.worldGen.reposition.extendedYAxisEffect = extraYOffsetEntry.getValue();
            config.worldGen.reposition.highPrecisionMode = bigIntegerRewriteEntry.getValue();
            config.fixesAndExpansion.endRings = fixEndRingsEntry.getValue();
            config.fixesAndExpansion.chunkGenerationOutOfBound = fixChunkGenerationOutOfBoundEntry.getValue();
            config.utilities.expandDatapackValueRange = expandDatapackValueRangeEntry.getValue();
            config.fixesAndExpansion.worldBorder = expandWorldBorderEntry.getValue();
            config.fixesAndExpansion.mineShaftCannotGenerate = fixMineshaftCannotGenerateEntry.getValue();

            try {
                ConfigManager.saveConfig();
            } catch (IOException e) {
                ModMetadata.LOGGER.error("Failed to save config file", e);
            }
        });
        return builder;
    }

    private static Config.FarLandsPos toNewFarLandsPos(FarLandsPos value) {
        return switch (value) {
            case BETA -> Config.FarLandsPos.BETA;
            case CUSTOM, DEFAULT -> Config.FarLandsPos.DEFAULT;
            case RELEASE -> Config.FarLandsPos.RELEASE;
            case REMOVED -> Config.FarLandsPos.REMOVED;
        };
    }

    private static FarLandsPos fromNewFarLandsPos(Config.FarLandsPos farLandsPos, boolean useCustomDivisor) {
        return switch (farLandsPos) {
            case BETA -> FarLandsPos.BETA;
            case RELEASE -> FarLandsPos.RELEASE;
            case DEFAULT -> useCustomDivisor ? FarLandsPos.CUSTOM : FarLandsPos.DEFAULT;
            case REMOVED -> FarLandsPos.REMOVED;
        };
    }

    /**
     * 旧版的边境之地位置选项枚举类。由于新版移除了带有误导性的 {@code CUSTOM} 选项，因此需要单独适配旧版。
     *
     * @see Config.FarLandsPos
     */
    private enum FarLandsPos {
        BETA,
        RELEASE,
        REMOVED,
        DEFAULT,
        CUSTOM
    }
}
