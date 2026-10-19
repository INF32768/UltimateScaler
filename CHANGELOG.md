**English** | [简体中文](CHANGELOG.zh.md)

## 0.4.0 — 2026-10-19

### Project Refactoring Complete

This release is about rebuilding the foundation rather than adding new features or gameplay. After roughly eleven months of work, Ultimate Scaler has grown from a personal codebase into a documented, automated and collaborative project: the commit workflow, CI/CD, documentation, build chain, code structure and config system were all replaced. In-game behavior stays compatible, and existing configs migrate automatically — no manual re-entry required.

### Configuration

- The config model is now V4, organised into General, World Generation, Fixes & Expansion, and Utilities, with field names that better match their behavior.
- V2/V3 configs migrate automatically to V4; migration results are written back immediately, and the V2 file is deleted only after the new config is saved.
- The config screen was rebuilt with five categories, unified environment/risk/performance/restart labels, a distance-effect legend, dependent sub-options, decimal validation, block dropdowns, and clickable Wiki/Issue links.
- The old config screen remains as a transition and is marked deprecated; hold Shift while clicking the mod icon in Mod Menu to reach it.
- The keybind now opens the new screen directly. Config loads at startup, a default file is generated on first launch, and `/reload` re-reads the file.
- Per-option comments are no longer written to the config file; the header links to the Wiki, where the detailed documentation lives.

### Engineering & architecture

- Migrated to mojmap and the modern Fabric template, renamed the package to `xyz.inf32768.ultimatescaler`, and split client/common sources.
- Unified config naming, added `ModMetadata` for shared constants, and switched version checks to Fabric's built-in `Version`.
- Upgraded to Gradle 9.5.1, Loom 1.17, Fabric Loader 0.19.3 and Fabric API 0.136.1; toml4j is now a regular dependency instead of vendored source.
- Added Javadocs, cleaned up utilities and mixins, and replaced fragile `@ModifyConstant` injections to improve cross-version compatibility.

### CI/CD & documentation

- AppVeyor now provides nightly and release pipelines; tagging a release builds, packages and publishes automatically.
- Rewrote the README, added CONTRIBUTING, a contributors list and bilingual issue templates, and expanded the Wiki.
- Adopted Conventional Commits, Trunk-Based Development and atomic commits. Automated smoke tests are deferred; build verification is in place.

### Fixes & improvements

- Rewrote `locate pos`: all parameters are now `double`, negative values are supported, manual range selection is gone, and calculations are dramatically faster.
- Cached debug HUD coordinate conversions to avoid repeated work.
- Fixed incorrect End island offsets in certain configs, `rarity` calculation in `WeirdScaledSampler`, and a wrong mixin target for mineshafts.
- Fixed several 1.21.x compile/runtime issues, including a fluid sampler crash and mixin loading conditions.

### Upgrade notes

- Existing configs migrate to V4 automatically; back up custom configs before upgrading.
- The old config screen is deprecated and will be removed in a future release.

Normal feature development resumes from here; automated tests and removal of the legacy config screen are planned as follow-up work.