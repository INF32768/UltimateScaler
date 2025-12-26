# Ultimate Scaler

[English](../README.md) | **简体中文**

[![CI Status](https://ci.appveyor.com/api/projects/status/dsti38xjw0jknojx?svg=true)](https://ci.appveyor.com/project/INF32768/ultimatescaler)
![GitHub Release](https://img.shields.io/github/v/release/INF32768/UltimateScaler)
![GitHub License](https://img.shields.io/github/license/INF32768/UltimateScaler)

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1323296?style=for-the-badge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/ultimate-scaler)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/ktrA4Qtm?style=for-the-badge&logo=modrinth)](https://www.modrinth.com/mod/ultimate-scaler)

**一个用于突破 Minecraft 地形生成边界，对地形生成（尤其是边境之地等距离现象）进行深度研究与探索的 Fabric 模组**

注：此文档主要面向开发者和高级用户，对于想要体验内容的普通玩家，请点击上面的徽章进入 CurseForge 或 Modrinth 页面，那里有详细的安装说明、截图和用户讨论。

> **🛠️ 开发状态：重构进行时**
>
> 我们正在对代码库与工作流进行现代化重构，目标包括：**语义化版本控制**、**自动化 CI/CD 流水线**、**规范化提交历史**与**完善的贡献者指南**。[点击此处](https://github.com/INF32768/UltimateScaler/wiki/Project_Refactoring_zh)前往项目Wiki了解更多详情。

## 技术概览

本模组通过一系列对 Minecraft 地形生成系统的底层修改，实现了以下核心能力：

- **🧬 地形生成偏移**：通过拦截并修改噪声采样坐标，在大尺度上稳定地偏移地形。目前已经能够偏移绝大多数地形生成。
- **🔧 距离现象修复**：修复某些距离现象，如“末地环”、废弃矿井无法生成等，甚至是修复边境之地本身。
- **🌐 解除软性限制**：解除各种人为设定的限制，如世界边界、命令中坐标值和数据包中某些数值的限制等。
- **🔬 更多修改功能**：数不胜数的杂项修改功能，比如修改噪声采样算法中的数值，或是复现经典的“天空网格”等。

几乎所有功能都是可选的，可以根据自己的需求选择开启或关闭。

## 开始开发

**环境配置：**
- **JDK 21+**
- **Fabric Toolchain**
- **依赖**: Fabric API (运行时), Cloth Config API (可选，用于GUI)

**快速构建：**
```bash
git clone https://github.com/INF32768/UltimateScaler.git

cd UltimateScaler

./gradlew build
```

随后，在 `build/libs` 目录下找到 jar 文件（选择文件名中不带有`source`的），将其放入 `.minecraft/mods` 目录即可。

**配置文件：**`.minecraft/config/ultimate_scaler.toml`（可在游戏内使用快捷键打开配置GUI，默认`Ctrl+U`，若手动修改配置文件，在游戏内执行`/reload`命令即可生效）。

**贡献：** 我们欢迎包括问题报告、功能建议和代码提交在内的所有贡献。在开始前，请阅读 [贡献指南](CONTRIBUTING.md)。

## 每日构建版

[![Build status](https://ci.appveyor.com/api/projects/status/gf3ma7wu8cd6uf23?svg=true)](https://ci.appveyor.com/project/INF32768/ultimatescaler-nightly)

想要体验或测试最新的代码变动？如果某天有代码变动，我们会在当晚或次日发布一个每日构建版。

1.  **访问工作流页面**：点击上面的徽章进入 AppVeyor 页面。
2.  **选择最新成功的构建**。
3.  在构建详情页的 **“Artifacts” (制品)** 标签页下载文件。

这些构建包含了最新的特性与修复，但也可能包含不稳定的更改，**请勿用于生产环境**。

## 兼容性说明

- **支持版本**：![CurseForge Game Versions](https://img.shields.io/curseforge/game-versions/1323296)
- **兼容性**：
  - ✅ 与基于数据包的地形生成模组兼容，偏移算法对其生效（如 _The Aether_）。
  - ⚠️ 与注入式修改地形生成的模组部分兼容，偏移算法通常不生效，但可共存（如 _Modern Beta_）。
  - ❌ 与侵入式修改地形生成的模组不兼容，同时装载会导致游戏崩溃或各种奇怪的问题（如 _Concurrent Chunk Management Engine_）。

## 更新计划

更新计划已搬迁至项目 Wiki，请点击[此处](https://github.com/INF32768/UltimateScaler/wiki/Project_Roadmap_zh)查看。

## 许可证

本项目依据 MIT 许可证开源。详见 [LICENSE](../LICENSE) 文件。

本项目内置了 toml4j 库，其许可证为 MIT，详见 [LICENSE-toml4j](../third-party-licenses/LICENSE-toml4j)

---

## 附录：技术细节

*此部分包含详细的技术规范，内容将在项目重构稳定后迁移至项目Wiki。*

### A1. 当前支持的密度函数偏移列表
本模组目前通过坐标变换，支持对以下核心密度函数进行偏移：
- `old_blended_noise`
- `noise`
- `shifted_noise`
- `shift_A`
- `shift_B`
- `shift`
- `weird_scaled_sampler`
- `y_clamped_gradient`（可选）
- `end_island`（需开启 BigInteger 重写）