/**
 * Comprehensive Backend & RAG Integration Test Suite
 * Tests Express API contracts, RAG query engine, failure modes,
 * database isolation, and security guarantees.
 */
const assert = require('assert');
const http = require('http');
const path = require('path');
const fs = require('fs');

// Ensure RAG runtime is synced
require('../scripts/sync-rag');

const app = require('../src/app');

let server;
let port;
let baseUrl;

function makeRequest(method, endpoint, body = null, headers = {}) {
    return new Promise((resolve, reject) => {
        const url = new URL(endpoint, baseUrl);
        const reqHeaders = { ...headers };
        let payload = null;

        if (body) {
            payload = typeof body === 'string' ? body : JSON.stringify(body);
            reqHeaders['Content-Type'] = 'application/json';
            reqHeaders['Content-Length'] = Buffer.byteLength(payload);
        }

        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method,
            headers: reqHeaders,
            timeout: 35000
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                let parsed = null;
                try {
                    parsed = JSON.parse(data);
                } catch (e) {
                    parsed = data;
                }
                resolve({ statusCode: res.statusCode, headers: res.headers, body: parsed });
            });
        });

        req.on('error', reject);
        req.on('timeout', () => {
            req.destroy(new Error('Request timed out'));
        });

        if (payload) {
            req.write(payload);
        }
        req.end();
    });
}

async function runTests() {
    console.log('=== JANSAARTHI Backend & RAG Integration Tests ===\n');
    let passed = 0;
    let failed = 0;

    async function test(name, fn) {
        try {
            process.stdout.write(`• ${name}... `);
            await fn();
            console.log('PASSED');
            passed++;
        } catch (err) {
            console.log('FAILED');
            console.error(`  Error: ${err.message}`);
            if (err.stack) console.error(`  ${err.stack.split('\n').slice(1, 3).join('\n')}`);
            failed++;
        }
    }

    // Start HTTP server on random ephemeral port
    await new Promise((resolve) => {
        server = app.listen(0, '127.0.0.1', () => {
            port = server.address().port;
            baseUrl = `http://127.0.0.1:${port}`;
            console.log(`Test server running on ${baseUrl}\n`);
            resolve();
        });
    });

    try {
        // Test 1: Root Health Check
        await test('GET / returns 200 with service info', async () => {
            const res = await makeRequest('GET', '/');
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'running');
            assert.strictEqual(res.body.name, 'JANSAARTHI Backend API');
        });

        // Test 2: RAG query - Empty query validation
        await test('POST /api/rag/ask rejects empty query with HTTP 400', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', { query: '   ' });
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.code, 'INVALID_QUERY');
        });

        // Test 3: RAG query - Query too long
        await test('POST /api/rag/ask rejects query exceeding 1000 characters with HTTP 400', async () => {
            const longQuery = 'A'.repeat(1005);
            const res = await makeRequest('POST', '/api/rag/ask', { query: longQuery });
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.code, 'QUERY_TOO_LONG');
        });

        // Test 4: RAG query - Hybrid mode (Default) with real scheme data
        await test('POST /api/rag/ask handles Hybrid query and returns top matching schemes', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', {
                query: 'Pradhan Mantri Mudra Yojana',
                mode: 'hybrid',
                k: 3
            });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.retrieval_mode, 'hybrid');
            assert.strictEqual(res.body.sources_count >= 1, true);
            assert.strictEqual(res.body.sources[0].schemeId, 'SCH002');
            assert.strictEqual(res.body.sources[0].schemeName, 'Pradhan Mantri Mudra Yojana');
            assert.strictEqual(res.body.sources[0].sourcePlatform, 'myScheme.gov.in');
        });

        // Test 5: RAG query - Semantic mode
        await test('POST /api/rag/ask handles Semantic mode for conceptual paraphrase', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', {
                query: 'Financial support for female business founders',
                mode: 'semantic',
                k: 5
            });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.retrieval_mode, 'semantic');
            assert.strictEqual(res.body.sources_count >= 1, true);
            // Verify Stand Up India (SCH003) or Mudra (SCH002) is retrieved
            const schemeIds = res.body.sources.map(s => s.schemeId);
            assert.strictEqual(schemeIds.includes('SCH003') || schemeIds.includes('SCH002'), true);
        });

        // Test 6: RAG query - Lexical mode
        await test('POST /api/rag/ask handles Lexical mode with token boundaries', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', {
                query: 'Kisan Credit Card',
                mode: 'lexical',
                k: 3
            });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.retrieval_mode, 'lexical');
            assert.strictEqual(res.body.sources_count >= 1, true);
            assert.strictEqual(res.body.sources[0].schemeId, 'SCH025');
        });

        // Test 7: Out-of-Domain (OOD) Protection
        await test('POST /api/rag/ask returns 0 sources and no_context for OOD query', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', {
                query: 'Galactic space orbit satellite quantum cryptographic blockchain',
                mode: 'hybrid'
            });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.sources_count, 0);
            assert.strictEqual(res.body.sources.length, 0);
            assert.strictEqual(res.body.llm_status, 'no_context');
        });

        // Test 8: Absence of GROQ_API_KEY does not break retrieval
        await test('POST /api/rag/ask safely handles absence of GROQ_API_KEY', async () => {
            const res = await makeRequest('POST', '/api/rag/ask', {
                query: 'PM-KISAN'
            });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.sources.length > 0, true);
            // Should contain clear unconfigured message or generated answer
            assert.strictEqual(['unconfigured_api_key', 'generated', 'service_error'].includes(res.body.llm_status), true);
        });

        // Test 9: RAG Endpoint works even when MongoDB is disconnected
        await test('RAG inquiry endpoint functions independently of MongoDB database state', async () => {
            // Note: MongoDB is not connected in this test server instance
            const res = await makeRequest('POST', '/api/rag/ask', { query: 'housing' });
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.status, 'success');
            assert.strictEqual(res.body.sources.length > 0, true);
        });

        // Test 10: Database-guarded routes return HTTP 503 when DB is disconnected
        await test('Database-guarded routes return HTTP 503 when MongoDB is disconnected', async () => {
            const res = await makeRequest('GET', '/api/recommendations');
            assert.strictEqual(res.statusCode, 503);
            assert.strictEqual(res.body.code, 'DATABASE_UNAVAILABLE');
        });

    } finally {
        server.close();
    }

    console.log(`\nTest Summary: ${passed} passed, ${failed} failed`);
    if (failed > 0) {
        process.exit(1);
    }
}

runTests();
