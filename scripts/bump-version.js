#!/usr/bin/env node
/**
 * Version bumping script for Compressy
 * 
 * Usage:
 *   node scripts/bump-version.js [patch|minor|major]
 * 
 * If no argument provided, defaults to 'patch'
 * Updates gradle.properties with the new version
 */

const fs = require('fs');
const path = require('path');

const GRADLE_PROPS_PATH = path.join(__dirname, '..', 'gradle.properties');

function readVersion() {
    const content = fs.readFileSync(GRADLE_PROPS_PATH, 'utf8');
    const match = content.match(/mod_version=(\d+\.\d+\.\d+)/);
    if (!match) {
        throw new Error('Could not find mod_version in gradle.properties');
    }
    return match[1];
}

function writeVersion(newVersion) {
    let content = fs.readFileSync(GRADLE_PROPS_PATH, 'utf8');
    content = content.replace(/mod_version=\d+\.\d+\.\d+/, `mod_version=${newVersion}`);
    fs.writeFileSync(GRADLE_PROPS_PATH, content, 'utf8');
}

function bumpVersion(currentVersion, bumpType) {
    const [major, minor, patch] = currentVersion.split('.').map(Number);
    
    switch (bumpType) {
        case 'major':
            return `${major + 1}.0.0`;
        case 'minor':
            return `${major}.${minor + 1}.0`;
        case 'patch':
        default:
            return `${major}.${minor}.${patch + 1}`;
    }
}

// Main
const bumpType = process.argv[2] || 'patch';

if (!['patch', 'minor', 'major'].includes(bumpType)) {
    console.error(`Invalid bump type: ${bumpType}`);
    console.error('Usage: node bump-version.js [patch|minor|major]');
    process.exit(1);
}

try {
    const currentVersion = readVersion();
    const newVersion = bumpVersion(currentVersion, bumpType);
    
    writeVersion(newVersion);
    
    console.log(`${currentVersion} -> ${newVersion}`);
    
    // Output for GitHub Actions
    if (process.env.GITHUB_OUTPUT) {
        fs.appendFileSync(process.env.GITHUB_OUTPUT, `old_version=${currentVersion}\n`);
        fs.appendFileSync(process.env.GITHUB_OUTPUT, `new_version=${newVersion}\n`);
    }
} catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
}

