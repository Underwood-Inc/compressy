#!/usr/bin/env node
/**
 * Check for pending changesets and determine bump type
 * 
 * Outputs:
 *   has_changesets: true/false
 *   bump_type: major/minor/patch
 *   changelog: Combined changelog text
 */

const fs = require('fs');
const path = require('path');

const CHANGESET_DIR = path.join(__dirname, '..', '.changeset');

function getChangesetFiles() {
    if (!fs.existsSync(CHANGESET_DIR)) {
        return [];
    }
    
    return fs.readdirSync(CHANGESET_DIR)
        .filter(file => file.endsWith('.md') && file !== 'README.md');
}

function parseChangeset(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const lines = content.split('\n');
    
    let bumpType = 'patch';
    let description = [];
    let inFrontmatter = false;
    let frontmatterDone = false;
    
    for (const line of lines) {
        if (line.trim() === '---') {
            if (!inFrontmatter) {
                inFrontmatter = true;
            } else {
                inFrontmatter = false;
                frontmatterDone = true;
            }
            continue;
        }
        
        if (inFrontmatter) {
            // Parse frontmatter for bump type
            // Format: "compressy": patch
            const match = line.match(/"?compressy"?\s*:\s*(major|minor|patch)/i);
            if (match) {
                bumpType = match[1].toLowerCase();
            }
        } else if (frontmatterDone && line.trim()) {
            description.push(line.trim());
        }
    }
    
    return { bumpType, description: description.join(' ') };
}

function getBumpPriority(type) {
    switch (type) {
        case 'major': return 3;
        case 'minor': return 2;
        case 'patch': return 1;
        default: return 0;
    }
}

// Main
const changesetFiles = getChangesetFiles();

if (changesetFiles.length === 0) {
    console.log('No changesets found');
    
    if (process.env.GITHUB_OUTPUT) {
        fs.appendFileSync(process.env.GITHUB_OUTPUT, `has_changesets=false\n`);
        fs.appendFileSync(process.env.GITHUB_OUTPUT, `bump_type=patch\n`);
        fs.appendFileSync(process.env.GITHUB_OUTPUT, `changelog=Automatic patch release\n`);
    }
    
    process.exit(0);
}

// Parse all changesets
let highestBump = 'patch';
const descriptions = [];

for (const file of changesetFiles) {
    const filePath = path.join(CHANGESET_DIR, file);
    const { bumpType, description } = parseChangeset(filePath);
    
    if (getBumpPriority(bumpType) > getBumpPriority(highestBump)) {
        highestBump = bumpType;
    }
    
    if (description) {
        descriptions.push(`- ${description}`);
    }
}

const changelog = descriptions.join('\n') || 'Version bump';

console.log(`Found ${changesetFiles.length} changeset(s)`);
console.log(`Bump type: ${highestBump}`);
console.log(`Changelog:\n${changelog}`);

if (process.env.GITHUB_OUTPUT) {
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `has_changesets=true\n`);
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `bump_type=${highestBump}\n`);
    // Escape newlines for GitHub Actions
    const escapedChangelog = changelog.replace(/\n/g, '%0A');
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `changelog=${escapedChangelog}\n`);
    fs.appendFileSync(process.env.GITHUB_OUTPUT, `changeset_files=${changesetFiles.join(',')}\n`);
}

