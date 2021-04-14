# 隐藏部分共有访问权限的符号

## 背景

众所周知，Java中包含四种访问权限，其中只有`public`的符号才可以在另一个包中被访问到，但是实际上不同项目的组织方式有所不同，一个`public`的符号不一定真的希望被公开。

比方说，有一个名为foobar的库，它包含一个名为foobar.Foobar的公有类作为唯一公开的API，但是这个foobar.Fooar也许要用到foo.Foo和bar.Bar，那么foo.Foo和bar.Bar必须也被`public`所修饰，但是这样的话foobar库的用户就有可能会引用这两个不希望被公开的类。

对于Java9及以上的版本，模块系统可以支持这种需求，但是对于Java8及以下的版本，则缺少一种手段将这些`public`的符号隐藏起来。

## 特性

本插件即可以实现这样的功能，比方说某foobar项目中有这两个类：

``` java
package foobar;
import org.sweetchips.annotations.Hide;
@Hide
public class Foo {
}

public class Bar {
    @Hide
    public void temp(String s) {
    }
}
```

本插件将在编译期处理被`Hide`标记过的符号，于是在其他的项目引入foobar库后，对`foobar.Foo`和`foobar.Bar#temp(String)`这两个符号的引用将会无法通过编译，但是对于`foobar`的正常运行则完全没有影响。

`org.sweetchips.annotations.Hide`的目标可以是类、字段、方法。

## 接入Gradle

首先在build.gradle中添加以下依赖：

``` groovy
compileOnly "org.sweetchips:annotations:$version_sweetchips"
```

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Hide`这个注解。

然后需要先接入以下两插件之一：

``` groovy
apply plugin: 'SweetChips-android'
// apply plugin: 'SweetChips-java'
```

这两个插件的相关文档：

- [使用Gradle构建的Android项目](../../gradle-android/README.md)
- [使用Gradle构建的Java项目、Kotlin项目](../../gradle-java/README.md)

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
        first adapt('org.sweetchips.annotationsvisitors.HideTransformClassNode')
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

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Hide`这个注解。

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
