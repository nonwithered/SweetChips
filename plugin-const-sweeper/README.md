# ConstSweeper

## 背景

在Java层的代码中的许多常量会在编译期被内联，比如以下示例：

``` java
class Foobar {
    public static final String TAG = "Foobar";
    public static void test() {
        // ...
    }
}
class Main {
    public static void main(String[] args) {
        System.out.println(Foobar.TAG);
        Foobar.test();
    }
}
```

在上面这个示例中，`Foobar#TAG`这个字段在`Main`类被编译的过程中将直接被内联到字节码文件里，因此即使将`Foobar#TAG`这个字段删掉也不会影响实际的运行结果，但是像`Foobar#TAG`这样的字段却仍然会存在于字节码文件中，从而使增大二进制文件增大。

## 特性

本插件可以在不侵入源码的条件下，将冗余的`static final`的基本类型和字符串常量字段移除。在这些常量被引用的位置，如果未被直接内联则会被本插件完成内联。

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
classpath "org.sweetchips:plugin-const-sweeper:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'ConstSweeper'
ConstSweeper {
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

- Android项目[示例](../demo-app/config/plugin.gradle)
- Java项目[示例](../demo-main/config/plugin.gradle)
- Kotlin项目[示例](../demo-mainkt/config/plugin.gradle)

## 接入Maven

在pom.xml中添加以下插件：

``` xml
<plugin>
    <groupId>org.sweetchips</groupId>
    <artifactId>plugin-const-sweeper-mvn</artifactId>
    <version>${version_sweetchips}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>constsweeper</goal>
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
