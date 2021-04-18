# TraceWeaver

## 背景

在Android项目中有一个很好用的工具叫做systrace，通过它可以在APP的运行过程中收集许多信息，从而为调试与优化提供帮助。

但是，只有使用了`android.os.Trace`的代码才可以受到监控，而如果要修改所有相关的源代码又是不现实的。

## 特性

本插件的作用是，在APK打包之前，在其中所有类的所有函数前后添加对`android.os.Trace#beginSection(String)`和`android.os.Trace#endSection()`的调用，从而在不侵入源码的条件下快速实现对所有Java层代码的监控。

经过这样的改动，就可以用atrace和systrace，在APP运行过程中采集到大量的信息。

## 接入Gradle

首先需要先接入以下插件：

``` groovy
apply plugin: 'SweetChips-android'
```

这个插件的相关文档：

- [SweetChips-android](../gradle-android/README.md)

然后在项目根目录的build.gradle中添加以下依赖：

``` groovy
classpath "org.sweetchips:plugin-trace-weaver:$version_sweetchips"
```

然后添加以下配置项：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'TraceWeaver'
TraceWeaver {
    attach 'foobar'
    ignore 'foo.*'
    ignore 'bar.Bar'
    notice 'foo.Foo'
    notice 'bar.Bar#test'
    ignore '#TAG'
    maxDepth 10
    sectionName { classInfo, methodInfo ->
        classInfo.name.replaceAll('/', '.') + '#' + methodInfo.name
    }
}
```

`attach`是必要选项且只能设定一次，在以上示例中，经过`attach`可以将本插件绑定到`SweetChips`的`foobar`流程中。

`ignore`和`notice`是可选项且可以设定多次，在以上示例中，`foo`包下除`foo.Foo`类外其余所有类的所有成员都会被忽略，`bar.Bar`类中除`bar.Bar#test`外的其余所有成员都会被忽略，以及所有类的`TAG`成员都会被忽略。

通过`sectionName`可以自定义每段函数的tag，这是一个可选项，以上示例是默认的实现方式。但是tag的长度不能够超过`127`，这是由于`android.os.Trace#MAX_SECTION_NAME_LEN`所施加的限制，若超出范围则会抛出`java.lang.IllegalArgumentException`。在本插件下不必担心crash，若超过`127`则将截取最后的`127`个字符。

`maxDepth`是可选项，其默认值为`Integer.MAX_VALUE`，它代表采集到trace的最大深度。

相关示例：

- Android项目[示例](../demo-app/config/plugin.gradle)