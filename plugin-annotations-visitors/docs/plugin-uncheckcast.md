# 移除部分冗余的强制类型转换

注意：本插件几乎没有任何适用场景，最好只把它当作一个示例程序，可用于学习、研究、测试。

## 背景

Java中可以对引用类型的强制类型转换操作，其原理是使用`checkcast`指令对操作数栈顶进行类型检查，若类型不匹配则会抛出`java.lang.ClassCastException`。

`checkcast`这个指令虽然在类型匹配的情况下不会对运行过程产生任何影响，但是实际上它仍然是十分必要的，因为在类加载的过程中要进行一些检验工作，如果因为缺少必要的`checkcast`指令，则会抛出`java.lang.VerifyError`。

但是这并不意味着，所有的`checkcast`指令都是不可省略的，比如下面这个例子：

``` java
class Main {
    public static void main(String[] args) {
        Object obj = new Main();
        ((Main) obj).test();
    }
    private void test() {
    }
}
```

在上面的例子中的强制转换是完全没有意义的，但是编译产物中仍然会存在`checkcast`指令。

本插件的作用就是可以有选择地移除不必要的`checkcast`指令。

## 特性

``` java
import org.sweetchips.annotations.Uncheckcast;
@Uncheckcast({String.class})
class Foobar {
    void test() {
        // ...
    }
    @Uncheckcast({Integer.class, Double.class})
    void foo() {
        // ...
    }
    @Uncheckcast
    void bar() {
        // ...
    }
}
```

对于以上程序，在插件生效后所编译出的字节码中，`Foobar#test()`中对`String`的强制转换、`Foobar#foo()`中对`String`和`Integer`和`Double`的强制转换、`Foobar#bar()`中对所有类型的的强制转换都会被忽略。

`org.sweetchips.annotations.Uncheckcast`的目标可以是类和方法，若不提供实参则忽略所有强制转换。

## 接入Gradle

首先在build.gradle中添加以下依赖：

``` groovy
compileOnly "org.sweetchips:annotations:$version_sweetchips"
```

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Uncheckcast`这个注解。

然后需要先接入以下两插件之一：

``` groovy
apply plugin: 'SweetChips-android'
// apply plugin: 'SweetChips-java'
```

这些插件的相关文档：

- [SweetChips-android](../../gradle-android/README.md)
- [SweetChips-java](../../gradle-java/README.md)

在项目根目录的build.gradle中添加以下依赖：

``` groovy
classpath "org.sweetchips:plugin-annotations-visitors:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
foobar {
    transform {
        first adapt('org.sweetchips.annotationsvisitors.UncheckcastTransformClassNode')
    }
}
```

相关示例：

- Android项目[示例](../../demo-app/config/plugin.gradle)
- Java项目[示例](../../demo-main/config/plugin.gradle)
- Kotlin项目[示例](../../demo-mainkt/config/plugin.gradle)

## 接入Maven

首先在pom.xml中添加以下依赖：

``` xml
<dependency>
    <groupId>org.sweetchips</groupId>
    <artifactId>annotations</artifactId>
    <version>${version_sweetchips}</version>
    <scope>provided</scope>
</dependency>
```

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Uncheckcast`这个注解。

然后添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-annotations-visitors-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>annotationsvisitors</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

相关示例：

- Java项目[示例](../../demo-main/pom.xml)
- Kotlin项目[示例](../../demo-mainkt/pom.xml)
