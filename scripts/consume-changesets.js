#!/usr/bin/env node
/**
 * Remove consumed changeset files after release
 */

const fs = require('fs');
const path = require('path');

const changesetFiles = process.argv.slice(2);

if (changesetFiles.length === 0) {
    console.log('No changeset files to consume');
    process.exit(0);
}

const CHANGESET_DIR = path.join(__dirname, '..', '.changeset');

for (const file of changesetFiles) {
    const filePath = path.join(CHANGESET_DIR, file);
    
    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
        console.log(`Removed: ${file}`);
    }
}

console.log(`Consumed ${changesetFiles.length} changeset(s)`);

