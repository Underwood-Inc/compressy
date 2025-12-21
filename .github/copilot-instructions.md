# GitHub Copilot Instructions for Compressy

## Git Operations - CRITICAL RULES

- **NEVER** automatically run `git commit` or `git push` commands
- **NEVER** commit files without explicit user permission
- When a task is complete, ASK: "Ready to commit? Here's what changed: [list files]"
- Wait for user confirmation before any git operations
- Always confirm commit message before executing

## Build & Run Commands

- **NEVER** run build scripts (`gradlew`, `pnpm build`) unless user explicitly requests
- Ask before running any long-running or potentially destructive commands
- When suggesting builds, ask: "Want me to run the build to verify?"

## CI/CD & Workflows

- When modifying GitHub Actions:
  - Validate YAML syntax
  - Check file permissions (gradlew needs chmod +x on Linux)
  - Verify referenced files/scripts exist
  - Consider cross-platform differences

## Package Manager

- Use `pnpm`, not `npm` or `npx`
- Version is defined in `package.json` `packageManager` field

## Commit Standards

- Use conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, etc.
- Keep commit messages concise but descriptive

## Task Completion Protocol

1. Complete all required changes
2. Verify changes are correct
3. List modified files
4. **ASK** for permission to commit
5. Wait for explicit confirmation
6. Only then execute git commands

