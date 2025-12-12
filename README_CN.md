# Ultimate Scaler

[English](README.md) | **简体中文**

[![](https://s21.ax1x.com/2025/05/28/pVpUmYq.jpg)]()

[![Build status](https://ci.appveyor.com/api/projects/status/dsti38xjw0jknojx?svg=true)](https://ci.appveyor.com/project/INF32768/ultimatescaler)
![GitHub Release](https://img.shields.io/github/v/release/INF32768/UltimateScaler)
![GitHub License](https://img.shields.io/github/license/INF32768/UltimateScaler)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/INF32768/UltimateScaler)](https://github.com/INF32768/ultimatescaler)
![CurseForge Game Versions](https://img.shields.io/curseforge/game-versions/1323296)

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1323296?style=for-the-badge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/ultimate-scaler)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/ktrA4Qtm?style=for-the-badge&logo=modrinth)](https://www.modrinth.com/mod/ultimate-scaler)

## 介绍

Ultimate Scaler是一个Minecraft Fabric Mod，它提供了一些有关地形生成的修改功能，主要用于探索边境之地和各种与地形生成有关的距离现象。

此Mod仍处于开发阶段，功能仍在不断增加中，且可能存在一些Bug。

**警告：修改的结果不一定100%符合理论实际。**

**警告：请在使用前备份存档，若因使用本Mod而造成存档破坏或损失，作者概不负责！**

## 前置

- [Fabric API](https://github.com/FabricMC/fabric)：用于提供Mod加载、配置、命令等功能。
  - 即使不安装Fabric API，本Mod也能正常运行，但可能无法使用部分重要功能。
- （可选）[Cloth Config](https://github.com/shedaniel/cloth-config)：用于提供配置界面。

## 功能

- 偏移地形生成：在大范围内偏移大多数地形生成，目前支持偏移的内容有：
  - 密度函数：
    - `old_blended_noise`,
    - `noise`,
    - `shifted_noise`,
    - `shift_A`,
    - `shift_B`,
    - `shift`,
    - `weird_scaled_sampler`,
    - `y_clamped_gradient`（可选）.
  - 海平面和地底熔岩层（可选）。
  - 若开启“BigInteger重写”（_实验性_），则可以额外偏移密度函数`end_islands`，并实现精准偏移。
- 扩展世界边界；
- 修改 `maintainPrecision` 方法，实现更改边境之地生成的位置、移除边缘之地等功能；
- `locate pos` 命令，用于定位一个特定位置偏移后的坐标；
- 修复各种原版地形生成算法的Bug；

## 配置

在安装了*Cloth Config*的情况下，配置界面可以通过*Mod Menu*，或是按下快捷键（默认是 `Ctrl + U`）来打开，否则需要通过配置文件来进行修改。

配置文件位于 `config/ultimate_scaler.toml` 中，修改后执行 `/reload` 命令使配置生效。

## 兼容性

### Minecraft版本

本Mod目前支持Minecraft 1.21 - 1.21.9（25w33a）。

### 其他Mod

- 本Mod与大多数无关地形生成的mod互不影响。
- 本Mod兼容几乎所有通过数据包新增自定义地形生成，或是微调原版地形生成的mod，且可以偏移其生成的地形，如《The Aether》；
    - 得益于数据包的强大能力，大多数mod都使用了数据包来新增自定义地形生成。
- 本Mod兼容，但不影响大多数通过数据包以外的其他方法新增地形生成的mod，如《Modern Beta》；
- 本Mod不兼容对原版生成器进行了大量侵入式修改的mod，同时安装通常会使游戏无法启动，如《Concurrent Chunk Management Engine》。

## 更新计划

### 短期计划

- [ ] 向上移植到1.21.9；
- [ ] 实现客户端和服务端的简单通信；
- [ ] 禁用指定噪声：让指定噪声在采样时始终返回0；
- [ ] 向下移植到1.18.2 ~ 1.19.3；
- [ ] 新命令：`noiseinfo`，给定一个噪声的名称或定义，显示其相关信息（如值域、频率、溢出位置等）；
- [ ] 将调试屏幕中过大的数值显示为科学计数法；
- [x] 偏移末地岛屿的生成；
- [x] 偏移密度函数 `weird_scaled_sampler`。
- [x] 使用 BigInteger 重写噪声类密度函数；
- [x] 依据当前使用的偏移模式，自动调整调试屏幕中 `TerrainPos` 的显示。
- [x] 兼容服务端；
- [x] 为配置文件添加注释；
- [x] 偏移海平面和地底熔岩层；
- [x] 全局流体替换：在地形生成时替换指定的流体，防止流体过多导致游戏卡顿；
- [x] 向下移植到1.21 ~ 1.21.1；
- [x] 在选项界面中添加使用说明
- [x] 让运行在1.21 - 1.21.1版本的Mod也能使用配置界面；

### 中期计划

- [ ] 向下移植到1.18.1及以下版本；
- [ ] 移植到Forge加载器；
- [ ] 添加Java版不同版本的地形生成算法，如1.18.1，1.12.2，Beta版等，直至Pre-Classic版本；
- [ ] 修复坐标超过33554432时出现的一系列Bug，如崩溃、方块停止渲染、光照效果异常等；
- [ ] 在边缘之地生成天空网格；
- [ ] 添加“不保存并退出”功能；
- [ ] 阻止游戏在区块生成失败时崩溃。
- [x] 移除30000000处的空气墙，破除/tp的坐标限制；

### 长期计划

- [ ] 用BigInteger重写地形生成算法，支持偏移更多的地形生成，包括结构、地物等，预计届时模组将发布1.0版本；
- [ ] 添加基岩版地形生成算法，包括旧版本；
- [ ] 与某个小地图模组（尚未确定）联动，将噪声、密度函数的图像绘制在地图上；
- [ ] 在世界界限方面突破32位整数限制、64位整数限制、单精度浮点数乃至双精度浮点数的限制，预计届时模组将发布2.0版本并更换名称。
- [ ] 在世界界限方面突破Y轴限制，支持生成高度达到2147483647的地形（未来也有可能支持更高的高度）。

## 鸣谢

- 感谢 @SysWOWBrO3([BiliBili](https://space.bilibili.com/482351725)) 为本Mod提供图标、封面和建议。

## 许可证

本Mod遵循MIT许可证，可以在[LICENSE](LICENSE)文件中找到相关信息。

本Mod内置了[toml4j](https://github.com/mwanji/toml4j)库，该库遵循MIT许可证，可以在[LICENSE-toml4j](third-party-licenses/LICENSE-toml4j)中找到相关信息。