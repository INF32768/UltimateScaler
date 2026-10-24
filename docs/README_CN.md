# Ultimate Scaler

[English](../README.md) | **简体中文**

![AppVeyor Build](https://img.shields.io/appveyor/build/inf32768/ultimatescaler)
![GitHub Release](https://img.shields.io/github/v/release/INF32768/UltimateScaler)
![GitHub License](https://img.shields.io/github/license/INF32768/UltimateScaler)

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1323296?style=for-the-badge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/ultimate-scaler)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/ktrA4Qtm?style=for-the-badge&logo=modrinth)](https://www.modrinth.com/mod/ultimate-scaler)

**一个用于突破 Minecraft 地形生成边界，对地形生成（尤其是边境之地等距离现象）进行深度研究与探索的 Fabric 模组**

注：此文档主要面向开发者和高级用户，对于想要体验内容的普通玩家，请点击上面的徽章进入 CurseForge 或 Modrinth 页面，那里有详细的安装说明、截图和用户讨论。

> **✅ 开发状态：重构完成**
>
> 项目的大规模重构已经完成，并随 0.4.0 版本发布。本次更新重建了项目基础——包括 V4 配置系统、新版配置界面、现代
> Fabric 工具链，以及重新组织的 CI/CD 与文档流程——没有新增玩法内容。详情请见
> [0.4.0 更新日志](CHANGELOG_CN.md) 与 [重构记录](https://github.com/INF32768/UltimateScaler/wiki/Project.Refactoring.zh)。

> **📢 维护者公告：项目进入半休眠期，预计持续至 2027 年 6 月**
>
> 模组作者 [INF32768](https://github.com/INF32768) 已进入高三，需要备战高考，直至 2027 年 6 月高考结束。在长达约 9 个月的
> 半休眠期内，模组将不再收到内容更新。代理维护者 [冒险家岐哥](https://github.com/SmallmanSeries) 将在此期间代替作者
> 回复 Issues、处理 Pull Requests，并修复重大漏洞。如有问题或需要帮助，请在此期间联系他。

## 技术概览

本模组通过一系列对 Minecraft 地形生成系统的底层修改，实现了以下核心能力：

- **🧬 地形生成偏移**：通过拦截并修改噪声采样坐标，在大尺度上稳定地偏移地形。目前已经能够偏移绝大多数地形生成。
- **🔧 距离现象修复**：修复某些距离现象，如“末地环”、废弃矿井无法生成等，甚至是修复边境之地本身。
- **🌐 解除软性限制**：解除各种人为设定的限制，如世界边界、命令中坐标值和数据包中某些数值的限制等。
- **🔬 更多修改功能**：数不胜数的杂项修改功能，比如修改噪声采样算法中的数值，或是复现经典的“天空网格”等。

几乎所有功能都是可选的，可以根据自己的需求选择开启或关闭。完整的受支持密度函数列表与技术细节，请参阅[世界生成重定位功能详解](https://github.com/INF32768/UltimateScaler/wiki/UserGuide.WorldgenReposition.zh)和[配置全解](https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.zh)。

## 文档与 Wiki

配置参考、距离现象研究资料与项目文档都整理在
[Ultimate Scaler Wiki](https://github.com/INF32768/UltimateScaler/wiki) 中。初次使用可以从
[Wiki 首页](https://github.com/INF32768/UltimateScaler/wiki/Home.zh) 开始；全部配置项说明见
[配置全解](https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.zh)。

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

**配置文件：**`.minecraft/config/ultimate_scaler.toml`。可在游戏内使用默认快捷键（`Ctrl+U`）打开新版配置界面，也可以直接编辑配置文件，然后在游戏内执行 `/reload` 应用修改。全部配置项说明见[配置全解](https://github.com/INF32768/UltimateScaler/wiki/UserGuide.Configuration.zh)。

**贡献：** 我们欢迎包括问题报告、功能建议和代码提交在内的所有贡献。在开始前，请阅读 [贡献指南](CONTRIBUTING.md)。

## 每日构建版

![AppVeyor Build](https://img.shields.io/appveyor/build/inf32768/ultimatescaler-nightly)

想要体验或测试最新的代码变动？如果某天有代码变动，我们会在当晚或次日发布一个每日构建版。

每日构建版的二进制文件部署在一个**统一的 GitHub Release 页面上**，点击[此处](https://github.com/INF32768/UltimateScaler/releases/nightly)并根据说明下载文件即可。

这些构建包含了最新的特性与修复，但也可能包含不稳定的更改，**请勿用于生产环境**。

## 兼容性说明

- **支持版本**：![CurseForge Game Versions](https://img.shields.io/curseforge/game-versions/1323296)
- **兼容性**：
  - ✅ 与基于数据包的地形生成模组兼容，偏移算法对其生效（如 _The Aether_）。
  - ⚠️ 与注入式修改地形生成的模组部分兼容，偏移算法通常不生效，但可共存（如 _Modern Beta_）。
  - ❌ 与侵入式修改地形生成的模组不兼容，同时装载会导致游戏崩溃或各种奇怪的问题（如 _Concurrent Chunk Management Engine_）。

## 更新计划

更新计划已搬迁至项目 Wiki，请点击[此处](https://github.com/INF32768/UltimateScaler/wiki/Project.Roadmap.zh)查看。

## 许可证

本项目依据 MIT 许可证开源。详见 [LICENSE](../LICENSE) 文件。

本项目捆绑了 toml4j 库，其许可证为 MIT，详见 [LICENSE-toml4j](../third-party-licenses/LICENSE-toml4j)。
