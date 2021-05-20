package com.github.rntrp.lambdavsclassicreflectionbenchmark;

import java.beans.PropertyDescriptor;
import java.lang.invoke.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LambdaUtils {
    private static final String GETTER_NAME = "apply";
    private static final MethodType GETTER_TYPE = MethodType.methodType(Function.class);
    private static final MethodType GETTER_SIGNATURE = MethodType.methodType(Object.class, Object.class);

    private static final String SETTER_NAME = "accept";
    private static final MethodType SETTER_TYPE = MethodType.methodType(BiConsumer.class);
    private static final MethodType SETTER_SIGNATURE = MethodType.methodType(Void.TYPE, Object.class, Object.class);

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private LambdaUtils() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    public static Function<Object, Object> createGetter(final Class<?> clazz, final String fieldName) {
        return createGetter(clazz, fieldName, LOOKUP);
    }

    @SuppressWarnings("unchecked")
    public static Function<Object, Object> createGetter(final Class<?> clazz, final String fieldName, final MethodHandles.Lookup lookup) {
        try {
            MethodHandle getterHandle = lookup.unreflect(new PropertyDescriptor(fieldName, clazz).getReadMethod());
            CallSite callSite = LambdaMetafactory.metafactory(lookup, GETTER_NAME, GETTER_TYPE, GETTER_SIGNATURE, getterHandle, getterHandle.type());
            return (Function<Object, Object>) callSite.getTarget().invokeExact();
        } catch (Throwable t) {
            String msg = String.format("Unable to create getter for field %s (%s).", clazz.getName(), fieldName);
            throw new IllegalArgumentException(msg, t);
        }
    }

    public static BiConsumer<Object, Object> createSetter(final Class<?> clazz, final String fieldName) {
        return createSetter(clazz, fieldName, LOOKUP);
    }

    @SuppressWarnings("unchecked")
    public static BiConsumer<Object, Object> createSetter(final Class<?> clazz, final String fieldName, final MethodHandles.Lookup lookup) {
        try {
            MethodHandle setterHandle = lookup.unreflect(new PropertyDescriptor(fieldName, clazz).getWriteMethod());
            CallSite callSite = LambdaMetafactory.metafactory(lookup, SETTER_NAME, SETTER_TYPE, SETTER_SIGNATURE, setterHandle, setterHandle.type());
            return (BiConsumer<Object, Object>) callSite.getTarget().invokeExact();
        } catch (Throwable t) {
            String msg = String.format("Unable to create setter for field %s (%s).", clazz.getName(), fieldName);
            throw new IllegalArgumentException(msg, t);
        }
    }
}
