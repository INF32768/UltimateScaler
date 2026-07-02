package xyz.inf32768.ultimatescaler;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.inf32768.ultimatescaler.versionutil.VersionUtil;

import java.util.List;
import java.util.Set;

/**
 * 自定义 Mixin 配置插件，目前用于根据版本控制 Mixin 应用范围
 *
 * @see IMixinConfigPlugin
 */
public final class MixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    /**
     * 获取 Mixin RefMap 配置。高版本的 Fabric Loom 默认使用 TinyRemapper，不需要用到 RefMap，因此返回 {@code null} 来禁用 RefMap 配置。
     * @return {@code null}
     */
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * 根据版本控制启用的 Mixin。
     * <p>
     * 目前版本控制的 Mixin 有：
     * <ul>
     *     <li>{@link xyz.inf32768.ultimatescaler.mixin.fixing.MixinGenerationChunkHolder}：仅在 1.21.2 及以上版本启用</li>
     *     <li>{@link xyz.inf32768.ultimatescaler.mixin.border.MixinEntityBefore1_21_6}：仅在 1.21.5 及以下版本启用</li>
     *     <li>{@link xyz.inf32768.ultimatescaler.mixin.border.MixinEntityAfter1_21_6}：仅在 1.21.6 及以上版本启用</li>
     * </ul>
     * @param targetClassName 目标类的全限定名
     * @param mixinClassName 目标类的 Mixin 类的全限定名
     * @return {@code true} 启用这个 Mixin，{@code false} 禁用这个 Mixin
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (VersionUtil.isVersionAtLeast(VersionUtil.parse("1.21.6"))) {
            if (mixinClassName.contains("MixinEntityBefore1_21_6")) return false;
        } else {
            if (mixinClassName.contains("MixinEntityAfter1_21_6")) return false;
        }

        if (!VersionUtil.isVersionAtLeast(VersionUtil.parse("1.21.2"))) {
            if (mixinClassName.contains("MixinGenerationChunkHolder")) return false;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
