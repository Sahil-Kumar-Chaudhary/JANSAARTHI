/**
 * Syncs the minimal RAG runtime bundle into backend/RAG.
 * Ensures the deployment container has all necessary Python files
 * and semantic index assets even when Render Root Directory is 'backend'.
 */
const fs = require('fs');
const path = require('path');

const projectRoot = path.resolve(__dirname, '../../');
const srcRag = path.join(projectRoot, 'RAG');
const destRag = path.resolve(__dirname, '../RAG');

if (!fs.existsSync(srcRag)) {
    console.log('[sync-rag] Source RAG directory not found (already in packaged environment). Skipping.');
    process.exit(0);
}

console.log('[sync-rag] Syncing minimal RAG runtime assets into backend/RAG...');

// Ensure destination directories
fs.mkdirSync(path.join(destRag, 'src'), { recursive: true });
fs.mkdirSync(path.join(destRag, 'semantic_index'), { recursive: true });

// Copy runtime files
const filesToSync = [
    { src: path.join(srcRag, 'src', 'query_engine.py'), dest: path.join(destRag, 'src', 'query_engine.py') },
    { src: path.join(srcRag, 'src', '__init__.py'), dest: path.join(destRag, 'src', '__init__.py') },
    { src: path.join(srcRag, 'semantic_index', 'scheme_metadata.json'), dest: path.join(destRag, 'semantic_index', 'scheme_metadata.json') },
    { src: path.join(srcRag, 'semantic_index', 'scheme_embeddings.npy'), dest: path.join(destRag, 'semantic_index', 'scheme_embeddings.npy') },
    { src: path.join(srcRag, 'requirements_render.txt'), dest: path.join(destRag, 'requirements.txt') }
];

let syncedCount = 0;
for (const file of filesToSync) {
    if (fs.existsSync(file.src)) {
        fs.copyFileSync(file.src, file.dest);
        const stats = fs.statSync(file.dest);
        console.log(`[sync-rag] Synced: ${path.relative(destRag, file.dest)} (${Math.round(stats.size / 1024)} KB)`);
        syncedCount++;
    } else {
        console.warn(`[sync-rag] Source file not found: ${file.src}`);
    }
}

console.log(`[sync-rag] Successfully synced ${syncedCount} RAG runtime files into backend/RAG.`);
