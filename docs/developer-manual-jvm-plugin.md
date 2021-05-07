# JVM字节码插件开发手册

## 子插件的核心

每个子插件必须定义一个继承自`org.sweetchips.platform.jvm.BasePluginContext`的子类，例如以下示例：

``` java
// src/main/java/foobar/FoobarContext.java
package foobar;

import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.WorkflowSettings;

public final class FoobarContext extends BasePluginContext {

    @Override
    public final void onAttach(WorkflowSettings settings) {
        settings.addTransformLast((api, cv, ext) -> new ClassNodeAdaptor(api, cv, new FoobarClassNode(api).setContext(this)));
    }

    public static final String NAME = "Foobar";
}

// src/main/java/foobar/FoobarClassNode.java
package foobar;

import org.sweetchips.platform.jvm.BaseClassNode;

public final class FoobarClassNode extends BaseClassNode<FoobarContext> {
    
    public FoobarClassNode(int api) {
        super(api);
    }

    @Override
    protected final void onAccept() {
        // TODO
    }
}
```

`onAttach`方法用于将这个插件所实现的功能添加到某个`Workflow`中，这个函数不应该被手动调用。

`org.sweetchips.platform.jvm.WorkflowSettings`中包含以下方法：

|成员方法|
|:-|
|`void addPrepareBefore(Consumer<Map<Object, Object>> consumer)`|
|`void addPrepareAfter(Consumer<Map<Object, Object>> consumer)`|
|`void addTransformBefore(Consumer<Map<Object, Object>> consumer)`|
|`void addTransformAfter(Consumer<Map<Object, Object>> consumer)`|
|`int getAsmApi()`|
|`void addClass(Supplier<ClassNode> cn)`|
|`void addPrepareFirst(ClassVisitorFactory factory)`|
|`void addPrepareLast(ClassVisitorFactory factory)`|
|`void addTransformFirst(ClassVisitorFactory factory)`|
|`void addTransformLast(ClassVisitorFactory factory)`|

`addPrepareBefore`、`addPrepareAfter`、`addTransformBefore`、`addTransformAfter`，可在四个位置添加回调函数，在回调函数中可以访问一个`Map<Object, Object>`即一个`extra`。

`getAsmApi`可获取被设定的`asmApi`。

`addClass`，可在处理过程中添加新的类，这个方法是线程安全的，可以在`Prepare`阶段的`ClassVisitor`中调用。

`addPrepareFirst`、`addPrepareLast`、`addTransformFirst`、`addTransformLast`，可在Prepare和Transform中添加新的`ClassVisitor`，它们可接受的参数为`org.sweetchips.platform.jvm.ClassVisitorFactory`，这是一个接口，它有这样一个方法`ClassVisitor newInstance(int api, ClassVisitor cv, Map<Object, Object> ext)`，其第三个参数就是`extra`。

在`org.sweetchips.platform.jvm.ClassVisitorFactory`中另有两个静态方法，可以简化代码：

|成员方法|参数说明|
|:-|:-|
|`ClassVisitorFactory fromClassVisitor(Class<? extends ClassVisitor> clazz)`|参数对应的类应实现参数为`(int api, ClassVisitor cv)`或`(int api, ClassVisitor cv, Map<Object, Object> extra)`的构造方法|
|`ClassVisitorFactory fromClassNode(Class<? extends ClassNode> clazz)`|参数对应的类应实现参数为`(int api)`或`(int api, Map<Object, Object> extra)`的构造方法|

从`org.sweetchips.platform.jvm.BasePluginContext`还可以继承到以下方法：

|成员方法|
|:-|
|`ContextLogger getLogger()`|
|`void setLogger(ContextLogger logger)`|
|`boolean isIgnored(String clazz, String member)`|
|`void addIgnore(String name)`|
|`void addNotice(String name)`|

`getLogger`可以用于打印日志，`setLogger`会预先被设置好，您也可以为自己设置新的`ContextLogger`。

`isIgnored`可用于判断某个成员是否已被忽略，`member`为成员名且不区分类型，`clazz`应符合形如`"foobar/Foobar"`的格式，若`member`为`null`则判断某个类是否已被忽略。

`addIgnore`可用于添加`ignore`规则，`addNotice`可用于添加`notice`规则，被`notice`的目标不会被`ignore`。`addIgnore`和`addNotice`的参数应符合以下格式：

|格式|意义|
|:-|:-|
|`"foobar.Foobar"`|名为`foobar.Foobar`的类及其所有成员|
|`"foobar.*"`|名为`foobar`的包及其子包下所有类和所有成员|
|`"#foobar"`|所有类的名为`foobar`的成员|

为便于使用，在框架内已经实现了`org.sweetchips.platform.jvm.BaseClassVisitor`和`org.sweetchips.platform.jvm.BaseClassNode`这两个抽象类，`org.sweetchips.platform.jvm.BaseClassNode`的子类必须实现`onAccept`方法。这两个抽象类的子类都需要一个继承自`org.sweetchips.platform.jvm.BasePluginContext`的类型参数，且可以用`getContext`获得对应实例，可以用`setContext`设置这个实例。

`org.sweetchips.platform.jvm.ClassNodeAdaptor`可以将`ClassNode`转为普通的`ClassVisitor`。

在`Transfrom`阶段的`ClassVisitor`中，可以调用`org.sweetchips.platform.jvm.ClassesSetting#deleteCurrentClass()`从而将当前类删除。

## 移植到Gradle

首先需要添加以下依赖：

``` groovy
compileOnly "org.sweetchips:gradle-common:$version_sweetchips"
```

然后参考以下示例：

``` java
// src/main/java/foobar/gradle/FoobarExtension.java
package foobar.gradle;

import org.sweetchips.gradle.common.AbstractGradleExtension;
import foobar.FoobarContext;

public class FoobarExtension extends AbstractGradleExtension<FoobarContext> {
}

// src/main/java/foobar/gradle/FoobarGradlePlugin.java
package foobar.gradle;

import org.sweetchips.gradle.common.AbstractGradlePlugin;
import foobar.FoobarContext;

public final class FoobarGradlePlugin extends AbstractGradlePlugin<FoobarExtension> {

    @Override
    protected final String getName() {
        return FoobarContext.NAME;
    }

    @Override
    protected final void onApply() {
        // TODO
    }
}

// src/main/resources/META-INF/gradle-plugins/Foobar.properties
implementation-class=foobar.gradle.FoobarGradlePlugin
```

`attach`、`ignore`、`notice`这三个成员方法已经从超类`org.sweetchips.gradle.common.AbstractGradleExtension`中继承而来，可以在build.gradle中直接使用，通过它的成员方法`getContext`可以获得`org.sweetchips.platform.jvm.BasePluginContext`的实例，它的实现类要作为`org.sweetchips.gradle.common.AbstractGradleExtension`的类型参数。

`org.sweetchips.gradle.common.AbstractGradlePlugin`的子类需要继承自`org.sweetchips.gradle.common.AbstractGradleExtension`的类型参数，`getExtension`方法可以获取这个实例，`getProject`可以获取`org.gradle.api.Project`的实例，重写`void onApply()`则可以定义一些初始化操作。`org.sweetchips.gradle.common.AbstractGradlePlugin`的子类必须实现`String getName()`方法，这将作为它的`Extension`的名称。

这个示例可以按以下方式使用：

``` groovy
SweetChips {
    addTransform 'foobar'
}
apply plugin: 'Foobar'
Foobar {
    attach 'foobar'
    ignore 'foo.*'
    ignore 'bar.Bar'
    notice 'foo.Foo'
    notice 'bar.Bar#test'
    ignore '#TAG'
}
```

## 移植到Maven

首先需要添加以下依赖：

``` xml
<dependency>
    <groupId>org.sweetchips</groupId>
    <artifactId>gradle-common</artifactId>
    <version>$version_sweetchips</version>
    <scope>compile</scope>
</dependency>
```

然后修改打包方式：

``` xml
<packaging>maven-plugin</packaging>
```

然后添加以下依赖：

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-plugin-plugin</artifactId>
    <version>${versionMavenPluginPlugin}</version>
</plugin>
```

然后参考以下示例：

``` java
// src/main/java/foobar/maven/FoobarMavenPlugin.java
package foobar.maven;

import java.io.File;
import org.apache.maven.plugin.logging.Log;
import org.sweetchips.maven.java.AbstractMavenPlugin;
import foobar.FoobarContext;

final class FoobarMavenPlugin extends AbstractMavenPlugin<FoobarContext> {

    @Override
    protected final String getName() {
        return FoobarContext.NAME;
    }

    public FoobarMavenPlugin(Log log, int asmApi, File basedir) {
        super(log, asmApi, basedir);
    }
}

// src/main/java/foobar/maven/FoobarMavenMojo.java
package foobar.maven;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Opcodes;

@Mojo(name = "foobar")
public final class FoobarMavenMojo extends AbstractMojo {

    @Parameter(defaultValue = "" + Opcodes.ASM5)
    private int asmApi;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File basedir;

    @Parameter
    private String[] ignores;

    @Parameter
    private String[] notices;

    @Override
    public void execute() {
        FoobarMavenPlugin plugin = new FoobarMavenPlugin(getLog(), asmApi, basedir);
        if (ignores != null) {
            Arrays.stream(ignores).forEach(plugin.getContext()::addIgnore);
        }
        if (notices != null) {
            Arrays.stream(notices).forEach(plugin.getContext()::addNotice);
        }
        plugin.execute();
    }
}
```

`org.sweetchips.maven.java.AbstractMavenPlugin`需要的类型参数就是上面实现的`org.sweetchips.platform.jvm.BasePluginContext`子类，通过`getContext`可以获取对应实例。子类必须实现`String getName()`方法，它将作为临时目录的名称。

`asmApi`的值可以按照需求自行设置合适的值。

`basedir`应设为`target`的上级目录，按示例方式设置即可。

`ignores`和`notices`的设定方式可以直接参考以上示例。

最后在`org.apache.maven.plugin.AbstractMojo#execute()`中调用`org.sweetchips.maven.java.AbstractMavenPlugin#execute()`即可进行字节码转换操作。

这个示例可以按以下方式使用：

``` xml
<plugin>
    <groupId>foobar</groupId>
    <artifactId>foobar</artifactId>
    <version>${versionFoobar}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>foobar</goal>
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
