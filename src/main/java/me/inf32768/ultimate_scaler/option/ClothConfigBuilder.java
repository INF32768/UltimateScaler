package me.inf32768.ultimate_scaler.option;

import me.inf32768.ultimate_scaler.util.RegistryAccessor;
import me.inf32768.ultimate_scaler.util.TextEventFactory;
import me.inf32768.ultimate_scaler.util.VersionHelper;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.inf32768.ultimate_scaler.option.UltimateScalerOptions.config;

/**
 * Cloth Config API 的集成类，提供了构造配置界面的方法。
 */
@Environment(EnvType.CLIENT)
public class ClothConfigBuilder {

    /**
     * 十进制数（{@code BigDecimal}）的输入检测器，可通过 {@link StringListBuilder#setCellErrorSupplier} 调用，用于在相关配置条目内检测输入值是否能被解析为十进制数。
     */
    public static final Function<String, Optional<Text>> DECIMAL_CELL_ERROR_SUPPLIER = (s -> {
        try {
            new BigDecimal(s);
        } catch (NumberFormatException e) {
            return Optional.of(Text.translatable("ultimate_scaler.options.worldgen.offset.invalidInput"));
        }
        return Optional.empty();
    });

    /**
     * 定义配置界面，包括其中的配置项和常见问题的部分，以及保存的逻辑。
     * @return 配置界面构建器，共 Cloth Config API 使用。
     * @see UltimateScalerOptions.ConfigImpl
     */
    @SuppressWarnings("UnstableApiUsage")
    public static ConfigBuilder getConfigBuilder() {
        // 配置项
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("ultimate_scaler.options"));
        builder.setGlobalized(true);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("ultimate_scaler.options.general"));
        KeyCodeEntry optionMenuEntry = entryBuilder.startModifierKeyCodeField(Text.translatable("ultimate_scaler.options.general.optionMenuKey"), ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(config.optionMenuKeyCode), Modifier.of(config.optionMenuModifierValue)))
                .setDefaultValue(ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_U), Modifier.of(false, true, false)))
                .build();
        BooleanListEntry showTerrainPosEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.general.showTerrainPos"), config.showTerrainPos)
                .setDefaultValue(true)
                .build();

        general.addEntry(optionMenuEntry);
        general.addEntry(showTerrainPosEntry);

        ConfigCategory worldGen = builder.getOrCreateCategory(Text.translatable("ultimate_scaler.options.worldgen"));

        TextListEntry worldGenHeader = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.worldgen.header").styled(s -> s.withBold(true).withColor(Formatting.YELLOW))).build();
        StringListListEntry globalOffsetEntry = entryBuilder.startStrList(Text.translatable("ultimate_scaler.options.worldgen.offset.globalOffset"), Arrays.stream(config.globalBigDecimalOffset).map(BigDecimal::toString).toList())
                .setTooltip(Text.translatable("ultimate_scaler.options.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("0", "0", "0"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        StringListListEntry globalScaleEntry = entryBuilder.startStrList(Text.translatable("ultimate_scaler.options.worldgen.offset.globalScale"), Arrays.stream(config.globalBigDecimalScale).map(BigDecimal::toString).toList())
                .setTooltip(Text.translatable("ultimate_scaler.options.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("1", "1", "1"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        try {
            // farLandsPos 项在配置文件中以字符串形式存储，可能无法对应到枚举中的项，因此需要处理空指针异常防止无法构建
            entryBuilder.startEnumSelector(Text.translatable("ultimate_scaler.options.worldgen.farLandsPos"), UltimateScalerOptions.FarLandsPos.class, config.farLandsPos);
        } catch (NullPointerException e) {
            SystemToast.show(MinecraftClient.getInstance().getToastManager(), new SystemToast.Type(), Text.translatable("ultimate_scaler.options.worldgen.offset.invalidInput"), Text.of("For enum: " + ConfigManager.readString(UltimateScalerOptions.CONFIG_PATH, "farLandsPos")));
            config.farLandsPos = UltimateScalerOptions.FarLandsPos.DEFAULT;
        }
        EnumListEntry<UltimateScalerOptions.FarLandsPos> farLandsPosEntry = entryBuilder.startEnumSelector(Text.translatable("ultimate_scaler.options.worldgen.farLandsPos"), UltimateScalerOptions.FarLandsPos.class, config.farLandsPos)
                .setDefaultValue(UltimateScalerOptions.FarLandsPos.DEFAULT)
                .setEnumNameProvider((farLandsPos) -> Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos." + farLandsPos.name()))
                .setTooltipSupplier((FarLandsPos) -> Optional.of(new Text[]{Text.translatable("ultimate_scaler.options.worldgen.FarLandsPos." + FarLandsPos.name() + ".tooltip")}))
                .build();
        DoubleListEntry maintainPrecisionCustomDivisorEntry = entryBuilder.startDoubleField(Text.translatable("ultimate_scaler.options.worldgen.maintainPrecisionCustomDivisor"), config.maintainPrecisionCustomDivisor)
                .setDefaultValue(33554432)
                .setMin(0.0)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.maintainPrecisionCustomDivisor.tooltip"))
                .setDisplayRequirement(Requirement.all(() -> farLandsPosEntry.getValue().equals(UltimateScalerOptions.FarLandsPos.CUSTOM)))
                .build();
        BooleanListEntry limitReturnValueEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.limitReturnValue"), config.limitReturnValue)
                .setDefaultValue(false)
                .build();
        IntegerListEntry maxNoiseValueEntry = entryBuilder.startIntField(Text.translatable("ultimate_scaler.options.worldgen.maxNoiseLogarithmValue"), config.maxNoiseLogarithmValue)
                .setDefaultValue(7)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.maxNoiseLogarithmValue.tooltip"))
                .setDisplayRequirement(Requirement.all(limitReturnValueEntry::getValue))
                .build();
        BooleanListEntry replaceDefaultFluidEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.replaceDefaultFluid"), config.replaceDefaultFluid)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.replaceDefaultFluid.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceDefaultFluidBlockEntry = entryBuilder.startDropdownMenu(Text.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceDefaultFluidBlock))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(Registries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
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

        SubCategoryBuilder experimental = entryBuilder.startSubCategory(Text.translatable("ultimate_scaler.options.worldgen.experimental"));
        TextListEntry experimentalHeader = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.worldgen.experimental.header").styled(s -> s.withBold(true).withColor(Formatting.RED))).build();
        BooleanListEntry bigIntegerRewriteEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite"), config.bigIntegerRewrite)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.1")
                        .append(Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.2").styled(s -> s.withColor(Formatting.YELLOW)))
                        .append(Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.3"))
                        .append(Text.translatable("ultimate_scaler.options.worldgen.bigIntegerRewrite.tooltip.4").styled(s -> s.withColor(Formatting.GREEN)))
                )
                .build();
        BooleanListEntry extraYOffsetEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.extraYOffset"), config.extraYOffset)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.extraYOffset.tooltip"))
                .build();
        BooleanListEntry replaceUndergroundLavaEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.replaceUndergroundLava"), config.replaceUndergroundLava)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.replaceUndergroundLava.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceUndergroundLavaBlockEntry = entryBuilder.startDropdownMenu(Text.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(RegistryAccessor.get(Registries.BLOCK, Identifier.of(config.replaceUndergroundLavaBlock))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
                .setDefaultValue(Blocks.AIR)
                .setSelections(Registries.BLOCK.stream().sorted(Comparator.comparing(Block::toString)).collect(Collectors.toCollection(LinkedHashSet::new)))
                .setDisplayRequirement(Requirement.all(replaceUndergroundLavaEntry::getValue))
                .build();

        experimental.add(experimentalHeader);
        experimental.add(bigIntegerRewriteEntry);
        experimental.add(extraYOffsetEntry);
        experimental.add(replaceUndergroundLavaEntry);
        experimental.add(replaceUndergroundLavaBlockEntry);
        worldGen.addEntry(experimental.build());

        ConfigCategory tweaks = builder.getOrCreateCategory(Text.translatable("ultimate_scaler.options.tweaks"));
        BooleanListEntry fixEndRingsEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.worldgen.fixEndRings"), config.fixEndRings)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("ultimate_scaler.options.worldgen.fixEndRings.tooltip"))
                .build();
        BooleanListEntry fixChunkGenerationOutOfBoundEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.tweaks.fixChunkGenerationOutOfBound"), config.fixChunkGenerationOutOfBound)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("ultimate_scaler.options.tweaks.fixChunkGenerationOutOfBound.tooltip"))
                .build();
        BooleanListEntry expandDatapackValueRangeEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.tweaks.expandDatapackValueRange"), config.expandDatapackValueRange)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("ultimate_scaler.options.tweaks.expandDatapackValueRange.tooltip").append(Text.translatable("ultimate_scaler.options.require_restart").styled(s -> s.withColor(Formatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry expandWorldBorderEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.tweaks.expandWorldBorder"), config.expandWorldBorder)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("ultimate_scaler.options.tweaks.expandWorldBorder.tooltip").append(Text.translatable("ultimate_scaler.options.require_restart").styled(s -> s.withColor(Formatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry fixMineshaftCannotGenerateEntry = entryBuilder.startBooleanToggle(Text.translatable("ultimate_scaler.options.tweaks.fixMineshaftCannotGenerate"), config.fixMineshaftCannotGenerate)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("ultimate_scaler.options.tweaks.fixMineshaftCannotGenerate.tooltip"))
                .build();
        tweaks.addEntry(fixEndRingsEntry);
        if (VersionHelper.isVersionAtLeast("1.21.2")) {
            tweaks.addEntry(fixChunkGenerationOutOfBoundEntry);
        }
        tweaks.addEntry(expandDatapackValueRangeEntry);
        tweaks.addEntry(expandWorldBorderEntry);
        tweaks.addEntry(fixMineshaftCannotGenerateEntry);

        ConfigCategory faq = builder.getOrCreateCategory(Text.translatable("ultimate_scaler.options.faq"));
        TextListEntry question1 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question1").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer1 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer1")).build();
        TextListEntry question2 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question2").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer2 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer2")).build();
        TextListEntry question3 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question3").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer3 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer3")).build();
        TextListEntry question4 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question4").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer4 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer4.1")
                .append(Text.translatable("ultimate_scaler.options.faq.answer4.2").styled(s -> s.withHoverEvent(TextEventFactory.createShowTextEvent(Text.translatable("chat.copy.click"))).withClickEvent(TextEventFactory.createCopyEvent("/locate pos 1808764368955220359643137 8 1808764368955220359643137"))))
                .append(Text.translatable("ultimate_scaler.options.faq.answer4.3"))).build();
        TextListEntry question5 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question5").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer5 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer5")).build();
        TextListEntry question6 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question6").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer6 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer6")).build();
        TextListEntry question7 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question7").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer7 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer7")).build();
        TextListEntry question8 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question8").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer8 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer8")).build();
        TextListEntry question9 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.question9").styled(s -> s.withColor(Formatting.BLUE))).build();
        TextListEntry answer9 = entryBuilder.startTextDescription(Text.translatable("ultimate_scaler.options.faq.answer9")).build();

        faq.addEntry(question1);
        faq.addEntry(answer1);
        faq.addEntry(question2);
        faq.addEntry(answer2);
        faq.addEntry(question3);
        faq.addEntry(answer3);
        faq.addEntry(question4);
        faq.addEntry(answer4);
        faq.addEntry(question5);
        faq.addEntry(answer5);
        faq.addEntry(question6);
        faq.addEntry(answer6);
        faq.addEntry(question7);
        faq.addEntry(answer7);
        faq.addEntry(question8);
        faq.addEntry(answer8);
        faq.addEntry(question9);
        faq.addEntry(answer9);

        // 保存逻辑。将输入值保存到 config 实例中，再尝试保存到文件中
        builder.setSavingRunnable(() -> {
            config.globalBigDecimalOffset = globalOffsetEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.globalBigDecimalScale = globalScaleEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            config.optionMenuKeyCode = optionMenuEntry.getValue().getKeyCode().getCode();
            config.optionMenuModifierValue = optionMenuEntry.getValue().getModifier().getValue();
            config.showTerrainPos = showTerrainPosEntry.getValue();
            config.farLandsPos = farLandsPosEntry.getValue();
            config.maintainPrecisionCustomDivisor = maintainPrecisionCustomDivisorEntry.getValue();
            config.limitReturnValue = limitReturnValueEntry.getValue();
            config.maxNoiseLogarithmValue = maxNoiseValueEntry.getValue();
            config.replaceDefaultFluid = replaceDefaultFluidEntry.getValue();
            config.replaceDefaultFluidBlock = Registries.BLOCK.getId(replaceDefaultFluidBlockEntry.getValue()).toString();
            config.replaceUndergroundLava = replaceUndergroundLavaEntry.getValue();
            config.replaceUndergroundLavaBlock = Registries.BLOCK.getId(replaceUndergroundLavaBlockEntry.getValue()).toString();
            config.extraYOffset = extraYOffsetEntry.getValue();
            config.bigIntegerRewrite = bigIntegerRewriteEntry.getValue();
            config.fixEndRings = fixEndRingsEntry.getValue();
            config.fixChunkGenerationOutOfBound = fixChunkGenerationOutOfBoundEntry.getValue();
            config.expandDatapackValueRange = expandDatapackValueRangeEntry.getValue();
            config.expandWorldBorder = expandWorldBorderEntry.getValue();
            config.fixMineshaftCannotGenerate = fixMineshaftCannotGenerateEntry.getValue();
            try {
                UltimateScalerOptions.saveConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return builder;
    }
}
