# Contribution Guidelines

**English** | [简体中文](CONTRIBUTING_CN.md)

## 🎉 Welcome!

First of all, thank you very much for using **Ultimate Scaler** and for your interest in this project! The growth of open source projects is inseparable from everyone's contributions, and every participation you make makes this project one step closer to the "ultimate".

> **🙏 About Sponsorship**
>
> We are currently **not accepting financial sponsorships of any kind**. If you wish to support us, the best way to do so is to **use it, share it, contribute code or ideas to it**. Your intellectual contribution is far more valuable than money.

## 🌟 Our Contributors

We firmly believe that everyone who contributes to the project deserves to be remembered. As such, we maintain a **[list of contributors](./CONTRIBUTORS.md)**.

**You can get on this list in any of the following ways:**
- Submit a valid **issue** (for bug reports, it is valid if it is not marked as `invalid` or `duplicate` after review; For suggestions, they do so only if they are marked as `enhancement` after review).
- Submitted **Pull Requests** are merged.
- Provided **critical help or advice** through social media such as GitHub Discussions (as determined by the maintainer).
- Provides **important non-code contributions** such as documentation, translation, design, etc.

We believe that the strength of community comes from every small contribution. This list is our sincerest thanks to all those who helped.

## 🪲 Report a problem / 💡 Give feedback

No matter if you're reporting a bug or proposing a new feature, we welcome **submitting an Issue**! To ensure that we can respond efficiently, we've prepared two pre-filled templates:

*   **[Report a bug](https://github.com/INF32768/UltimateScaler/issues/new?template=bug_report_zh.yml)** for reproducible bugs.
*   **[Provide feedback](https://github.com/INF32768/UltimateScaler/issues/new?template=suggestion_zh.yml)** for new features or improvements.

Before submitting, [search existing Issues](https://github.com/INF32768/UltimateScaler/issues) and [read the Wiki](https://github.com/INF32768/UltimateScaler/wiki) might help you find the answer quickly.

## 🧑‍💻 Contribute Code

Code contributions are warmly welcome! Here's a quick guide to getting started:

### Development Environment
1. **Fork** This repository.
2. Clone Your Fork:
    ```bash
    git clone https://github.com/<Your GitHub Username>/UltimateScaler.git
    ```
3. Configure the development environment as described in the [README](../README.md#start-development).
4. Create a new feature branch:
   ```bash
   git checkout -b feat/your-feature-name
   ```

### Commit specifications
We use the **Conventional Commits** specification to keep the commit history clear.
- `feat:` new feature
- `fix:` bug fix
- `docs:` only for documentation changes
- `style:` formatting changes that don't affect the meaning of the code (indentation, space, etc.)
- `refactor:` code optimizations that neither fix bugs nor add functionality
- `perf:` code changes that improve performance
- `test:` to add or modify the test
- `chore:` changes to the build process or auxiliary tools

**Example:**
```bash
git commit -m "feat: add support for offsetting structure generation"
git commit -m "fix(core): prevent crash when noise sampler is null"
```

### Code Style
- Follow existing code formatting (we mainly follow Java common conventions).
- Write clear Javadoc comments for new public classes and methods.
- Make sure your changes pass the existing test (`./gradlew test`).

### Initiate a Pull Request
1. Push your branch to your fork:
    ```bash
    git push origin feat/your-feature-name
    ```
2. Initiate a pull request on GitHub to the `main` branch of this repository.
3. In the PR description, clearly state the content of your changes, your motivations, and link any relevant issues.
4. Wait for code review. We may request some changes or discussions.

## 📜 License

By contributing code, you agree that your contribution will be licensed under the [MIT LICENSE](../LICENSE) of this project.

---

Thank you again for considering contributing to Ultimate Scaler! Every line of your code, every idea, is the driving force that drives this project forward.