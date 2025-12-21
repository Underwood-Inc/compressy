# Contributing to Compressy

Thank you for your interest in contributing to Compressy! This document provides guidelines and information for contributors.

## 🌟 Ways to Contribute

### 1. Report Bugs
Found a bug? Please [open a bug report](../../issues/new?template=bug_report.yml) with:
- Clear reproduction steps
- Expected vs actual behavior
- Your Minecraft and Compressy version
- Any relevant logs or screenshots

### 2. Suggest Features
Have an idea? [Submit a feature request](../../issues/new?template=feature_request.yml)!
- Explain the problem it solves
- Consider if it fits both FULL and LITE versions
- Think about automation compatibility

### 3. Improve Documentation
Documentation is always welcome!
- Fix typos or unclear explanations
- Add examples or clarifications
- Improve formatting or organization
- Create visual guides

### 4. Submit Code
Ready to code? Read on!

## 🛠️ Development Setup

### Prerequisites
- Java Development Kit (JDK) 21 or higher
- Gradle (included via wrapper)
- Git
- Node.js 20+ and pnpm (for build scripts)
- A text editor or IDE (IntelliJ IDEA recommended)

### Getting Started

1. **Fork the repository**
   ```bash
   # Fork via GitHub UI, then clone your fork
   git clone https://github.com/YOUR_USERNAME/compressy.git
   cd compressy
   ```

2. **Install dependencies**
   ```bash
   pnpm install
   ```

3. **Build the project**
   ```bash
   # Windows
   gradlew.bat build
   
   # Linux/Mac
   ./gradlew build
   ```

4. **Run in development**
   ```bash
   # Generate Minecraft run configurations
   ./gradlew genSources
   
   # Run Minecraft client
   ./gradlew runClient
   ```

## 📁 Project Structure

```
compressy/
├── data/                       # Datapack files
│   ├── compressy/
│   │   ├── function/          # Minecraft functions
│   │   ├── advancement/       # Achievement system
│   │   ├── recipe/           # Crafting recipes
│   │   └── tags/             # Custom tags
│   └── minecraft/
│       └── tags/function/    # Load/tick hooks
├── src/main/java/            # Fabric mod code
│   └── com/compressy/
├── src/main/resources/       # Mod resources
├── scripts/                  # Build/release scripts
├── .github/                  # GitHub templates & workflows
└── build.gradle             # Build configuration
```

## 📦 Build Outputs

The build produces three artifacts:

| Artifact | Location | Description |
|----------|----------|-------------|
| FULL | `build/libs/compressy-{version}.jar` | Full mod with block placement |
| LITE | `build/libs/compressy-lite-{version}.jar` | Inventory-only version |
| Datapack | `build/datapacks/compressy-datapack-{version}.zip` | Standalone datapack |

## 💻 Coding Standards

### Java Code
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods focused on a single responsibility
- Handle errors gracefully

### Minecraft Functions (.mcfunction)
- Use clear, descriptive function names
- Add comments for complex logic
- Keep functions focused and modular

### JSON Files
- Use proper indentation (2 spaces)
- Validate JSON syntax before committing
- Follow Minecraft's data pack format specification

## 🧪 Testing Guidelines

Before submitting a PR, test thoroughly:

### Functional Testing
- [ ] Test in both **singleplayer** and **multiplayer**
- [ ] Test **compression** at various levels (1, 5, 10, 32)
- [ ] Test **decompression** works correctly
- [ ] Test **block placement** (FULL version)
- [ ] Test **block breaking** returns correct items
- [ ] Test **tooltips** display correctly
- [ ] Test **commands** work properly
- [ ] Test with **modded blocks** (if possible)

### Version Testing
- [ ] Test **FULL** version
- [ ] Test **LITE** version
- [ ] Test **standalone datapack** (if applicable)

### Performance Testing
- Monitor for lag with many compressed blocks
- Check memory usage
- Test recipe generation with many mods

### Compatibility Testing
- Test with Fabric API only (minimal setup)
- Test with common storage mods (AE2, RS)
- Test with automation mods (Create, hoppers)

## 📝 Commit Guidelines

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Formatting, missing semicolons, etc.
- `refactor`: Code restructuring without behavior change
- `perf`: Performance improvements
- `test`: Adding tests
- `chore`: Maintenance tasks

### Examples
```
feat(compression): add visual indicator for max compression level

Shows a special particle effect when a block reaches level 32.

Fixes #123
```

```
fix(tooltip): correct block count calculation for level 10+

The tooltip was showing incorrect values for compression
levels above 10 due to integer overflow.

Fixes #456
```

## 🔍 Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feat/my-new-feature
   ```

2. **Make your changes**
   - Write clear, modular code
   - Add comments where needed
   - Update documentation

3. **Test thoroughly**
   - Follow testing guidelines above
   - Fix any bugs you find

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat(scope): add awesome new feature"
   ```

5. **Push to your fork**
   ```bash
   git push origin feat/my-new-feature
   ```

6. **Open a Pull Request**
   - Use the PR template
   - Fill out all relevant sections
   - Link related issues
   - Add screenshots/videos if applicable

7. **Address review feedback**
   - Respond to comments
   - Make requested changes
   - Push updates to the same branch

8. **Merge**
   - Once approved, a maintainer will merge
   - Your changes will be in the next release!

## ⚖️ Design Principles

When contributing, keep these principles in mind:

### 1. **Simplicity**
- Keep the core concept simple: compress any block
- Avoid feature creep
- Standard crafting recipes = maximum compatibility

### 2. **Performance**
- LITE version exists for a reason
- Minimize NBT data usage
- Consider server impact

### 3. **Compatibility**
- Work with vanilla Minecraft
- Don't break existing worlds
- Respect other mods
- Standard recipes = automation friendly

### 4. **User Experience**
- Clear, informative tooltips
- Intuitive compression/decompression
- Helpful error messages

### 5. **Two Versions**
- FULL: All features, for builders and creative play
- LITE: Inventory-only, for servers and automation

## 🎯 Priority Areas

We especially welcome contributions in:

1. **Performance** - Optimize recipe generation and block handling
2. **Compatibility** - Better integration with popular mods
3. **Documentation** - Expand guides, add examples
4. **Localization** - Translations for tooltips and messages
5. **Testing** - Report edge cases and unusual scenarios

## 📜 Code of Conduct

### Our Standards

- Be respectful and welcoming
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards others

### Unacceptable Behavior

- Harassment, trolling, or insulting comments
- Personal or political attacks
- Publishing others' private information
- Other unprofessional conduct

## 📞 Getting Help

- **Issues**: Search existing issues first
- **Discord**: Join our [Discord server](https://discord.gg/mpThbx67J7)
- **Live**: Watch development on [Twitch](https://www.twitch.tv/strixun)

## 📄 License

By contributing to Compressy, you agree that your contributions will be licensed under the MIT License.

## 🙏 Recognition

Contributors will be recognized in:
- CHANGELOG.md for their specific contributions
- Special thanks in documentation
- Community appreciation!

---

**Thank you for contributing to Compressy!** 📦

Your efforts help make this mod better for the entire Minecraft community.

