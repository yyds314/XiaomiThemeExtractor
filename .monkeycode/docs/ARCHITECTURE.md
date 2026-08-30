# 系统架构

Global Font Manager 第一阶段采用 Kotlin、Jetpack Compose、Material 3 与 MVVM。

## 分层

- `data`：字体文件模型与扫描数据源。
- `domain`：字体观察与刷新用例、仓储契约。
- `repository`：字体数据的默认实现。
- `ui`：Compose 页面、主题、导航与 ViewModel。
- `service`：Root 操作服务边界。
- `utils`：权限检测与依赖组装。

系统字体替换和 Root 命令执行保持在后续阶段实现。

第五阶段的 `SystemFontAnalyzer` 读取设备实际字体目录、系统属性和字体 XML 配置，向 Root 引擎提供动态替换目标。模块生成器按真实目标路径写入 system overlay 层，并同步配置文件映射。

第六阶段增加 Gradle 8.7 Wrapper、KSP Room 代码生成、RootEnvironmentChecker、GlobalExceptionHandler、DiagnosticReportExporter 和 AppSettingsRepository。RootEnvironmentChecker 读取 Root provider、模块目录权限和 SELinux 状态；诊断导出聚合设备、ROM、字体分析、模块状态、Root 日志和 `crash.log`。高级设置使用应用私有 SharedPreferences 持久化。
