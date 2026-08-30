# 接口说明

## Root 服务

`RootOperationService` 提供 Root 环境检测、系统字体目标检测、模块安装、模块禁用、模块删除和模块状态查询。

## 数据库

`FontEntity` 保存字体元数据，`FontBackupEntity` 保存原始系统字体备份信息。`MIGRATION_1_2` 为已有第一阶段数据库创建备份表。

## 关键服务

- `RootShellManager`：通过 `su -c` 执行受控命令。
- `BackupManager`：复制、记录和校验系统字体备份；恢复通过移除 overlay 完成。
- `FontModuleGenerator`：生成 overlay 模块和恢复模块。
- `FontReplacementEngine`：编排完整应用事务。
- `FontModuleExporter`：生成并验证可安装的模块 ZIP。
- `ModuleInstaller`：生成 Magisk、KernelSU、APatch 安装 Intent，并提供分享和打开文件 Intent。
- `RootOperationLogger`：记录 Root 命令和真实返回结果。
- `RootEnvironmentChecker`：检查 Root provider、模块目录权限和 SELinux 状态。
- `GlobalExceptionHandler`：将未捕获异常保存到应用私有 `crash.log`。
- `DiagnosticReportExporter`：导出设备、ROM、Root、字体分析、模块、操作日志和崩溃日志。
- `AppSettingsRepository`：持久化自动备份、字体检测、重启提醒、日志保存和模块路径设置。
