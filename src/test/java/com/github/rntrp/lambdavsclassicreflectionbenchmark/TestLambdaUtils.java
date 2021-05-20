package com.github.rntrp.lambdavsclassicreflectionbenchmark;

import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.rntrp.lambdavsclassicreflectionbenchmark.LambdaUtils.createGetter;
import static com.github.rntrp.lambdavsclassicreflectionbenchmark.LambdaUtils.createSetter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestLambdaUtils {
    @Test
    void testCreateGetter() {
        TestPojo testPojo = new TestPojo();
        testPojo.setField("test");
        Function<Object, Object> getter = createGetter(TestPojo.class, "field");
        assertEquals("test", getter.apply(testPojo));
    }

    @Test
    void testCreateSetter() {
        TestPojo testPojo = new TestPojo();
        BiConsumer<Object, Object> setter = createSetter(TestPojo.class, "field");
        setter.accept(testPojo, "test");
        assertEquals("test", testPojo.getField());
    }
}
