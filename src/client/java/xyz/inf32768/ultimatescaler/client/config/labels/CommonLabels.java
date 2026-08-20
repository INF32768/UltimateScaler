package xyz.inf32768.ultimatescaler.client.config.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 所有配置选项通用的标签枚举，用于描述选项的元信息（如环境需求、兼容性警告等）。
 */
public enum CommonLabels implements ConfigAnnotation {
    EXPERIMENTAL(Component.literal("[Exp]").withStyle(s -> (s.withColor(ChatFormatting.DARK_RED).withBold(true))), Component.literal("仍处于测试阶段，可能不稳定或存在风险")),
    FAULTABLE(Component.literal("[Fau]").withStyle(ChatFormatting.RED), Component.literal("此功能导致的现象可能不符合预期")),
    RADICAL(Component.literal("[Rad]").withStyle(ChatFormatting.RED), Component.literal("此功能可能很强大，但可能具有副作用")),
    REQUIRES_RESTART(Component.literal("[ReS]").withStyle(ChatFormatting.YELLOW), Component.literal("更改可能需要重启游戏以生效")),
    REQUIRES_REENTER(Component.literal("[ReE]").withStyle(ChatFormatting.YELLOW), Component.literal("更改可能需要重进世界（重启服务端）以生效")),
    LOW_PERFORMANCE(Component.literal("[LoP]").withStyle(ChatFormatting.GOLD), Component.literal("高性能开销，可能导致卡顿")),
    OPTIMIZATION(Component.literal("[Opt]").withStyle(ChatFormatting.GREEN), Component.literal("有助于优化性能或增加稳定性")),
    CLIENT_ONLY(Component.literal("[Cln]").withStyle(ChatFormatting.BLUE), Component.literal("纯客户端功能，仅影响本地显示/操作")),
    DUAL_ENV(Component.literal("[Dual]").withStyle(ChatFormatting.BLUE), Component.literal("需客户端和服务端同时开启才可用")),
    SERVER_ONLY(Component.literal("[Srv]").withStyle(ChatFormatting.BLUE), Component.literal("纯服务端功能，客户端无需开启")),
    SINGLEPLAYER_ONLY(Component.literal("[SPO]").withStyle(ChatFormatting.BLUE), Component.literal("仅在单人游戏中可正常生效"));

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
