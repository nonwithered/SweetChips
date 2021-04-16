# SourceLineEraser

## 背景

在JVM字节码中，class文件里通常会记录其对应的源码文件以及行号等信息，这些信息可以方便开发者debug，但是实际上即使将它们完全删除，程序仍旧可以不受影响地正常运行。

## 特性

本插件的作用就是可以将这些信息删除，从而缩小编译后的代码文件。

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
classpath "org.sweetchips:plugin-sourceline-eraser:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'SourceLineEraser'
SourceLineEraser {
    attach 'foobar'
    ignore 'foo.*'
    ignore 'bar.Bar'
    notice 'foo.Foo'
    notice 'bar.Bar#test'
}
```

`attach`是必要选项且只能设定一次，在以上示例中，经过`attach`可以将本插件绑定到`SweetChips`的`foobar`流程中。

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略。

相关示例：

- Java项目[示例](../demo-main/config/plugin.gradle)
- Kotlin项目[示例](../demo-mainkt/config/plugin.gradle)

## 接入Maven

在pom.xml中添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-sourceline-eraser-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>sourcelineeraser</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <ignores>
            <ignore>foo.*</ignore>
            <ignore>bar.Bar</ignore>
        </ignores>
        <notices>
            <notice>foo.Foo</notice>
            <notice>bar.Bar#test</notice>
        </notices>
    </configuration>
</plugin>
```

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略。

相关示例：

- Java项目[示例](../demo-main/pom.xml)
- Kotlin项目[示例](../demo-mainkt/pom.xml)
