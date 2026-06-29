package xyz.inf32768.ultimatescaler.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import xyz.inf32768.ultimatescaler.config.Config;
import xyz.inf32768.ultimatescaler.config.ConfigManager;
import xyz.inf32768.ultimatescaler.versionutil.RegistryAccessor;
import xyz.inf32768.ultimatescaler.versionutil.TextEventFactory;
import xyz.inf32768.ultimatescaler.versionutil.VersionUtil;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cloth Config API 的集成类，提供了构造配置界面的方法。
 */
@Environment(EnvType.CLIENT)
public class ClothConfigBuilder {

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

    /**
     * 定义配置界面，包括其中的配置项和常见问题的部分，以及保存的逻辑。
     * @return 配置界面构建器，共 Cloth Config API 使用。
     * @see Config.ConfigImpl
     */
    @SuppressWarnings("UnstableApiUsage")
    public static ConfigBuilder getConfigBuilder() {
        // 配置项
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Component.translatable("ultimatescaler.config"));
        builder.setGlobalized(true);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.general"));
        KeyCodeEntry optionMenuEntry = entryBuilder.startModifierKeyCodeField(Component.translatable("ultimatescaler.config.general.optionMenuKey"), ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(Config.impl.optionMenuKeyCode), Modifier.of(Config.impl.optionMenuModifierValue)))
                .setDefaultValue(ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U), Modifier.of(false, true, false)))
                .build();
        BooleanListEntry showTerrainPosEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.general.showTerrainPos"), Config.impl.showTerrainPos)
                .setDefaultValue(true)
                .build();

        general.addEntry(optionMenuEntry);
        general.addEntry(showTerrainPosEntry);

        ConfigCategory worldGen = builder.getOrCreateCategory(Component.translatable("ultimatescaler.config.worldgen"));

        TextListEntry worldGenHeader = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.worldgen.header").withStyle(s -> s.withBold(true).withColor(ChatFormatting.YELLOW))).build();
        StringListListEntry globalOffsetEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.worldgen.offset.globalOffset"), Arrays.stream(Config.impl.globalBigDecimalOffset).map(BigDecimal::toString).toList())
                .setTooltip(Component.translatable("ultimatescaler.config.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("0", "0", "0"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        StringListListEntry globalScaleEntry = entryBuilder.startStrList(Component.translatable("ultimatescaler.config.worldgen.offset.globalScale"), Arrays.stream(Config.impl.globalBigDecimalScale).map(BigDecimal::toString).toList())
                .setTooltip(Component.translatable("ultimatescaler.config.parsableDecimal.tooltip"))
                .setDefaultValue(Arrays.asList("1", "1", "1"))
                .setInsertButtonEnabled(false)
                .setDeleteButtonEnabled(false)
                .setCellErrorSupplier(DECIMAL_CELL_ERROR_SUPPLIER)
                .build();
        try {
            // farLandsPos 项在配置文件中以字符串形式存储，可能无法对应到枚举中的项，因此需要处理空指针异常防止无法构建
            entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.worldgen.farLandsPos"), Config.FarLandsPos.class, Config.impl.farLandsPos);
        } catch (NullPointerException e) {
            SystemToast.addOrUpdate(Minecraft.getInstance().getToastManager(), new SystemToast.SystemToastId(), Component.translatable("ultimatescaler.config.worldgen.offset.invalidInput"), Component.literal("For enum: " + ConfigManager.readString(Config.CONFIG_PATH, "farLandsPos")));
            Config.impl.farLandsPos = Config.FarLandsPos.DEFAULT;
        }
        EnumListEntry<Config.FarLandsPos> farLandsPosEntry = entryBuilder.startEnumSelector(Component.translatable("ultimatescaler.config.worldgen.farLandsPos"), Config.FarLandsPos.class, Config.impl.farLandsPos)
                .setDefaultValue(Config.FarLandsPos.DEFAULT)
                .setEnumNameProvider((farLandsPos) -> Component.translatable("ultimatescaler.config.worldgen.FarLandsPos." + farLandsPos.name()))
                .setTooltipSupplier((FarLandsPos) -> Optional.of(new Component[]{Component.translatable("ultimatescaler.config.worldgen.FarLandsPos." + FarLandsPos.name() + ".tooltip")}))
                .build();
        DoubleListEntry maintainPrecisionCustomDivisorEntry = entryBuilder.startDoubleField(Component.translatable("ultimatescaler.config.worldgen.maintainPrecisionCustomDivisor"), Config.impl.maintainPrecisionCustomDivisor)
                .setDefaultValue(33554432)
                .setMin(0.0)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.maintainPrecisionCustomDivisor.tooltip"))
                .setDisplayRequirement(Requirement.all(() -> farLandsPosEntry.getValue().equals(Config.FarLandsPos.CUSTOM)))
                .build();
        BooleanListEntry limitReturnValueEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.limitReturnValue"), Config.impl.limitReturnValue)
                .setDefaultValue(false)
                .build();
        IntegerListEntry maxNoiseValueEntry = entryBuilder.startIntField(Component.translatable("ultimatescaler.config.worldgen.maxNoiseLogarithmValue"), Config.impl.maxNoiseLogarithmValue)
                .setDefaultValue(7)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.maxNoiseLogarithmValue.tooltip"))
                .setDisplayRequirement(Requirement.all(limitReturnValueEntry::getValue))
                .build();
        BooleanListEntry replaceDefaultFluidEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.replaceDefaultFluid"), Config.impl.replaceDefaultFluid)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.replaceDefaultFluid.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceDefaultFluidBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(Config.impl.replaceDefaultFluidBlock))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
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
        BooleanListEntry bigIntegerRewriteEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite"), Config.impl.bigIntegerRewrite)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.1")
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.2").withStyle(s -> s.withColor(ChatFormatting.YELLOW)))
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.3"))
                        .append(Component.translatable("ultimatescaler.config.worldgen.bigIntegerRewrite.tooltip.4").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
                )
                .build();
        BooleanListEntry extraYOffsetEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.extraYOffset"), Config.impl.extraYOffset)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.extraYOffset.tooltip"))
                .build();
        BooleanListEntry replaceUndergroundLavaEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.replaceUndergroundLava"), Config.impl.replaceUndergroundLava)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.replaceUndergroundLava.tooltip"))
                .build();
        DropdownBoxEntry<Block> replaceUndergroundLavaBlockEntry = entryBuilder.startDropdownMenu(Component.empty(), DropdownMenuBuilder.TopCellElementBuilder.ofBlockObject(RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse(Config.impl.replaceUndergroundLavaBlock))), DropdownMenuBuilder.CellCreatorBuilder.ofBlockObject())
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
        BooleanListEntry fixEndRingsEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.worldgen.fixEndRings"), Config.impl.fixEndRings)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("ultimatescaler.config.worldgen.fixEndRings.tooltip"))
                .build();
        BooleanListEntry fixChunkGenerationOutOfBoundEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.fixChunkGenerationOutOfBound"), Config.impl.fixChunkGenerationOutOfBound)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.fixChunkGenerationOutOfBound.tooltip"))
                .build();
        BooleanListEntry expandDatapackValueRangeEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.expandDatapackValueRange"), Config.impl.expandDatapackValueRange)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.expandDatapackValueRange.tooltip").append(Component.translatable("ultimatescaler.config.require_restart").withStyle(s -> s.withColor(ChatFormatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry expandWorldBorderEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.expandWorldBorder"), Config.impl.expandWorldBorder)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("ultimatescaler.config.tweaks.expandWorldBorder.tooltip").append(Component.translatable("ultimatescaler.config.require_restart").withStyle(s -> s.withColor(ChatFormatting.GOLD))))
                .requireRestart()
                .build();
        BooleanListEntry fixMineshaftCannotGenerateEntry = entryBuilder.startBooleanToggle(Component.translatable("ultimatescaler.config.tweaks.fixMineshaftCannotGenerate"), Config.impl.fixMineshaftCannotGenerate)
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
        TextListEntry question1 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question1").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer1 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer1")).build();
        TextListEntry question2 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question2").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer2 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer2")).build();
        TextListEntry question3 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question3").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer3 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer3")).build();
        TextListEntry question4 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question4").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer4 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer4.1")
                .append(Component.translatable("ultimatescaler.config.faq.answer4.2").withStyle(s -> s.withHoverEvent(TextEventFactory.createShowTextEvent(Component.translatable("chat.copy.click"))).withClickEvent(TextEventFactory.createCopyEvent("/locate pos 1808764368955220359643137 8 1808764368955220359643137"))))
                .append(Component.translatable("ultimatescaler.config.faq.answer4.3"))).build();
        TextListEntry question5 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question5").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer5 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer5")).build();
        TextListEntry question6 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question6").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer6 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer6")).build();
        TextListEntry question7 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question7").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer7 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer7")).build();
        TextListEntry question8 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question8").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer8 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer8")).build();
        TextListEntry question9 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.question9").withStyle(s -> s.withColor(ChatFormatting.BLUE))).build();
        TextListEntry answer9 = entryBuilder.startTextDescription(Component.translatable("ultimatescaler.config.faq.answer9")).build();

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

        // 保存逻辑。将输入值保存到 Config.impl 实例中，再尝试保存到文件中
        builder.setSavingRunnable(() -> {
            Config.impl.globalBigDecimalOffset = globalOffsetEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            Config.impl.globalBigDecimalScale = globalScaleEntry.getValue().stream().map(BigDecimal::new).toArray(BigDecimal[]::new);
            Config.impl.optionMenuKeyCode = optionMenuEntry.getValue().getKeyCode().getValue();
            Config.impl.optionMenuModifierValue = optionMenuEntry.getValue().getModifier().getValue();
            Config.impl.showTerrainPos = showTerrainPosEntry.getValue();
            Config.impl.farLandsPos = farLandsPosEntry.getValue();
            Config.impl.maintainPrecisionCustomDivisor = maintainPrecisionCustomDivisorEntry.getValue();
            Config.impl.limitReturnValue = limitReturnValueEntry.getValue();
            Config.impl.maxNoiseLogarithmValue = maxNoiseValueEntry.getValue();
            Config.impl.replaceDefaultFluid = replaceDefaultFluidEntry.getValue();
            Config.impl.replaceDefaultFluidBlock = BuiltInRegistries.BLOCK.getKey(replaceDefaultFluidBlockEntry.getValue()).toString();
            Config.impl.replaceUndergroundLava = replaceUndergroundLavaEntry.getValue();
            Config.impl.replaceUndergroundLavaBlock = BuiltInRegistries.BLOCK.getKey(replaceUndergroundLavaBlockEntry.getValue()).toString();
            Config.impl.extraYOffset = extraYOffsetEntry.getValue();
            Config.impl.bigIntegerRewrite = bigIntegerRewriteEntry.getValue();
            Config.impl.fixEndRings = fixEndRingsEntry.getValue();
            Config.impl.fixChunkGenerationOutOfBound = fixChunkGenerationOutOfBoundEntry.getValue();
            Config.impl.expandDatapackValueRange = expandDatapackValueRangeEntry.getValue();
            Config.impl.expandWorldBorder = expandWorldBorderEntry.getValue();
            Config.impl.fixMineshaftCannotGenerate = fixMineshaftCannotGenerateEntry.getValue();
            try {
                Config.saveConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return builder;
    }
}
