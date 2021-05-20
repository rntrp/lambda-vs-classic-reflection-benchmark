package com.github.rntrp.lambdavsclassicreflectionbenchmark;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private ReflectionUtils() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    public static Method getGetter(final Class<?> clazz, final String fieldName) {
        try {
            return new PropertyDescriptor(fieldName, clazz).getReadMethod();
        } catch (IntrospectionException e) {
            String msg = String.format("Unable to retrieve getter for field %s (%s).", clazz.getName(), fieldName);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public static Method getSetter(final Class<?> clazz, final String fieldName) {
        try {
            return new PropertyDescriptor(fieldName, clazz).getWriteMethod();
        } catch (IntrospectionException e) {
            String msg = String.format("Unable to retrieve setter for field %s (%s).", clazz.getName(), fieldName);
            throw new IllegalArgumentException(msg, e);
        }
    }
}
