package xyz.inf32768.ultimatescaler.versionutil;

import net.fabricmc.loader.api.Version;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * 跨版本兼容的 {@link Registry#getValue(ResourceLocation)} 方法。
 * <p>
 * 由于 1.21.2 中此方法的 Intermediary 映射名被更改，因此在此版本前/后编译的模组无法运行于另一个版本。
 * <p>
 * 故设置此兼容层，基于反射和方法名和环境版本动态匹配此方法。
 */
public class RegistryAccessor {
    private RegistryAccessor() {}

    // 反射查找需要用到的类常量
    private static final Class<?> REGISTRY_CLASS = Registry.class;
    private static final Class<?> RESOURCE_LOCATION_CLASS = ResourceLocation.class;
    // Mojang 映射名中的方法名，用于适配开发环境
    private static final String NAMED_METHOD_NAME = "getValue";
    // Intermediary 映射名中的方法名，用于适配运行时环境
    private static final String OLD_INTERMEDIARY_METHOD_NAME = "method_10223";
    private static final String NEW_INTERMEDIARY_METHOD_NAME = "method_63535";
    // 方法名变更出现的版本
    private static final Version VERSION_THRESHOLD = VersionUtil.parse("1.21.2");

    // 匹配到的方法
    private static final MethodHandle GET_METHOD;

    static {
        String methodName;
        if (VersionUtil.CURRENT_RUNTIME_NAMESPACE.equals("named")) {
            // 当前在开发环境中，直接使用预设的方法名
            methodName = NAMED_METHOD_NAME;
        } else {
            // 当前在运行时环境中，根据环境版本选择方法名
            boolean isNewVersion = VersionUtil.isVersionAtLeast(VERSION_THRESHOLD);
            methodName = isNewVersion ? NEW_INTERMEDIARY_METHOD_NAME : OLD_INTERMEDIARY_METHOD_NAME;
        }

        // 方法签名：T getValue(ResourceLocation resourceLocation)
        MethodType mt = MethodType.methodType(Object.class, RESOURCE_LOCATION_CLASS);

        try {
            GET_METHOD = MethodHandles.publicLookup().findVirtual(REGISTRY_CLASS, methodName, mt);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 跨版本兼容的 {@link Registry#getValue(ResourceLocation)} 方法
     * <p>
     * 示例：{@code RegistryAccessor.get(BuiltInRegistries.BLOCK, ResourceLocation.parse("minecraft:stone"))}
     * <p>
     * 等价于 {@code BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse("minecraft:stone")))}
     * @param registry 注册表实例
     * @param id 资源标识符
     * @return 注册表中的条目
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Registry<T> registry, ResourceLocation id) {
        try {
            return (T) GET_METHOD.invoke(registry, id);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}