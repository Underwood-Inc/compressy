# Changesets

This folder contains changeset files for version management.

## How to use

### Creating a changeset

When you make changes that should be included in the changelog:

```bash
npx changeset
```

This will prompt you to:
1. Select the type of change (patch/minor/major)
2. Write a summary of the change

### Changeset types

- **patch**: Bug fixes, small improvements (0.0.X)
- **minor**: New features, backwards compatible (0.X.0)  
- **major**: Breaking changes (X.0.0)

### Automatic versioning

If no changeset is present, the CI will automatically bump the patch version on each build.
This ensures every build has a unique version number.

