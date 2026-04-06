package me.inf32768.ultimate_scaler.util;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

// AI 写的神秘东西，在下看不懂，反正暂时用不上而且迟早得换掉就不写注释了
public class RegistryAccessor {

    private static final ConcurrentMap<Class<?>, MethodHandle> GET_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Class<?> IDENTIFIER_CLASS = Identifier.class;
    private static final Class<?> OPTIONAL_CLASS = Optional.class;


    /**
     * 跨版本兼容的 Registry.get() 方法
     *
     * @param registry 注册表实例
     * @param id 资源标识符
     * @return 注册表中的条目（可能为 null）
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Registry<T> registry, Identifier id) {
        try {
            MethodHandle handle = resolveGetMethod(registry.getClass());
            Object result = handle.invoke(registry, id);

            // 处理 Optional 返回类型（1.18+）
            if (OPTIONAL_CLASS.isInstance(result)) {
                Optional<T> optional = (Optional<T>) result;
                return optional.orElse(null);
            }

            return (T) result;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke Registry.get() for id: " + id, e);
        }
    }

    private static MethodHandle resolveGetMethod(Class<?> registryClass) {
        return GET_METHOD_CACHE.computeIfAbsent(registryClass, cls -> {
            try {
                return resolveGetMethodBySignature(cls);
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve Registry.get() method for " + cls.getName(), e);
            }
        });
    }

    private static MethodHandle resolveGetMethodBySignature(Class<?> registryClass)
            throws ReflectiveOperationException {

        Method exactMatch = null;
        Method fallbackMatch = null;

        for (Method method : Stream.concat(Arrays.stream(registryClass.getMethods()), Arrays.stream(registryClass.getDeclaredMethods())).toList()) {
            if (isValidGetSignature(method)) {
                if (method.getParameterTypes()[0] == IDENTIFIER_CLASS) {
                    exactMatch = method;
                } else if (fallbackMatch == null) {
                    fallbackMatch = method;
                }
            }
        }

        Method selectedMethod = exactMatch != null ? exactMatch : fallbackMatch;
        if (selectedMethod == null) {
            throw new NoSuchMethodException("No suitable get method found in " + registryClass.getName());
        }

        if (!selectedMethod.trySetAccessible()) {
            throw new IllegalAccessException("Cannot access method: " + selectedMethod.getName());
        }

        return MethodHandles.lookup().unreflect(selectedMethod);
    }

    private static boolean isValidGetSignature(Method method) {
        if (method.getParameterCount() != 1) return false;

        Class<?> paramType = method.getParameterTypes()[0];
        if (!IDENTIFIER_CLASS.isAssignableFrom(paramType)) return false;

        return method.getReturnType() != void.class;
    }
}