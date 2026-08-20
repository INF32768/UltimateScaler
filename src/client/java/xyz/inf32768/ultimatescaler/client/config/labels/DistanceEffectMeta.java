package xyz.inf32768.ultimatescaler.client.config.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * “扩展与修复”配置项专用的距离现象元信息标签，利用具有特定颜色的几何图形来标识距离现象的原因和类型。
 * @see Symbol
 * @see Color
 */
public class DistanceEffectMeta implements ConfigAnnotation {
    /**
     * 距离现象发生原因（整数溢出、精度损失、人为限制等）的几何图形标识。
     */
    public enum Symbol implements ConfigAnnotation {
        INTEGER_OVERFLOW('■', Component.literal("该问题是由整数溢出所致")),
        PRECISION_LOSS('●', Component.literal("该问题是由浮点数精度损失所致")),
        BOTH('▲', Component.literal("该问题同时存在整数溢出和精度损失的因素")),
        INTENDED_RESTRICTION('◆', Component.literal("该问题很可能是人为限制"));

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
        CRASH_OR_FREEZE(ChatFormatting.BLUE, Component.literal("该问题会导致游戏卡死或崩溃")),
        CLIENT_RENDERING(ChatFormatting.GOLD, Component.literal("该问题会导致游戏客户端渲染出现异常")),
        GAME_LOGIC(ChatFormatting.DARK_GREEN, Component.literal("该问题会导致其他游戏逻辑出错")),
        WORLD_GEN(ChatFormatting.YELLOW, Component.literal("该问题会导致世界生成出现异常"));

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

    private final Symbol symbol;
    private final Color color;

    public DistanceEffectMeta(Symbol symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    @Override
    public MutableComponent getTag() {
        return symbol.getTag().withStyle(color.color);
    }

    @Override
    public MutableComponent getDescription() {
        return symbol.description.append(" | ").append(color.description);
    }
}
