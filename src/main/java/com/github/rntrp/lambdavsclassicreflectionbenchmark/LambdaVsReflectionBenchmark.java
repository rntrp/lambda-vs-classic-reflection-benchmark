package com.github.rntrp.lambdavsclassicreflectionbenchmark;

import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Method;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

@BenchmarkMode(Mode.AverageTime)
@Fork(warmups = 1, value = 1)
@Warmup(iterations = 200, batchSize = 100, time = 10, timeUnit = TimeUnit.MICROSECONDS)
@Measurement(iterations = 1000, batchSize = 100, time = 10, timeUnit = TimeUnit.MICROSECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@SuppressWarnings("unused")
public class LambdaVsReflectionBenchmark {
    @Benchmark
    public Object benchDirectGetter(CommonState state) {
        return state.pojo.getField();
    }

    @Benchmark
    public Object benchDirectSetter(CommonState state) {
        state.pojo.setField(state.newValue);
        return state.pojo;
    }
    
    @Benchmark
    public Object benchLambdaGetter(LambdaState state) {
        return state.getter.apply(state.pojo);
    }

    @Benchmark
    public Object benchLambdaSetter(LambdaState state) {
        state.setter.accept(state.pojo, state.newValue);
        return state.pojo;
    }

    @Benchmark
    public Object benchReflectionGetter(ReflectionState state) throws Exception {
        return state.getter.invoke(state.pojo);
    }

    @Benchmark
    public Object benchReflectionSetter(ReflectionState state) throws Exception {
        state.setter.invoke(state.pojo, state.newValue);
        return state.pojo;
    }

    public static class BenchPojo {
        private String field;

        public BenchPojo(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    @State(Scope.Benchmark)
    public static class CommonState {
        /**
         * @see BenchPojo#field
         */
        static final String FIELD_NAME = "field";

        private static final SplittableRandom RAND = new SplittableRandom();
        private static final int NUM_CHARS = 8;
        final BenchPojo pojo = new BenchPojo("_initial");
        final String newValue = new String(RAND.ints(97, 123).limit(NUM_CHARS).toArray(), 0, NUM_CHARS);
    }

    @State(Scope.Benchmark)
    public static class ReflectionState extends CommonState {
        private final Method getter = ReflectionUtils.getGetter(BenchPojo.class, FIELD_NAME);
        private final Method setter = ReflectionUtils.getSetter(BenchPojo.class, FIELD_NAME);
    }

    @State(Scope.Benchmark)
    public static class LambdaState extends CommonState {
        private final Function<Object, Object> getter = LambdaUtils.createGetter(BenchPojo.class, FIELD_NAME);
        private final BiConsumer<Object, Object> setter = LambdaUtils.createSetter(BenchPojo.class, FIELD_NAME);
    }
}
