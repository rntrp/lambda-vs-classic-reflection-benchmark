package com.github.rntrp.lambdavsclassicreflectionbenchmark;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.rntrp.lambdavsclassicreflectionbenchmark.ReflectionUtils.getGetter;
import static com.github.rntrp.lambdavsclassicreflectionbenchmark.ReflectionUtils.getSetter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestReflectionUtils {
    @Test
    void testGetGetter() throws InvocationTargetException, IllegalAccessException {
        TestPojo testPojo = new TestPojo();
        testPojo.setField("test");
        Method method = getGetter(TestPojo.class, "field");
        assertEquals("test", method.invoke(testPojo));
    }

    @Test
    void testGetSetter() throws InvocationTargetException, IllegalAccessException {
        TestPojo testPojo = new TestPojo();
        Method method = getSetter(TestPojo.class, "field");
        method.invoke(testPojo, "test");
        assertEquals("test", testPojo.getField());
    }
}
