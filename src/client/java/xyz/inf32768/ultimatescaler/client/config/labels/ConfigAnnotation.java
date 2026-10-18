package xyz.inf32768.ultimatescaler.client.config.labels;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 配置选项标签类和枚举使用的接口，实现此接口可用于标签的自动化规范添加。
 *
 * @see CommonLabels
 * @see DistanceEffectMeta
 */
public interface ConfigAnnotation {
    /**
     * 将标签附加到文本组件中。可将标识附加在文本开头，或将完整图例说明附加在文本末尾。实现配置项标签的自动化添加。
     * <p>
     * 注意：Cloth Config 配置项名称组件中的所有悬浮事件都不可生效，因此请避免添加。
     *
     * @param component   原始文本组件，标签将被添加在这之上。
     * @param prefix      若为真，则将标签的简短标识附加在文本开头，否则将完整图例说明附加在文本末尾。
     * @param annotations 要添加的标签序列。实际添加时将按照传入顺序依次添加。
     * @return 带有规范标签的文本组件。
     */
    static MutableComponent addAnnotations(MutableComponent component, boolean prefix, ConfigAnnotation... annotations) {
        MutableComponent result = Component.empty();
        if (prefix) {
            for (ConfigAnnotation label : annotations) {
                result.append(label.getTag()).append(" ");
            }
            result.append(component);
        } else {
            if (!component.equals(Component.empty())) result.append(component).append("\n");
            result.append(Component.translatable("ultimatescaler.config.new.labels_description").withStyle(ChatFormatting.DARK_GRAY));
            for (ConfigAnnotation label : annotations) {
                result.append(Component.literal("\n")).append(label.getRepresentation());
            }
        }
        return result;
    }

    /**
     * 获取可直接附加在配置项名称开头的简短标识。
     *
     * @return 前缀标识的文本组件
     */
    MutableComponent getTag();

    /**
     * 获取标签的描述性文本组件。
     *
     * @return 描述文本
     */
    MutableComponent getDescription();

    /**
     * 获取标签的标识和描述的结合文本组件，实现标签的完整介绍。可用于制作标签图例等。
     *
     * @return 完整的文本组件
     */
    default MutableComponent getRepresentation() {
        return Component.empty().append(getTag()).append(": ").append(getDescription());
    }
}
