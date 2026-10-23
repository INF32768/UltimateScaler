**English** | [简体中文](https://github.com/INF32768/UltimateScaler/blob/main/docs/CHANGELOG_CN.md)

Marking the completion of the [project refactor](https://github.com/INF32768/UltimateScaler/wiki/Project.Refactoring.en), this release rebuilds the project's foundation rather than adding gameplay features. In-game behavior remains
compatible, and existing V2/V3 configurations migrate automatically to the new V4 format.

### Added

- A new configuration screen with five categories, unified environment/risk/performance/restart labels, a
  distance-effect legend, dependent sub-options, decimal validation, block dropdowns, and clickable Wiki/Issue links.
- Automatic migration from V2/V3 configurations to the V4 configuration model.
- AppVeyor nightly and release pipelines; tagging a release now builds, packages and publishes automatically.
- Project documentation and community infrastructure: a rewritten README, CONTRIBUTING, a contributors list, bilingual
  Issue templates, expanded Wiki pages, and Javadocs for core code.
- Third-party license files for bundled dependencies.

### Changed

- The configuration model is now V4, organized into General, World Generation, Fixes & Expansion, and Utilities, with
  field names that better match their behavior.
- Configuration migration results are written back immediately; the V2 file is deleted only after the new configuration
  has been saved successfully.
- The keybind now opens the new configuration screen directly. Configuration loads at startup, a default file is
  generated on first launch, and `/reload` re-reads the file.
- The configuration file header now links to the Wiki; detailed documentation lives in the Wiki and in the screen's
  tooltips.
- Migrated to mojmap and the modern Fabric template, renamed the package to `xyz.inf32768.ultimatescaler`, and split
  client/common sources.
- Unified configuration naming, added `ModMetadata` for shared constants, and switched version checks to Fabric's
  built-in `Version`.
- Upgraded to Gradle 9.5.1, Loom 1.17, Fabric Loader 0.19.3 and Fabric API 0.136.1.
- Replaced fragile `@ModifyConstant` injections to improve cross-version compatibility.
- Rewrote `locate pos`: all parameters are now `double`, negative values are supported, manual range selection is gone,
  and calculations are dramatically faster.
- Cached debug HUD coordinate conversions to avoid repeated work.
- Adopted Conventional Commits, trunk-based development, and atomic commits as the project workflow.

### Deprecated

- The old configuration screen remains as a transition and is marked deprecated; hold Shift while clicking the mod icon
  in Mod Menu to reach it. It will be removed in a future release.

### Removed

- Per-option comments are no longer written to the configuration file.
- Manual range selection in `locate pos`.
- The vendored toml4j source; toml4j is now a regular dependency.

### Fixed

- Fixed configuration loading so it no longer replaces the active configuration with defaults when loading or V3
  migration fails. The previous configuration is kept and written back; if there is no previous configuration, defaults
  are used. Before a fallback write, the original file is backed up as `ultimate_scaler.toml.bak`; saves use a temporary
  file with atomic replacement when the filesystem supports it.
- Fixed the `RELEASE` Far Lands position behavior.
- Fixed incorrect End island offsets in certain configurations.
- Fixed `rarity` calculation in `WeirdScaledSampler`.
- Fixed a wrong mixin target for mineshafts.
- Fixed several 1.21.x compile/runtime issues, including a fluid sampler crash and mixin loading conditions.

> **Upgrade note:** Existing V2/V3 configurations migrate to V4 automatically; back up custom configurations before
> upgrading. The old configuration screen is deprecated and will be removed in a future release.

> Automated smoke tests remain a deferred follow-up; build verification is currently in place. Normal feature
> development resumes from this release.
