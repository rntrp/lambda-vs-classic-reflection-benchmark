# lambda-vs-classic-reflection-benchmark
Comparing dynamic method invocation performance between lambda proxies and classic reflection.

## Motivation
One common use case, when working with JavaBeans or POJOs or DTOs etc., is dynamic access to its properties by its names.
Application is provided with the name of the property, which is a mere member variable of a class with a getter and a setter method.
A line of code is worth a thousand words, hence let's look at the following example:
```java
// A simple JavaBean:
public class MyBean implements java.io.Serializable {
  private String str;
  public String getStr() { return str; }
  public void setStr(String str) { this.str = str; }
}
```
The package `java.beans` provides useful utilities for finding getters and setters by its property name using Java reflection API.
Given the property `"str"`, one common way to find corresponding getter and setter is via `java.beans.PropertyDescriptor`:
```java
java.beans.PropertyDescriptor pd = new java.beans.PropertyDescriptor("str", MyBean.class);
java.lang.reflect.Method getter = pd.getReadMethod();
java.lang.reflect.Method setter = pd.getWriteMethod();
```
Hence, reflective method invocation looks like this:
```java
String str = (String) getter.invoke(myBean);
setter.invoke(myBean, "new value");
```
We can also cache these method references if `MyBean` is expected to be used often, so that we don't need to find these methods via `PropertyDescriptor` each time we access its properties.
This approach was battle tested for decades and is found across myriads of prominent Java libraries.
However, reflective access comes at a cost.
Java reflection API has been surely optimized over the years of its existence, but there is still a certain overhead when compared to direct method calls.

So, now consider Java 8 lambda expressions:
```java
// Create a lambda proxy for the getter:
java.util.function.Function<MyBean, String> getterProxy = myBean -> myBean.getStr();
// Same for the setter:
java.util.function.BiConsumer<MyBean, String> setterProxy = (myBean, str) -> myBean.setStr(str);
```
Such lambda proxy adds an extra layer to method stack, but it must be performant enough, since lambdas are heavily used within new Java APIs.
We also can cache these lambda references, e.g. in a map, but we need to do it manually for each method call.
If only there was some way to create lambdas dynamically...

Well, there is. `java.lang.invoke.LambdaMetafactory` is able to create such proxies from method references.
```java
import java.lang.function.*;
import java.lang.invoke.*;
// ...
java.beans.PropertyDescriptor pd = new java.beans.PropertyDescriptor("str", MyBean.class);
MethodHandles.Lookup lookup = MethodHandles.lookup();
// Getter proxy:
MethodHandle getterHandle = lookup.unreflect(pd.getReadMethod());
MethodType getterType = MethodType.methodType(Function.class);
MethodType getterSignature = MethodType.methodType(Object.class, Object.class);
CallSite getterCS = LambdaMetafactory.metafactory(lookup, "apply", getterType, getterSignature, getterHandle, getterHandle.type());
Function<Object, Object> getterProxy = (Function<Object, Object>) getterCS.getTarget().invokeExact();
// Setter proxy:
MethodHandle setterHandle = lookup.unreflect(pd.getWriteMethod());
MethodType setterType = MethodType.methodType(BiConsumer.class);
MethodType setterSignature = MethodType.methodType(Void.TYPE, Object.class, Object.class);
CallSite setterCS = LambdaMetafactory.metafactory(lookup, "accept", setterType, setterSignature, setterHandle, setterHandle.type());
BiConsumer<Object, Object> setterProxy = (BiConsumer<Object, Object>) setterCS.getTarget().invokeExact();
```
That's a pretty bulky construction up there, but we can simply pack this code in some (static) method and reuse it.

We finally come to the point. Both approaches add some performance overhead to method invocation.
*We want to measure, which approach is faster: classic reflictive method access or indirect method call via dynamically created lambda proxy.*

## Results
```
Benchmark                                          Mode   Cnt  Score   Error  Units
LambdaVsReflectionBenchmark.benchDirectGetter      avgt  1000  0,274 ± 0,002  us/op
LambdaVsReflectionBenchmark.benchDirectSetter      avgt  1000  0,318 ± 0,001  us/op
LambdaVsReflectionBenchmark.benchLambdaGetter      avgt  1000  0,322 ± 0,001  us/op
LambdaVsReflectionBenchmark.benchLambdaSetter      avgt  1000  0,366 ± 0,002  us/op
LambdaVsReflectionBenchmark.benchReflectionGetter  avgt  1000  0,604 ± 0,004  us/op
LambdaVsReflectionBenchmark.benchReflectionSetter  avgt  1000  0,805 ± 0,005  us/op
```
