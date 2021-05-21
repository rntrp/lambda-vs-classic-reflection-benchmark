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

    @State(Scope.Thread)
    public static class CommonState {
        /**
         * @see BenchPojo#field
         */
        static final String FIELD_NAME = "field";

        private static final SplittableRandom RAND = new SplittableRandom();

        BenchPojo pojo;
        String newValue;

        @Setup(Level.Trial)
        public void setup() {
            pojo = new BenchPojo("_initial");
            // prevent any possible optimization of setter method execution, if using the same string all over again:
            int numChars = 8, origin = 97, bound = 123; // random string [a-z]{8}
            newValue = new String(RAND.ints(origin, bound).limit(numChars).toArray(), 0, numChars);
        }
    }

    @State(Scope.Thread)
    public static class ReflectionState extends CommonState {
        private final Method getter = ReflectionUtils.getGetter(BenchPojo.class, FIELD_NAME);
        private final Method setter = ReflectionUtils.getSetter(BenchPojo.class, FIELD_NAME);
    }

    @State(Scope.Thread)
    public static class LambdaState extends CommonState {
        private final Function<Object, Object> getter = LambdaUtils.createGetter(BenchPojo.class, FIELD_NAME);
        private final BiConsumer<Object, Object> setter = LambdaUtils.createSetter(BenchPojo.class, FIELD_NAME);
    }
}
