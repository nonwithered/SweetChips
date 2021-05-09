# RecursiveTail

## 背景

javac在编译的过程中几乎不会做任何优化，比方说尾递归消除这种优化就不会被编译器处理。

## 特性

本插件的作用就是，可以对部分函数做尾递归消除。比如以下示例：

``` java
final class Foobar {
    int foobar(int n) {
        if (n == 0) {
            return n;
        } else if (n > 0) {
            return foobar(n - 1);
        } else {
            return foobar(n + 1) + 1;
        }
    }
}
```

在以上示例中，第二个分支中的递归就会被消除。

不过，在本插件中，允许被优化的方法，必须满足以下条件之一：

- 这个方法是`static`方法
- 这个方法是`private`方法
- 这个方法是`final`方法
- 这个方法所在的类是`final`类

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
classpath "org.sweetchips:plugin-recursive-tail:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'RecursiveTail'
RecursiveTail {
    attach 'foobar'
    ignore 'foo.*'
    ignore 'bar.Bar'
    notice 'foo.Foo'
    notice 'bar.Bar#test'
    ignore '#TAG'
}
```

`attach`是必要选项且只能设定一次，在以上示例中，经过`attach`可以将本插件绑定到`SweetChips`的`foobar`工作流中。

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略，以及所有类的`TAG`成员都会被忽略。

相关示例：

- Android项目[示例](../demo-app/config/plugin.gradle)
- Java项目[示例](../demo-main/config/plugin.gradle)
- Kotlin项目[示例](../demo-mainkt/config/plugin.gradle)

## 接入Maven

在pom.xml中添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-recursive-tail-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>recursivetail</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <ignores>
            <ignore>foo.*</ignore>
            <ignore>bar.Bar</ignore>
            <ignore>#TAG</ignore>
        </ignores>
        <notices>
            <notice>foo.Foo</notice>
            <notice>bar.Bar#test</notice>
        </notices>
    </configuration>
</plugin>
```

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略，以及所有类的`TAG`成员都会被忽略。

相关示例：

- Java项目[示例](../demo-main/pom.xml)
- Kotlin项目[示例](../demo-mainkt/pom.xml)
