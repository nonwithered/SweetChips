# InlineTailor

## 背景

在Java代码被编译的过程中，几乎不会被处理任何可能的优化，而对于kotlin、C++等语言来说，都具有内联的能力，从而降低部分运行开销，但是Java中并没有这种功能，本插件就试图解决这个问题。

一个增强版的插件可以和这个插件一起使用，从而实现更大范围的内联：

[InlineTailorPlus](../plugin-inline-tailor-plus/README.md)

## 特性

本插件的作用是，可以为部分方法提供在其所在类内的内联能力。比如以下示例：

``` java
class Foobar {
    public static void main(String[] args) {
        new Foobar().foo(123);
    }
    public final int foo(int n) {
        return bar(n, n);
    }
    private int bar(int a, int b) {
        return a + b;
    }
}
```

在上面这个示例中，`Foobar#foo(int)`并不会调用`Foobar#bar(int, int)`，而`Foobar#main(String[])`也不会调用这两个函数，这两个调用点都将彻底被展开。

在本插件中，如果某次调用可以被展开，那么调用者与被调用者必须在同一个类中，并且被调用者必须满足以下条件之一：

- 这个方法是`static`方法
- 这个方法是`private`方法
- 这个方法是`final`方法
- 这个类是`final`类

除此之外，这个方法还必须满足以下所有条件；

- 这个方法不是`synchronized`方法
- 这个方法中没有任何分支
- 这个方法中除参数外没有任何局部变量
- 这个方法不会对任何参数做写操作

除了以上所有条件之外，还有最后一个要求，对于包含`this`在内的所有参数，其访问时机必须可以限制在栈顶。

假如以上所有条件都得到了满足，那么这个函数就可以被本插件展开。

## 接入Gradle

首先需要先接入以下插件之一：

``` groovy
apply plugin: 'SweetChips-android'
// apply plugin: 'SweetChips-java'
```

这些插件的相关文档：

- [SweetChips-android](../gradle-android/README.md)
- [SweetChips-java](../gradle-java/README.md)

然后在项目根目录的build.gradle中添加以下依赖：

``` groovy
classpath "org.sweetchips:plugin-inline-tailor:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'InlineTailor'
InlineTailor {
    attach 'foobar'
    ignore 'foo.*'
    ignore 'bar.Bar'
    notice 'foo.Foo'
    notice 'bar.Bar#test'
    ignore '#foobar'
}
```

`attach`是必要选项且只能设定一次，在以上示例中，经过`attach`可以将本插件绑定到`SweetChips`的`foobar`流程中。

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略, 以及所有类的`foobar`成员都会被忽略。

相关示例：

- Android项目[示例](../demo-app/config/plugin.gradle)
- Java项目[示例](../demo-main/config/plugin.gradle)

## 接入Maven

在pom.xml中添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-inline-tailor-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>inlinetailor</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <ignores>
            <ignore>foo.*</ignore>
            <ignore>bar.Bar</ignore>
            <ignore>#foobar</ignore>
        </ignores>
        <notices>
            <notice>foo.Foo</notice>
            <notice>bar.Bar#test</notice>
        </notices>
    </configuration>
</plugin>
```

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略, 以及所有类的`foobar`成员都会被忽略。

相关示例：

- Java项目[示例](../demo-main/pom.xml)
