package me.inf32768.ultimate_scaler.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 跨版本兼容的（部分）文本事件创建工厂类，用于适配 Minecraft 1.21.5 中文本事件创建语法的变更。
 */
public class TextEventFactory {
    /**
     * 点击复制到剪贴板事件的构造器。
     * <p>
     * 1.21.5 前创建此事件的语法：{@code new ClickEvent(HoverEvent.Action.SHOW_TEXT, text)}；
     * <p>
     * 1.21.5 及以后创建此事件的语法：{@code new ClickEvent.CopyToClipboard(text)}。
     */
    private static final Constructor<?> CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR;

    /**
     * 悬浮提示文本的构造器。
     * <p>
     * 1.21.5 前创建此事件的语法：{@code new HoverEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)}；
     * <p>
     * 1.21.5 及以后创建此事件的语法：{@code new HoverEvent.ShowText(text)}。
     */
    private static final Constructor<?> HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR;

    /**
     * 使用新版语法的最低 Minecraft 版本（含）。
     */
    private static final String VERSION_THRESHOLD = "1.21.5";

    /**
     * 创建复制事件：在鼠标点击时将特定文本复制到剪贴板。
     * <p>
     * 用法：{@code (TextObj).withClickEvent(TextEventFactory.createCopyEvent(...))}。
     * @param text 要复制到剪贴板的文本。
     * @return 点击事件，可供 {@code withClickEvent(...)} 方法使用。
     */
    public static ClickEvent createCopyEvent(String text) {
        if (VersionHelper.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                return (ClickEvent) CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR.newInstance(text);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (ClickEvent) CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR.newInstance(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 创建悬浮文本事件：在鼠标悬浮时展示特定文本。
     * <p>
     * 用法：{@code (TextObj).withHoverEvent(TextEventFactory.createShowTextEvent(...))}。
     * @param text 要展示的文本。
     * @return 悬浮事件，可供 {@code withHoverEvent(...)} 方法使用。
     */
    public static HoverEvent createShowTextEvent(Text text) {
        if (VersionHelper.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                return (HoverEvent) HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR.newInstance(text);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (HoverEvent) HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR.newInstance(HoverEvent.Action.SHOW_TEXT, text);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static {
        // 从 ClickEvent 和 HoverEvent 类中提取需要的构造方法
        if (VersionHelper.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR = Class.forName(ClickEvent.CopyToClipboard.class.getName()).getConstructor(String.class);
                HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR = Class.forName(HoverEvent.ShowText.class.getName()).getConstructor(Text.class);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR = Class.forName(ClickEvent.class.getName()).getConstructor(ClickEvent.Action.class, String.class);
                HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR = Class.forName(HoverEvent.class.getName()).getConstructor(HoverEvent.Action.class, Object.class);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
