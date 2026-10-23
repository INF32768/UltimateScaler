package xyz.inf32768.ultimatescaler.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import xyz.inf32768.ultimatescaler.ModMetadata;
import xyz.inf32768.ultimatescaler.client.config.labels.CommonLabels;
import xyz.inf32768.ultimatescaler.client.config.labels.ConfigAnnotation;
import xyz.inf32768.ultimatescaler.client.config.labels.DistanceEffectMeta;
import xyz.inf32768.ultimatescaler.config.Config;
import xyz.inf32768.ultimatescaler.config.ConfigManager;
import xyz.inf32768.ultimatescaler.versionutil.RegistryAccessor;
import xyz.inf32768.ultimatescaler.versionutil.TextEventFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 新版的 Cloth Config API 的集成类，提供了模块化的构造配置界面的方法，且利用标签系统 {@link ConfigAnnotation} 来让界面直观且井井有条。
 */
@SuppressWarnings("UnstableApiUsage")
public class NewClothConfigBuilder {
    /**
     * 十进制数（{@code BigDecimal}）的输入检测器，可通过 {@link StringListBuilder#setCellErrorSupplier} 调用，用于在相关配置条目内检测输入值是否能被解析为十进制数。
     */
    public static final Function<String, Optional<Component>> DECIMAL_CELL_ERROR_SUPPLIER = (s -> {
        try {
            new BigDecimal(s);
        } catch (NumberFormatException e) {
            return Optional.of(Component.translatable("ultimatescaler.config.worldgen.offset.invalidInput"));
        }
        return Optional.empty();
    });
    public static Config config;

    public static ConfigBuilder getMainConfigBuilder() {
        config = ConfigManager.getConfig();
        ConfigBuilder builder = ConfigBuilder.create();
        builder.setGlobalized(true);
        builder.setTitle(Component.translatable("ultimatescaler.config.new"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        builder.setAlwaysShowTabs(false);

        List<Runnable> savingRunnables = new ArrayList<>();

        buildHintsCategory(builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.new.hints")), entryBuilder);
        savingRunnables.add(buildGeneralCategory(builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.new.general")), entryBuilder));
        savingRunnables.add(buildWorldGenCategory(builder.getOrCreateCategory(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen"), true, CommonLabels.SERVER_ONLY)), entryBuilder));
        savingRunnables.add(buildFixesAndExpansionCategory(builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.new.fae")), entryBuilder));
        savingRunnables.add(buildUtilitiesCategory(builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.new.utilities")), entryBuilder));

        savingRunnables.add(() -> {
            try {
                ConfigManager.saveConfig();
            } catch (IOException e) {
                ModMetadata.LOGGER.error("Failed to save config file", e);
            }
        });
        builder.setSavingRunnable(() -> savingRunnables.forEach((runnable) -> {
            if (runnable != null) runnable.run();
        }));

        return builder;
    }

    private static Runnable buildUtilitiesCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        BooleanListEntry expandDatapackValueRangeEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.utilities.expand_datapack_value_range"), true, CommonLabels.SERVER_ONLY, CommonLabels.REQUIRES_RESTART), config.utilities.expandDatapackValueRange)
                .setDefaultValue(true)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.utilities.expand_datapack_value_range.tooltip"), false, CommonLabels.SERVER_ONLY, CommonLabels.REQUIRES_RESTART))
                .requireRestart()
                .build();

        category.addEntry(expandDatapackValueRangeEntry);

        return () -> {
            Config config = ConfigManager.getConfig();
            config.utilities.expandDatapackValueRange = expandDatapackValueRangeEntry.getValue();
        };
    }

    private static Runnable buildFixesAndExpansionCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder labelsLegend = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.new.labels_legend")).setExpanded(false);
        for (DistanceEffectMeta.Symbol symbol : DistanceEffectMeta.Symbol.values())
            labelsLegend.add(entryBuilder.startTextDescription(symbol.getRepresentation()).build());
        for (DistanceEffectMeta.Color color : DistanceEffectMeta.Color.values())
            labelsLegend.add(entryBuilder.startTextDescription(color.getRepresentation()).build());
        category.addEntry(labelsLegend.build());

        DistanceEffectMeta endRingsMeta = new DistanceEffectMeta(DistanceEffectMeta.Symbol.INTEGER_OVERFLOW, DistanceEffectMeta.Color.WORLD_GEN);
        BooleanListEntry endRingsEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.end_rings"), true, CommonLabels.SERVER_ONLY, endRingsMeta), config.fixesAndExpansion.endRings)
                .setDefaultValue(false)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.end_rings.tooltip"), false, CommonLabels.SERVER_ONLY, endRingsMeta))
                .build();

        DistanceEffectMeta worldBorderMeta = new DistanceEffectMeta(DistanceEffectMeta.Symbol.INTENDED_RESTRICTION, DistanceEffectMeta.Color.GAME_LOGIC);
        BooleanListEntry worldBorderEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.world_border"), true, CommonLabels.DUAL_ENV, CommonLabels.REQUIRES_RESTART, worldBorderMeta), config.fixesAndExpansion.worldBorder)
                .setDefaultValue(true)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.world_border.tooltip"), false, CommonLabels.DUAL_ENV, CommonLabels.REQUIRES_RESTART, worldBorderMeta))
                .requireRestart()
                .build();

        DistanceEffectMeta chunkGenerationOOBMeta = new DistanceEffectMeta(DistanceEffectMeta.Symbol.INTENDED_RESTRICTION, DistanceEffectMeta.Color.CRASH_OR_FREEZE);
        BooleanListEntry chunkGenerationOOBEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.chunk_generation_out_of_bound"), true, CommonLabels.SERVER_ONLY, chunkGenerationOOBMeta), config.fixesAndExpansion.chunkGenerationOutOfBound)
                .setDefaultValue(true)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.chunk_generation_out_of_bound.tooltip"), false, CommonLabels.SERVER_ONLY, chunkGenerationOOBMeta))
                .build();

        DistanceEffectMeta mineShaftMeta = new DistanceEffectMeta(DistanceEffectMeta.Symbol.INTEGER_OVERFLOW, DistanceEffectMeta.Color.CRASH_OR_FREEZE);
        BooleanListEntry mineShaftCannotGenerateEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.mineshaft_cannot_generate"), true, CommonLabels.SERVER_ONLY, mineShaftMeta), config.fixesAndExpansion.mineShaftCannotGenerate)
                .setDefaultValue(true)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.fae.mineshaft_cannot_generate.tooltip"), false, CommonLabels.SERVER_ONLY, mineShaftMeta))
                .build();

        category.addEntry(endRingsEntry);
        category.addEntry(worldBorderEntry);
        category.addEntry(chunkGenerationOOBEntry);
        category.addEntry(mineShaftCannotGenerateEntry);

        return () -> {
            Config config = NewClothConfigBuilder.config;
            config.fixesAndExpansion.endRings = endRingsEntry.getValue();
            config.fixesAndExpansion.worldBorder = worldBorderEntry.getValue();
            config.fixesAndExpansion.chunkGenerationOutOfBound = chunkGenerationOOBEntry.getValue();
            config.fixesAndExpansion.mineShaftCannotGenerate = mineShaftCannotGenerateEntry.getValue();
        };
    }

    private static Runnable buildWorldGenCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder reposition = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.new.worldgen.reposition")).setExpanded(true);

        StringListListEntry shiftEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.new.worldgen.reposition.shift"), Arrays.stream(config.worldGen.reposition.shift).map(BigDecimal::toString).toList())
                .setTooltip(Component.empty().append(Component.translatable("ultimatescaler.config.new.worldgen.reposition.shift.tooltip")).append("\n").append(Component.translatable("ultimatescaler.config.new.parsable_decimal.tooltip")))
                .setDefaultValue(Arrays.asList("0", "0", "0"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(NewClothConfigBuilder.DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        StringListListEntry scaleEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.new.worldgen.reposition.scale"), Arrays.stream(config.worldGen.reposition.scale).map(BigDecimal::toString).toList())
                .setTooltip(Component.empty().append(Component.translatable("ultimatescaler.config.new.worldgen.reposition.scale.tooltip")).append("\n").append(Component.translatable("ultimatescaler.config.new.parsable_decimal.tooltip")))
                .setDefaultValue(Arrays.asList("1", "1", "1"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(NewClothConfigBuilder.DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        BooleanListEntry highPrecisionModeEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.reposition.high_precision_mode"), true, CommonLabels.EXPERIMENTAL, CommonLabels.RADICAL, CommonLabels.LOW_PERFORMANCE), config.worldGen.reposition.highPrecisionMode)
                .setDefaultValue(false)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.reposition.high_precision_mode.tooltip"), false, CommonLabels.EXPERIMENTAL, CommonLabels.RADICAL, CommonLabels.LOW_PERFORMANCE))
                .build();
        BooleanListEntry extendedYAxisEffectEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.reposition.extra_y_axis_effect"), true, CommonLabels.EXPERIMENTAL, CommonLabels.FAULTABLE), config.worldGen.reposition.extendedYAxisEffect)
                .setDefaultValue(false)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.reposition.extra_y_axis_effect.tooltip"), false, CommonLabels.EXPERIMENTAL, CommonLabels.FAULTABLE))
                .build();

        reposition.add(shiftEntry);
        reposition.add(scaleEntry);
        reposition.add(highPrecisionModeEntry);
        reposition.add(extendedYAxisEffectEntry);

        SubCategoryBuilder fluidReplace = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.new.worldgen.fluid_replace")).setExpanded(true);

        BooleanListEntry defaultFluidEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.new.worldgen.fluid_replace.default_fluid"), !config.worldGen.fluidReplace.defaultFluid.isEmpty())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.new.worldgen.fluid_replace.default_fluid.tooltip"))
                .build();
        DropdownBoxEntry<Block> defaultFluidBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(config.worldGen.fluidReplace.defaultFluid.isEmpty() ? Blocks.AIR : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.defaultFluid))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(BuiltInRegistries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
                .setDisplayRequirement(Requirement.all(defaultFluidEntry::getValue))
                .build();
        BooleanListEntry undergroundLavaEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.fluid_replace.underground_lava"), true, CommonLabels.EXPERIMENTAL, CommonLabels.FAULTABLE), !config.worldGen.fluidReplace.undergroundLava.isEmpty())
                .setDefaultValue(false)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.fluid_replace.underground_lava.tooltip"), false, CommonLabels.EXPERIMENTAL, CommonLabels.FAULTABLE))
                .build();
        DropdownBoxEntry<Block> undergroundLavaBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(config.worldGen.fluidReplace.undergroundLava.isEmpty() ? Blocks.AIR : RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(config.worldGen.fluidReplace.undergroundLava))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(BuiltInRegistries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
                .setDisplayRequirement(Requirement.all(undergroundLavaEntry::getValue))
                .build();

        fluidReplace.add(defaultFluidEntry);
        fluidReplace.add(defaultFluidBlockEntry);
        fluidReplace.add(undergroundLavaEntry);
        fluidReplace.add(undergroundLavaBlockEntry);

        SubCategoryBuilder maintainPrecisionControl = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control")).setExpanded(true);

        try {
            // farLandsPos 项在配置文件中以字符串形式存储，可能无法对应到枚举中的项，因此需要处理空指针异常防止无法构建
            entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos"), Config.FarLandsPos.class, config.worldGen.farLandsControl.farLandsPos);
        } catch (NullPointerException e) {
            config.worldGen.farLandsControl.farLandsPos = Config.FarLandsPos.DEFAULT;
        }
        EnumListEntry<Config.FarLandsPos> farLandsPosEntry = entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos"), Config.FarLandsPos.class, config.worldGen.farLandsControl.farLandsPos)
                .setDefaultValue(Config.FarLandsPos.DEFAULT)
                .setEnumNameProvider((farLandsPos) -> Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos." + farLandsPos.name()))
                .setTooltipSupplier((farLandsPos) -> Optional.of(new Component[]{Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos.tooltip.1"), Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos.tooltip.2").append(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.pos." + farLandsPos.name() + ".tooltip"))}))
                .build();
        DoubleListEntry customDivisorEntry = entryBuilder.startDoubleField(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.custom_divisor"), config.worldGen.farLandsControl.customDivisor)
                .setDefaultValue(33554432)
                .setMin(0.0)
                .setTooltip(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.custom_divisor.tooltip"))
                .setDisplayRequirement(Requirement.all(() -> farLandsPosEntry.getValue().equals(Config.FarLandsPos.DEFAULT)))
                .build();
        BooleanListEntry limitReturnValueEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.limit_return_value"), true, CommonLabels.FAULTABLE), config.worldGen.farLandsControl.limitReturnValue)
                .setDefaultValue(false)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.limit_return_value.tooltip"), false, CommonLabels.FAULTABLE))
                .build();
        IntegerListEntry maxNoiseValueEntry = entryBuilder.startIntField(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.max_logarithm_value"), config.worldGen.farLandsControl.maxLogarithmValue)
                .setDefaultValue(7)
                .setTooltip(Component.translatable("ultimatescaler.config.new.worldgen.far_lands_control.max_logarithm_value.tooltip"))
                .setDisplayRequirement(Requirement.all(limitReturnValueEntry::getValue))
                .build();

        maintainPrecisionControl.add(farLandsPosEntry);
        maintainPrecisionControl.add(customDivisorEntry);
        maintainPrecisionControl.add(limitReturnValueEntry);
        maintainPrecisionControl.add(maxNoiseValueEntry);

        category.addEntry(reposition.build());
        category.addEntry(fluidReplace.build());
        category.addEntry(maintainPrecisionControl.build());

        return () -> {
            Config config = NewClothConfigBuilder.config;
            config.worldGen.reposition.shift = shiftEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.worldGen.reposition.scale = scaleEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.worldGen.reposition.highPrecisionMode = highPrecisionModeEntry.getValue();
            config.worldGen.reposition.extendedYAxisEffect = extendedYAxisEffectEntry.getValue();
            config.worldGen.fluidReplace.defaultFluid = defaultFluidEntry.getValue() ? BuiltInRegistries.BLOCK.getKey(defaultFluidBlockEntry.getValue()).toString() : "";
            config.worldGen.fluidReplace.undergroundLava = undergroundLavaEntry.getValue() ? BuiltInRegistries.BLOCK.getKey(undergroundLavaBlockEntry.getValue()).toString() : "";
            config.worldGen.farLandsControl.farLandsPos = farLandsPosEntry.getValue();
            config.worldGen.farLandsControl.customDivisor = customDivisorEntry.getValue();
            config.worldGen.farLandsControl.limitReturnValue = limitReturnValueEntry.getValue();
            config.worldGen.farLandsControl.maxLogarithmValue = maxNoiseValueEntry.getValue();
        };
    }

    private static Runnable buildGeneralCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        KeyCodeEntry optionMenuEntry = entryBuilder.startModifierKeyCodeField(
                        ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.general.option_menu_key"), true, CommonLabels.CLIENT_ONLY),
                        ModifierKeyCode.of(
                                InputConstants.Type.KEYSYM.getOrCreate(
                                        config.general.configScreenKeybind >> 3),
                                Modifier.of((short) (config.general.configScreenKeybind & 0b111))
                        )
                )
                .setDefaultValue(ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U), Modifier.of(false, true, false)))
                .setTooltip(ConfigAnnotation.addAnnotations(Component.empty(), false, CommonLabels.CLIENT_ONLY))
                .build();
        BooleanListEntry showTerrainPosEntry = entryBuilder.startBooleanToggle(ConfigAnnotation.addAnnotations(Component.translatable("ultimatescaler.config.new.general.show_terrain_pos"), true, CommonLabels.CLIENT_ONLY), config.general.showTerrainPos)
                .setDefaultValue(true)
                .setTooltip(ConfigAnnotation.addAnnotations(Component.empty(), false, CommonLabels.CLIENT_ONLY))
                .build();

        category.addEntry(optionMenuEntry);
        category.addEntry(showTerrainPosEntry);

        return () -> {
            Config config = NewClothConfigBuilder.config;
            config.general.configScreenKeybind = (optionMenuEntry.getValue().getKeyCode().getValue() << 3) | optionMenuEntry.getValue().getModifier().getValue();
            config.general.showTerrainPos = showTerrainPosEntry.getValue();
        };
    }

    private static void buildHintsCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder) {
        TextListEntry welcome = entryBuilder.startTextDescription(
                Component.translatable("ultimatescaler.config.new.header.welcome.1")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("ultimatescaler.config.new.header.welcome.2")
                                .withStyle(s -> s
                                        .withUnderlined(true)
                                        .withClickEvent(TextEventFactory.createOpenUrlEvent("https://github.com/INF32768/UltimateScaler/issues"))
                                )
                        ).append(Component.translatable("ultimatescaler.config.new.header.welcome.3"))
        ).build();
        TextListEntry wikiIntro = entryBuilder.startTextDescription(
                Component.translatable("ultimatescaler.config.new.header.wiki_intro.1")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("ultimatescaler.config.new.header.wiki_intro.2")
                                .withStyle(s -> s
                                        .withUnderlined(true)
                                        .withClickEvent(TextEventFactory.createOpenUrlEvent("https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.en"))
                                )
                        ).append(Component.translatable("ultimatescaler.config.new.header.wiki_intro.3"))
        ).build();
        TextListEntry oldScreen = entryBuilder.startTextDescription(
                Component.translatable("ultimatescaler.config.new.header.old_screen").withStyle(ChatFormatting.GRAY)
        ).build();

        SubCategoryBuilder labelsLegend = entryBuilder.startSubCategory(Component.translatable("ultimatescaler.config.new.labels_legend"));
        for (CommonLabels label : CommonLabels.values())
            labelsLegend.add(entryBuilder.startTextDescription(label.getRepresentation()).build());

        category.addEntry(welcome);
        category.addEntry(wikiIntro);
        category.addEntry(oldScreen);
        category.addEntry(labelsLegend.build());
    }
}
