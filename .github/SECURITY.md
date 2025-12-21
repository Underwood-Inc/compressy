# Security Policy

## Supported Versions

The following versions of Compressy are currently supported with security updates:

| Version | Supported          | Minecraft Version |
| ------- | ------------------ | ----------------- |
| 1.0.x   | :white_check_mark: | 1.21.11           |
| < 1.0   | :x:                | Various           |

## Reporting a Vulnerability

If you discover a security vulnerability in Compressy, please report it responsibly.

### What Qualifies as a Security Issue?

For a Minecraft mod/datapack, security issues might include:

- **Command Injection** - Ability to execute unintended commands
- **Permission Bypass** - Non-operators gaining access to admin functions
- **Resource Exhaustion** - Exploits that cause excessive lag or crashes
- **Dupe Glitches** - Item duplication exploits
- **NBT Exploits** - Malicious NBT data causing issues
- **World Corruption** - Actions that corrupt world data

### How to Report

**DO NOT** open a public issue for security vulnerabilities.

Instead:

1. **Email** the maintainer privately (if contact info available)
2. **Open a private security advisory** via GitHub's Security tab
3. **Contact via Discord** in our [server](https://discord.gg/mpThbx67J7) (DM a moderator)
4. **Contact via Twitch** during live streams: [strixun](https://www.twitch.tv/strixun)

### What to Include

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if you have one)
- Your contact information (if you want credit)

### Response Timeline

- **24-48 hours**: Acknowledgment of your report
- **1 week**: Initial assessment and severity classification
- **2-4 weeks**: Patch development and testing
- **Release**: Security update published with credit to reporter (if desired)

## Security Best Practices for Users

### Server Operators

1. **Limit Operator Permissions**
   - Only give operator status to trusted players
   - Admin commands require operator level 2+

2. **Regular Backups**
   - Back up your world regularly
   - Test restore procedures

3. **Monitor Performance**
   - Watch for unusual lag spikes
   - Report potential exploits

4. **Choose the Right Version**
   - Use LITE version for public servers (smaller attack surface)
   - FULL version is fine for trusted environments

5. **Keep Updated**
   - Update to the latest version regularly
   - Read changelogs for security fixes

### Players

1. **Download from Official Sources**
   - Use official [Modrinth](https://modrinth.com/datapack/compressy) releases
   - Use official [GitHub](https://github.com/Underwood-Inc/compressy) releases
   - Verify file integrity

2. **Report Exploits**
   - If you find an exploit, report it privately
   - Don't share exploit details publicly

## Known Issues

### Not Security Issues

The following are **not** considered security vulnerabilities:

- **Balance Issues** - Overpowered compression levels (use feature request)
- **Vanilla Bugs** - Issues caused by Minecraft itself
- **Compatibility** - Conflicts with other mods (unless causing crashes)
- **Performance** - General lag (unless caused by exploitable loop)

## Security Updates

Security updates will be released as:
- **Patch versions** (1.0.1) for critical fixes
- **Announcements** in release notes marked with 🔒
- **Hotfixes** available immediately on GitHub

## Credits

We appreciate responsible disclosure. Security researchers who report valid vulnerabilities will be:
- Credited in the CHANGELOG (unless they prefer anonymity)
- Thanked in the release notes
- Recognized in the community

---

**Thank you for keeping Compressy safe!** 🔐

