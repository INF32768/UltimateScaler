package xyz.inf32768.ultimatescaler.client.config.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 所有配置选项通用的标签枚举，用于描述选项的元信息（如环境需求、兼容性警告等）。
 */
public enum CommonLabels implements ConfigAnnotation {
    EXPERIMENTAL(Component.literal("[Exp]").withStyle(s -> (s.withColor(ChatFormatting.DARK_RED).withBold(true))), Component.translatable("ultimatescaler.config.new.common_labels.experimental")),
    FAULTABLE(Component.literal("[Fau]").withStyle(ChatFormatting.RED), Component.translatable("ultimatescaler.config.new.common_labels.faultable")),
    RADICAL(Component.literal("[Rad]").withStyle(ChatFormatting.RED), Component.translatable("ultimatescaler.config.new.common_labels.radical")),
    REQUIRES_RESTART(Component.literal("[ReS]").withStyle(ChatFormatting.YELLOW), Component.translatable("ultimatescaler.config.new.common_labels.requires_restart")),
    REQUIRES_REENTER(Component.literal("[ReE]").withStyle(ChatFormatting.YELLOW), Component.translatable("ultimatescaler.config.new.common_labels.requires_re-enter")),
    LOW_PERFORMANCE(Component.literal("[LoP]").withStyle(ChatFormatting.GOLD), Component.translatable("ultimatescaler.config.new.common_labels.low_performance")),
    OPTIMIZATION(Component.literal("[Opt]").withStyle(ChatFormatting.GREEN), Component.translatable("ultimatescaler.config.new.common_labels.optimization")),
    CLIENT_ONLY(Component.literal("[Cln]").withStyle(ChatFormatting.BLUE), Component.translatable("ultimatescaler.config.new.common_labels.client_only")),
    DUAL_ENV(Component.literal("[Dual]").withStyle(ChatFormatting.BLUE), Component.translatable("ultimatescaler.config.new.common_labels.dual_env")),
    SERVER_ONLY(Component.literal("[Srv]").withStyle(ChatFormatting.BLUE), Component.translatable("ultimatescaler.config.new.common_labels.server_only")),
    SINGLEPLAYER_ONLY(Component.literal("[SPO]").withStyle(ChatFormatting.BLUE), Component.translatable("ultimatescaler.config.new.common_labels.singleplayer_only")),
    ;

    private final MutableComponent tag;
    private final MutableComponent description;

    CommonLabels(MutableComponent tag, MutableComponent description) {
        this.tag = tag;
        this.description = description;
    }

    @Override
    public MutableComponent getTag() {
        return this.tag;
    }

    @Override
    public MutableComponent getDescription() {
        return this.description;
    }

    @Override
    public MutableComponent getRepresentation() {
        return Component.empty().append(tag).append(": " + name() + ", ").append(getDescription());
    }
}
