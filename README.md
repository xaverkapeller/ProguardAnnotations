# ProguardAnnotations

Makes dealing with Proguard simple and easy!

* **Short and concise, but powerful API**: Define your rules with a few simple annotations and ProguardAnnotations takes care of the rest!
* **Simplifies development**: You can move classes and refactor your code without worrying about breaking any proguard rules!
* **Works with your current setup**: Don't want to migrate your whole project to try out ProguardAnnotations? You don't have to! While using ProguardAnnotations you can still define rules manually. No conflicts, no problems.

## Installation

To use Proguard Annotations add the following at the very top of your build.gradle file:

```groovy
plugins {
    id "com.github.wrdlbrnft.proguard-annotations" version "0.2.0.36"
}
```

And you are done! Now you can use ProguardAnnotations.

## How to use it

ProguardAnnotations currently provides five different annotations with which you can define your proguard rules:

* `@KeepClass`: Annotate a class, interface or enum with this annotation to keep it and its members. You can also modify what exactly is kept from the class by specifying the `KeepSetting` on the annotation (explained below).
* `@KeepMethod`: Annotate a method with this annotation to keep it. Only keeps the method, not the class, interface or enum which contains it.
* `@KeepField`: Annotate a field with this annotation to keep it. Only keeps the method, not the class, interface or enum which contains it.
* `@KeepName`: Annotate a class, interface or enum with this annotation to just keep the name, and not any members. You can still use `@KeepField` or `@KeepMethod` to selectively keep members from the class.
* `@DontKeep`: Annotate a method or field with this annotation to specifically not keep it.

## Examples

In most cases the `@KeepClass` annotation is all you need! By default it will keep the annotated class and all of its members. For example:

```java
@KeepClass
public interface ExampleInterface {
    void foo();
    void bar();
}
```

If you want don't want to keep a specific method you can use `@DontKeep`:

```java
@KeepClass
public interface ExampleInterface {
    void foo();
    
    @DontKeep
    void bar();
}
```

You can use the `KeepSetting` enum to set what exactly is kept from a class annotated with `@KeepClass`. A few examples:

Keep only public members and the name of the class:
```java
@KeepClass(KeepSetting.PUBLIC_MEMBERS)
public class ExampleClass {
    
    // This is kept
    public static int SOME_ID = 0x01;
    
    // This is kept as well
    public void doIt() {
        ...
    }
    
    // This is not kept
    private String mSomeValue;
    
    // This is also not kept
    String void doSomethingElse() {
        ...
    }
}
```

Keep package local methods and public fields as well as the name of the class.
```java
@KeepClass({KeepSetting.PACKAGE_LOCAL_METHODS, KeepSetting.PUBLIC_FIELDS)
public class ExampleClass {
    
    // This is kept
    public static int SOME_ID = 0x01;
    
    // This is not kept
    public void doIt() {
        ...
    }
    
    // This is not kept
    private String mSomeValue;
    
    // This is kept
    String void doSomethingElse() {
        ...
    }
}
```

You can keep specific methods or fields with the `@KeepMethod` and `@KeepField` annotations:

```java
@KeepField
public static int SOME_ID = 0x01;
    
@KeepMethod
public void doIt() {
    ...
}
```

But this does not automatically keep the containing class as well! If you want to keep the name of the containing class too you can use the `@KeepName` annotation:

```java
@KeepName
public class ExampleClass {
    
    @KeepField
    public static int SOME_ID = 0x01;
    
    @KeepMethod
    public void doIt() {
        ...
    }
    
    // This is not kept
    private String mSomeValue;
    
    // This is also not kept
    String void doSomethingElse() {
        ...
    }
}
```
