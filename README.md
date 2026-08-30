# Global Font Manager

Global Font Manager 是一个面向 Xiaomi、Redmi、POCO 设备的 Android Root 字体管理工具。

## 当前版本

- Version 0.6.0 / versionCode 6
- MIUI / HyperOS 动态字体分析与 Root overlay 模块
- TTC 字体集合和 face index 检查
- Root、兼容性、崩溃和诊断报告

## 功能

- Kotlin + Jetpack Compose + Material Design 3
- MVVM 分层架构
- 首页、字体库、设置页面导航
- 深色与浅色模式切换
- 字体扫描、Root 操作与系统字体替换

## 开发环境

- compileSdk 35
- targetSdk 35
- minSdk 29
- JDK 17
- Gradle 8.7
- Android Gradle Plugin 8.6.1
- Kotlin 2.0.21
- KSP 2.0.21-1.0.28

在 Android Studio 中打开工作区根目录即可导入项目。

## 构建

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Release 构建当前使用 Debug keystore 作为测试签名，正式发布需要配置正式签名。
