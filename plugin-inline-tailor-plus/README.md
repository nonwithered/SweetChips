# InlineTailorPlus

## 背景

在此之前，为了在同一个类中展开被调用的方法，下面这个插件先一步被实现：

- [InlineTailor](../plugin-inline-tailor/README.md)

本插件的作用就是，要实现类与类之间的内联。

应该令InlineTailor在InlineTailorPlus前生效，从而增大影响范围。

## 特性

有以下示例：

``` java
import org.sweetchips.annotations.Inline;
class Main {
    public static void main(String[] args) {
        new Foobar().foobar(2, 3, 4);
    }
}
class Foobar {
    @Inline
    public final int foobar(int a, int b, int c) {
        return a + (c + c);
    }
}
```

在上面的例子中，`Foobar#foobar(int, int, int)`将会在`Main#main(String[])`中被展开。

但是，必须要注意，无论InlineTailor还是InlineTailorPlus，都只是改变方法中的指令，而不会修改任何的访问权限，因此假如上例中`Foobar#foobar(int, int, int)`调用了未被展开的`private`方法，那么就会导致`java.lang.VerifyError`被抛出。

`org.sweetchips.annotations.Inline`的目标只能是函数，被标记过的函数不一定真的会被展开，判断的条件与InlineTailor一致。

## 接入Gradle

首先在build.gradle中添加以下依赖：

``` groovy
compileOnly "org.sweetchips:annotations:$version_sweetchips"
```

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Inline`这个注解。

然后需要接入以下插件之一：

``` groovy
apply plugin: 'SweetChips-android'
// apply plugin: 'SweetChips-java'
```

这些插件的相关文档：

- [SweetChips-android](../gradle-android/README.md)
- [SweetChips-java](../gradle-java/README.md)

然后在项目根目录的build.gradle中添加以下依赖：

``` groovy
classpath "org.sweetchips:plugin-inline-tailor-plus:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    newWorkflow 'foobar'
}
apply plugin: 'InlineTailor-plus'
InlineTailorPlus {
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

加入以上依赖项后，即可开始使用`org.sweetchips.annotations.Inline`这个注解。

然后添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-inline-tailor-plus-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>inlinetailorplus</goal>
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
