# Ultimate Scaler

<!-- modrinth_exclude.start -->
**English** | [简体中文](README_CN.md)
<!-- modrinth_exclude.end -->

[![](https://s21.ax1x.com/2025/05/28/pVpUmYq.jpg)]()

[![Build status](https://ci.appveyor.com/api/projects/status/dsti38xjw0jknojx?svg=true)](https://ci.appveyor.com/project/INF32768/ultimatescaler)
![GitHub Release](https://img.shields.io/github/v/release/INF32768/UltimateScaler)
![GitHub License](https://img.shields.io/github/license/INF32768/UltimateScaler)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/INF32768/UltimateScaler)](https://github.com/INF32768/ultimatescaler)
![CurseForge Game Versions](https://img.shields.io/curseforge/game-versions/1323296)

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1323296?style=for-the-badge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/ultimate-scaler)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/ktrA4Qtm?style=for-the-badge&logo=modrinth)](https://www.modrinth.com/mod/ultimate-scaler)

## Introduction

Ultimate Scaler is a Minecraft Fabric Mod that provides some modification functions related to terrain generation, mainly for exploring the Far lands and various distance effects related to terrain generation.

This Mod is still in development, and new features are being added frequently. There may be some bugs.

**Warning: The results of the modification may not be 100% in line with theoretical actual.**

**Warning: Please back up your save before using this Mod, and the author is not responsible for any damage or loss to your save caused by using this Mod!**

<!-- modrinth_exclude.start -->
## Dependencies

- [Fabric API](https://github.com/FabricMC/fabric): Provides Mod loading, configuration, and command functions.
    - Even if Fabric API is not installed, this Mod can still run normally, but some important functions may not be available.
- (Optional) [Cloth Config](https://github.com/shedaniel/cloth-config):Provides a configuration interface.
<!-- modrinth_exclude.end -->

## Features

- Offset terrain generation: In a wide range, offset most of the terrain generation, and currently support the following content:
  - Density function:
    - `old_blended_noise`,
    - `noise`,
    - `shifted_noise`,
    - `shift_A`,
    - `shift_B`,
    - `shift`,
    - `weird_scaled_sampler`,
    - `y_clamped_gradient`(optional).
  - Sea level and underground lava layer(optional).
  - If the "BigInteger rewrite"(_Experimental_) option is enabled, the `end_islands` density function can be offset, and other content can be offset accurately.
- Extend the world border.
- Modify the `maintainPrecision` method, which implements the modification of the position of the far lands, and other functions such as removing the fringe lands;
- The `locate pos` command, which can be used to locate the coordinates of a specific position after offset.
- Fix various bugs in the original terrain generation algorithms.

## Configuration

If `Cloth Config` is installed, the option screen can be opened through *Mod Menu* or by pressing the binding key (default is `Ctrl + U`).

The configuration file is located in `config/ultimate_scaler.toml`. After modifying the file, execute the `/reload` command to take effect.

## Compatibility

### Minecraft Version

This Mod currently supports Minecraft 1.21 - 1.21.9 (25w33a).

### Other Mods

- This Mod does not interfere with most mods unrelated to terrain generation.
- This Mod is compatible with most mods that add custom terrain generation through data packs, or modify the vanilla terrain generation, and can offset the generated terrain, such as _The Aether_.
    - Thanks to the powerful capabilities of data packs, most mods use data packs to add custom terrain generation.
- This Mod is compatible, but does not interfere with most mods that add terrain generation through other methods other than data packs, such as _Modern Beta_.
- This Mod is not compatible with mods that make large-scale modifications to the vanilla terrain generation, and may cause the game to fail to start, such as _Concurrent Chunk Management Engine_.

## Update plan

### Short-term plan

- [ ] Implement simple communication between the client and the server.
- [ ] Disable specified noise: Make specified noise always return 0 when sampling.
- [ ] Port to 1.18.2 ~ 1.19.4.
- [ ] New command: `noiseinfo`, which displays information related to a noise (such as value range, frequency, overflow position) when given a name or definition.
- [ ] Change the display of large values in the debugging screen to scientific notation.
- [x] Add comments to the configuration file.
- [x] Compatible with the server.
- [x] Offset the generation of the end island.
- [x] Offset the density function `weird_scaled_sampler`.
- [x] Rewrite the "noise" density function using BigInteger.
- [x] Automatically adjust the display of `TerrainPos` in the debugging screen according to the current offset mode.
- [x] Global fluid replacement: Replace specified fluids during terrain generation to prevent the game from lagging due to too many fluids.
- [x] Port to 1.21 ~ 1.21.1.
- [x] Offset the sea level and the underground lava layer.
- [x] Add usage instructions to the option interface.
- [x] Make the option screen available for Mods running on 1.21 - 1.21.1.
- [x] Port to 1.21.9.

### Middle-term plan

- [ ] Port to 1.18.1 and below versions.
- [ ] Port to Forge loader.
- [ ] Add terrain generation algorithms of different versions of Java Edition, such as 1.18.1, 1.12.2, Beta version, and so on, up to Pre-Classic version.
- [ ] Fix various bugs that occur when the coordinates exceed 33554432, such as crashes, blocks not rendering, and lighting effects abnormalities.
- [ ] Generate sky grids in the fringe lands.
- [ ] Add "Quit without saving" feature.
- [ ] Prevent the game from crashing when chunk generation fails.
- [x] Remove the air walls at 30000000.

### Long-term plan

- [ ] Rewrite the terrain generation algorithm using BigInteger, which can offset more terrain generation, including structures and other features, and the mod will probably release 1.0 version at that time.
- [ ] Add Minecraft Bedrock version of terrain generation algorithm, including old versions.
- [ ] Link with a minimap mod (not determined yet) and draw the noise and density function graphic on the map.
- [ ] Bypass the 32-bit integer limit, 64-bit integer limit, single-precision floating-point number limit, and even double-precision floating-point number limit in the world boundary, and the mod will probably release 2.0 version and change the name at that time.
- [ ] Bypass the Y-axis limit and support the generation of terrain with a height of 2147483647 (possibly higher).

## Special thanks

- Thanks to @SysWOWBrO3([BiliBili](https://space.bilibili.com/482351725)) for providing icons, covers and suggestions for this Mod.

## License

This Mod is under the MIT license <!-- modrinth_exclude.start -->
, and you can find the relevant information in the [LICENSE](LICENSE) file<!-- modrinth_exclude.end -->.

This Mod includes the [toml4j](https://github.com/mwanji/toml4j) library, which is licensed under the MIT license<!-- modrinth_exclude.start -->
, and you can find the relevant information in the [LICENSE-toml4j](third-party-licenses/LICENSE-toml4j) file<!-- modrinth_exclude.end -->.