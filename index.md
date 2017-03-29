* **Short and concise, but powerful API**: Define your rules with a few simple annotations and ProguardAnnotations takes care of the rest!
* **Simplifies development**: You can move classes and refactor your code without worrying about breaking any proguard rules!
* **Works with your current setup**: Don't want to migrate your whole project to try out ProguardAnnotations? You don't have to! While using ProguardAnnotations you can still define rules manually or use another Proguard library. No conflicts, no problems.

## How do I add it to my project?

To use Proguard Annotations add the following at the very top of your build.gradle file:

```groovy
plugins {
    id 'com.github.wrdlbrnft.proguard-annotations' version '0.3.0.11'
}
```

And you are done! Now you can use ProguardAnnotations.

## How do I use it?

ProguardAnnotations currently provides four different annotations with which you can define your Proguard rules:

* `@KeepClass`: Annotate a class, interface or enum with this annotation to keep it.
* `@KeepMember`: Annotate a method or field with this annotation to keep it. Only keeps the annotated member, not the class, interface or enum which contains it.
* `@KeepClassMembers`: Annotate a class, interface or enum with this annotation to keep its members. You can also modify what exactly is kept from the class by specifying the `KeepSetting` on the annotation (explained below).
* `@DontKeep`: Annotate a method or field with this annotation to specifically not keep it.

## Examples

In most cases the `@KeepClass` and `@KeepMember` annotations are all you need! For example:

```java
@KeepClass
public class ExampleClass {
    
    // This field is kept
    @KeepMember
    public static int SOME_ID = 0x01;
    
    // This method is kept too
    @KeepMember
    public void doIt() {
        ...
    }
    
    // This field is not kept
    private String mSomeValue;
    
    // This method is not kept as well
    String void doSomethingElse() {
        ...
    }
}
```

You can use `@KeepClassMembers` together with `@KeepClass` to keep all the members of a class:

```java
@KeepClass
@KeepClassMembers
public interface ExampleInterface {
    void foo();
    void bar();
}
```

If you want don't want to keep a specific method you can use `@DontKeep`:

```java
@KeepClass
@KeepClassMembers
public interface ExampleInterface {
    
    // This method is kept
    void foo();
    
    // This method is not kept
    @DontKeep
    void bar();
}
```

With `KeepSetting` you specify what exactly is kept by `@KeepClassMembers`:

```java
@KeepClass
@KeepClassMembers(KeepSetting.PUBLIC_MEMBERS)
public class ExampleClass {
    
    // This is kept
    public static final int SOME_ID = 0x01;
    
    // This is kept as well
    public void doIt() {
        ...
    }
    
    // This is not kept
    private String mSomeValue;
    
    // This too is not kept
    String void doSomethingElse() {
        ...
    }
}
```

Of course you can specify multiple `KeepSetting` values:

```java
@KeepClass
@KeepClassMembers({KeepSetting.PUBLIC_MEMBERS, KeepSetting.PACKAGE_LOCAL_METHODS})
public class ExampleClass {
    
    // This field is kept
    public static final int SOME_ID = 0x01;
    
    // This method is kept as well
    public void doIt() {
        ...
    }
    
    // This field is not kept
    private String mSomeValue;
    
    // But this method is kept too
    String void doSomethingElse() {
        ...
    }
}
```
