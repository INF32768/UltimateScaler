package me.inf32768.ultimate_scaler;

import me.inf32768.ultimate_scaler.util.VersionHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (VersionHelper.isVersionAtLeast("1.21.6")) {
            if (mixinClassName.contains("MixinEntityBefore1_21_6")) return false;
        } else {
            if (mixinClassName.contains("MixinAbstractChunkHolder")) return false;
            if (mixinClassName.contains("MixinEntityAfter1_21_6")) return false;
        }

        if (VersionHelper.isVersionAtLeast("1.21.9")) {
            if (mixinClassName.contains("MixinDebugHud")) return false;
        } else {
            if (mixinClassName.contains("MixinChunkGenerationStatsDebugHudEntry")) return false;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
