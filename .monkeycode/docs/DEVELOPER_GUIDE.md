# 开发指南

项目使用 Gradle Kotlin DSL、Kotlin 2.0、JDK 17、compileSdk 35、targetSdk 35 和 minSdk 29。

第三阶段 Root 代码只通过 `RootShellManager` 执行 `su -c`。字体应用流程必须先完成字体解析、系统目标检测、空间检查和备份，再生成并安装 overlay 模块。应用代码不直接写入真实 `/system`、`/product` 或 `/system_ext` 文件。

第六阶段使用 Gradle 8.7 Wrapper、Android Gradle Plugin 8.6.1、Kotlin 2.0.21、Compose compiler plugin 和 KSP 2.0.21-1.0.28。Room Schema 输出到 `app/schemas`，正式发布需要配置 release signing。
