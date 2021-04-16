# SweetChips-android

这一插件可以作为相关插件的基础插件，要接入本插件，首先要在项目根目录的build.gradle中添加以下依赖：

``` groovy
classpath "org.sweetchips:gradle-android:$version_sweetchips"
```

在正式启动本插件之前，以下两插件之一必须中先在对应模块中被启动：

``` groovy
apply plugin: 'com.android.application'
// apply plugin: 'com.android.library'
```

然后就可以在这个模块的build.gradle中启动本插件：

``` groovy
apply plugin: 'SweetChips-android'
```

当本插件已被启动后，可以用这样的方式进行配置：

``` groovy
SweetChips {
    asmApi = 5 << 16
    addTransform 'foo'
    addTransform 'bar', incremental: true, sameExtra: 'foo'
}
```

`asmApi`是可选项，默认为`Opcodes.ASM5`。

foo和bar都是将要被添加的阶段的名称，经过`addTransform`后将有与之同名的Extension被一同创建。

`incremental`是可选项，默认为`false`。

`sameExtra`是可选项，每个阶段都有一个类型为`Map<Object, Object>`的extra。在上面的示例中，bar的extra将沿用foo的extra，因此可以通过这种方式将foo阶段采集到的信息传递到bar阶段。

当`addTransform`被调用后，就可以配置所创建的阶段：

``` groovy
foo {
    prepare {
        before {
            println 'hello'
        }
        after {
            println 'world'
        }
    }
    transform {
        first "$name_of_class_visitor"
        last adapt("$name_of_class_node")
    }
}
```

每个阶段都有`prepare`和`transform`先后两个部分，可以为它们分别注册多个`before`任务与`after`任务，这些回调函数可以接受一个`Map<Object, Object>`类型的参数，也就是上面提到的`extra`。

在`prepare`和`transform`的`before`和`after`之间，则是`prepare`和`transform`的主要任务。`prepare`会对所有目标文件进行一次完整的扫描，`transform`则会对所有目标文件经过再一次扫描后写入文件系统并作为下一个阶段的输入。

`prepare`和`transform`两一部分中各有一个由`ClassVisitor`组成的任务队列，可以使用`first`或`last`向队首或队尾添加新的任务，这里的参数可以是`ClassVisitor`的类名，如果希望使用`ClassNode`那么可以用`adapt`做转换。

具体用法可以参考demo-app中的[示例](../demo-app/config/plugin.gradle)。
