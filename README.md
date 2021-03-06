# SweetChips

本框架为高效便捷地开发各种平台的编译插件而设计，这个项目可以作为相关插件的基础框架。

这里是[版本日志](docs/versions-log.md)，目前最新版本为`0.0.2`。

本框架的目标是为JVM、LLVM等平台下各种项目的插件开发提供帮助，目前已经支持以下三类项目的插件：

- [使用Gradle构建的Android项目](gradle-android/README.md)
- [使用Gradle构建的Java项目、Kotlin项目](gradle-java/README.md)
- 使用Maven构建的Java项目、Kotlin项目

在以上部分的基础上，以下插件已被实现：

- [TraceWeaver](plugin-trace-weaver/README.md)
- [ConstSweeper](plugin-const-sweeper/README.md)
- [SourceLineEraser](plugin-sourceline-eraser/README.md)
- [InlineTailor](plugin-inline-tailor/README.md)
- [InlineTailorPlus](plugin-inline-tailor-plus/README.md)
- [RecursiveTail](plugin-recursive-tail/README.md)
- [AnnotationsVisitors](plugin-annotations-visitors/README.md)

如果您希望以此为基础开发自己的JVM字节码插件，请阅读以下开发指南：

- [JVM字节码插件开发手册](docs/developer-manual-jvm-plugin.md)

如果您希望尝试在自己的项目中使用本项目所实现的插件，那么您可以参考[SweetChips-repo](https://github.com/nonwithered/SweetChips-repo)中的说明，配置好自己项目中的二进制仓库，然后按照各个模块的文档中的指引，实现您自己的需求。

LLVM等其他平台的开发工具尚未完善，敬请期待！
