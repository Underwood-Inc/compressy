# .github Directory

This directory contains GitHub-specific configuration files, templates, and workflows for the Compressy project.

## 📁 Directory Structure

```
.github/
├── ISSUE_TEMPLATE/          # Issue templates for bug reports, features, etc.
│   ├── bug_report.yml       # Report bugs and issues
│   ├── feature_request.yml  # Suggest new features
│   ├── compatibility.yml    # Report mod/datapack conflicts
│   ├── documentation.yml    # Report documentation issues
│   └── config.yml           # Issue template configuration
├── workflows/               # GitHub Actions CI/CD workflows
│   ├── ci.yml              # Build and test on push/PR
│   └── release.yml         # Create releases and publish to Modrinth
├── PULL_REQUEST_TEMPLATE.md # PR template
├── CONTRIBUTING.md          # Contribution guidelines
├── SECURITY.md             # Security policy
├── FUNDING.yml             # Funding/support links
├── copilot-instructions.md # GitHub Copilot instructions
└── README.md               # This file
```

## 🎯 Issue Templates

We provide several issue templates to help organize and triage issues effectively:

### Bug Report (`bug_report.yml`)
For reporting bugs, glitches, or unexpected behavior. Includes:
- Installation type (FULL/LITE/Datapack)
- Version information
- Bug category (Compression, Tooltips, Commands, etc.)
- Reproduction steps
- Compression level details

**Use when**: Something isn't working as documented

### Feature Request (`feature_request.yml`)
For suggesting new features or improvements. Includes:
- Feature type
- Problem it solves
- Proposed implementation
- Version scope (FULL/LITE/Both)

**Use when**: You have an idea for a new feature

### Compatibility Issue (`compatibility.yml`)
For reporting conflicts with other mods or datapacks. Includes:
- Conflicting mod information
- Conflict type (crash, visual, feature)
- Complete mod list
- Behavior comparisons

**Use when**: Compressy conflicts with another mod/datapack

### Documentation Issue (`documentation.yml`)
For reporting missing, incorrect, or unclear documentation. Includes:
- Documentation type
- Issue type (missing, incorrect, unclear, etc.)
- Document location
- Suggested improvement

**Use when**: The docs are wrong, missing, or confusing

## 🔄 Pull Request Template

The PR template (`PULL_REQUEST_TEMPLATE.md`) provides a comprehensive checklist for contributors:

- **Change Type**: Bug fix, feature, documentation, etc.
- **Testing Checklist**: General, component-specific, version-specific
- **Documentation**: Updates required
- **Technical Details**: Architecture changes, performance impact

All PRs should fill out the relevant sections of this template.

## 🤝 Contributing Guidelines

See `CONTRIBUTING.md` for detailed contribution guidelines including:

- Development setup
- Project structure
- Coding standards
- Testing guidelines
- Commit message format
- Pull request process
- Design principles

**Required reading** before submitting a PR!

## 🔒 Security Policy

See `SECURITY.md` for:

- Supported versions
- How to report security vulnerabilities
- What qualifies as a security issue
- Response timeline
- Security best practices

**Never** report security issues publicly - use the private disclosure methods described.

## ⚙️ GitHub Actions Workflows

### CI Workflow (`workflows/ci.yml`)

**Triggers**:
- Push to `main` or `master` branches
- Pull requests

**Actions**:
1. Checks out the code
2. Sets up JDK 21
3. Builds with Gradle
4. Validates the build

**Purpose**: Ensures all commits build successfully

### Release Workflow (`workflows/release.yml`)

**Triggers**:
- Push to `main` or `master` branches
- Manual workflow dispatch

**Actions**:
1. Builds the project (FULL, LITE, Datapack)
2. Creates version tag
3. Creates GitHub release with all artifacts
4. Publishes to Modrinth (3 separate versions)

**Purpose**: Automates the release process

## 💰 Funding

The `FUNDING.yml` file configures the "Sponsor" button on GitHub. Currently links to:
- Twitch: https://www.twitch.tv/strixun
- Discord: https://discord.gg/mpThbx67J7

## 📝 Template Usage Tips

### For Users

1. **Choose the right template** - Use the most specific template for your issue
2. **Fill in all required fields** - This helps us help you faster
3. **Be detailed** - More information = faster resolution
4. **Search first** - Check if your issue already exists

### For Maintainers

1. **Triage new issues** - Review and add appropriate labels
2. **Use saved replies** - Create saved replies for common questions
3. **Close duplicates** - Link to the original issue
4. **Update templates** - Improve based on recurring unclear submissions

## 🏷️ Recommended Labels

Common labels to use with these templates:

**Type**:
- `bug` - Something isn't working
- `enhancement` - New feature or improvement
- `documentation` - Documentation changes
- `compatibility` - Mod/datapack conflicts

**Priority**:
- `critical` - Game-breaking, needs immediate fix
- `high` - Important but not critical
- `medium` - Should be addressed
- `low` - Nice to have

**Status**:
- `needs-triage` - Needs initial review
- `needs-investigation` - Requires investigation
- `needs-review` - Awaiting review
- `in-progress` - Being worked on

**Component**:
- `compression` - Compression mechanics
- `placement` - Block placement (FULL version)
- `tooltip` - Tooltip display
- `recipe` - Recipe generation
- `performance` - Performance/optimization

## 📊 Workflow Status

Check workflow status:
- **CI**: [![CI](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
- **Release**: [![Release](../../actions/workflows/release.yml/badge.svg)](../../actions/workflows/release.yml)

## 📚 Additional Resources

- [GitHub Issue Template Docs](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [YAML Template Reference](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/syntax-for-issue-forms)

---

**Questions about these templates?** Join our [Discord](https://discord.gg/mpThbx67J7) or ask on [Twitch](https://www.twitch.tv/strixun)!

