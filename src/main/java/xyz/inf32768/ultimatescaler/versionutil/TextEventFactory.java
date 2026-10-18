package xyz.inf32768.ultimatescaler.versionutil;

import net.fabricmc.loader.api.Version;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.lang.reflect.Constructor;
import java.net.URI;

/**
 * 跨版本兼容的（部分）文本事件创建工厂类，用于适配 Minecraft 1.21.5 中文本事件创建语法的变更。
 */
public class TextEventFactory {
    /**
     * 点击复制到剪贴板事件的构造器。
     * <p>
     * 1.21.5 前创建此事件的语法：{@code new ClickEvent(HoverEvent.Action.SHOW_TEXT, uri)}；
     * <p>
     * 1.21.5 及以后创建此事件的语法：{@code new ClickEvent.CopyToClipboard(uri)}。
     */
    private static final Constructor<?> CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR;
    private static final Constructor<?> CLICK_EVENT_OPEN_URL_CONSTRUCTOR;
    /**
     * 悬浮提示文本的构造器。
     * <p>
     * 1.21.5 前创建此事件的语法：{@code new HoverEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uri)}；
     * <p>
     * 1.21.5 及以后创建此事件的语法：{@code new HoverEvent.ShowText(uri)}。
     */
    private static final Constructor<?> HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR;
    /**
     * 使用新版语法的最低 Minecraft 版本（含）。
     */
    private static final Version VERSION_THRESHOLD = VersionUtil.parse("1.21.5");

    static {
        // 从 ClickEvent 和 HoverEvent 类中提取需要的构造方法
        if (VersionUtil.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR = Class.forName(ClickEvent.class.getName() + "$CopyToClipboard").getConstructor(String.class);
                CLICK_EVENT_OPEN_URL_CONSTRUCTOR = Class.forName(ClickEvent.class.getName() + "$OpenUrl").getConstructor(URI.class);
                HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR = Class.forName(HoverEvent.class.getName() + "$ShowText").getConstructor(Component.class);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR = Class.forName(ClickEvent.class.getName()).getConstructor(ClickEvent.Action.class, String.class);
                CLICK_EVENT_OPEN_URL_CONSTRUCTOR = CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR;
                HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR = Class.forName(HoverEvent.class.getName()).getConstructor(HoverEvent.Action.class, Object.class);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private TextEventFactory() {
    }

    /**
     * 创建复制事件：在鼠标点击时将特定文本复制到剪贴板。
     * <p>
     * 用法：{@code (TextObj).withClickEvent(TextEventFactory.createCopyEvent(...))}。
     *
     * @param text 要复制到剪贴板的文本。
     * @return 点击事件，可供 {@code withClickEvent(...)} 方法使用。
     */
    @SuppressWarnings("JavaReflectionInvocation") // 不同版本获取到的构造函数不同，因此此警告通常不会导致实际问题
    public static ClickEvent createCopyEvent(String text) {
        if (VersionUtil.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                return (ClickEvent) CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR.newInstance(text);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (ClickEvent) CLICK_EVENT_COPY_TO_CLIPBOARD_CONSTRUCTOR.newInstance(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 创建打开链接事件：在鼠标点击时将在浏览器中打开特定链接。
     * <p>
     * 用法：{@code (TextObj).withClickEvent(TextEventFactory.createOpenUrlEvent(...))}。
     *
     * @param uri 要打开的链接。
     * @return 点击事件，可供 {@code withClickEvent(...)} 方法使用。
     */
    @SuppressWarnings("JavaReflectionInvocation")
    public static ClickEvent createOpenUrlEvent(String uri) {
        if (VersionUtil.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                return (ClickEvent) CLICK_EVENT_OPEN_URL_CONSTRUCTOR.newInstance(URI.create(uri));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (ClickEvent) CLICK_EVENT_OPEN_URL_CONSTRUCTOR.newInstance(ClickEvent.Action.OPEN_URL, uri);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 创建悬浮文本事件：在鼠标悬浮时展示特定文本。
     * <p>
     * 用法：{@code (StyleObj).withHoverEvent(TextEventFactory.createShowTextEvent(...))}。
     *
     * @param text 要展示的文本。
     * @return 悬浮事件，可供 {@code withHoverEvent(...)} 方法使用。
     */
    @SuppressWarnings("JavaReflectionInvocation")
    public static HoverEvent createShowTextEvent(Component text) {
        if (VersionUtil.isVersionAtLeast(VERSION_THRESHOLD)) {
            try {
                return (HoverEvent) HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR.newInstance(text);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (HoverEvent) HOVER_EVENT_SHOW_TEXT_CONSTRUCTOR.newInstance(HoverEvent.Action.SHOW_TEXT, text);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
