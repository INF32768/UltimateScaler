package xyz.inf32768.ultimatescaler.client.config.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * “扩展与修复”配置项专用的距离现象元信息标签，利用具有特定颜色的几何图形来标识距离现象的原因和类型。
 *
 * @see Symbol
 * @see Color
 */
public class DistanceEffectMeta implements ConfigAnnotation {
    private final Symbol symbol;
    private final Color color;

    public DistanceEffectMeta(Symbol symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    @Override
    public MutableComponent getTag() {
        return symbol.getTag().copy().withStyle(color.color);
    }

    @Override
    public MutableComponent getDescription() {
        return symbol.description.copy().append(" | ").append(color.description);
    }

    /**
     * 距离现象发生原因（整数溢出、精度损失、人为限制等）的几何图形标识。
     */
    public enum Symbol implements ConfigAnnotation {
        INTEGER_OVERFLOW('■', Component.translatable("ultimatescaler.config.new.distance_effect_meta.integer_overflow")),
        PRECISION_LOSS('●', Component.translatable("ultimatescaler.config.new.distance_effect_meta.precision_loss")),
        BOTH('▲', Component.translatable("ultimatescaler.config.new.distance_effect_meta.both")),
        INTENDED_RESTRICTION('◆', Component.translatable("ultimatescaler.config.new.distance_effect_meta.intended_restriction")),
        ;

        private final char character;
        private final MutableComponent description;

        Symbol(char character, MutableComponent description) {
            this.character = character;
            this.description = description;
        }

        @Override
        public MutableComponent getTag() {
            return Component.literal(String.valueOf(character));
        }

        @Override
        public MutableComponent getDescription() {
            return description;
        }
    }

    /**
     * 距离现象类型（崩溃、渲染、世界生成等）的颜色标识。
     */
    public enum Color implements ConfigAnnotation {
        CRASH_OR_FREEZE(ChatFormatting.BLUE, Component.translatable("ultimatescaler.config.new.distance_effect_meta.crash_or_freeze")),
        CLIENT_RENDERING(ChatFormatting.GOLD, Component.translatable("ultimatescaler.config.new.distance_effect_meta.client_rendering")),
        GAME_LOGIC(ChatFormatting.DARK_GREEN, Component.translatable("ultimatescaler.config.new.distance_effect_meta.game_logic")),
        WORLD_GEN(ChatFormatting.YELLOW, Component.translatable("ultimatescaler.config.new.distance_effect_meta.world_gen")),
        ;

        private final ChatFormatting color;
        private final MutableComponent description;

        Color(ChatFormatting color, MutableComponent description) {
            this.color = color;
            this.description = description;
        }

        @Override
        public MutableComponent getTag() {
            return Component.literal(color.getName()).withStyle(color);
        }

        @Override
        public MutableComponent getDescription() {
            return description;
        }
    }
}
