const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

/**
 * Resolves the appropriate Python executable path across environments.
 */
function resolvePythonBinary() {
    if (process.env.PYTHON_BIN && fs.existsSync(process.env.PYTHON_BIN)) {
        return process.env.PYTHON_BIN;
    }

    // Check project isolated virtualenv first (repo root or nested)
    const projectRoot = path.resolve(__dirname, '../../../');
    const winVenv = path.join(projectRoot, 'RAG', 'venv', 'Scripts', 'python.exe');
    const linuxVenv = path.join(projectRoot, 'RAG', 'venv', 'bin', 'python');
    const nestedWinVenv = path.resolve(__dirname, '../../RAG/venv/Scripts/python.exe');
    const nestedLinuxVenv = path.resolve(__dirname, '../../RAG/venv/bin/python');

    if (fs.existsSync(winVenv)) return winVenv;
    if (fs.existsSync(linuxVenv)) return linuxVenv;
    if (fs.existsSync(nestedWinVenv)) return nestedWinVenv;
    if (fs.existsSync(nestedLinuxVenv)) return nestedLinuxVenv;

    // Check localappdata Python on Windows
    if (process.env.LOCALAPPDATA) {
        const localPy = path.join(process.env.LOCALAPPDATA, 'Programs', 'Python', 'Python312', 'python.exe');
        if (fs.existsSync(localPy)) return localPy;
    }

    // Default system path
    return process.platform === 'win32' ? 'python' : 'python3';
}

/**
 * Controller for POST /api/rag/ask
 */
exports.askQuestion = (req, res) => {
    const rawQuery = req.body.query || req.body.question;

    // 1. Input Validation
    if (!rawQuery || typeof rawQuery !== 'string' || rawQuery.trim().length === 0) {
        return res.status(400).json({
            status: 'error',
            code: 'INVALID_QUERY',
            message: 'Query must be a non-empty string.'
        });
    }

    const query = rawQuery.trim();

    if (query.length > 1000) {
        return res.status(400).json({
            status: 'error',
            code: 'QUERY_TOO_LONG',
            message: 'Query must not exceed 1000 characters.'
        });
    }

    let k = parseInt(req.body.k, 10);
    if (isNaN(k) || k < 1) k = 5;
    if (k > 10) k = 10;

    let mode = 'hybrid';
    if (req.body.mode && ['hybrid', 'semantic', 'lexical'].includes(String(req.body.mode).toLowerCase())) {
        mode = String(req.body.mode).toLowerCase();
    }

    const payloadObj = { query, k, mode };
    const minScore = req.body.min_score !== undefined ? parseFloat(req.body.min_score) : (req.body.relevance_threshold !== undefined ? parseFloat(req.body.relevance_threshold) : undefined);
    if (minScore !== undefined && !isNaN(minScore)) {
        payloadObj.min_score = minScore;
    }
    const relThreshold = req.body.relative_threshold !== undefined ? parseFloat(req.body.relative_threshold) : undefined;
    if (relThreshold !== undefined && !isNaN(relThreshold)) {
        payloadObj.relative_threshold = relThreshold;
    }

    // 2. Resolve script and database paths (prioritizes self-contained backend/RAG, falls back to project root)
    let ragDir = path.resolve(__dirname, '../../RAG');
    const nestedMetaPath = path.join(ragDir, 'semantic_index', 'scheme_metadata.json');
    if (!fs.existsSync(nestedMetaPath)) {
        const projectRoot = path.resolve(__dirname, '../../../');
        const rootRagDir = path.join(projectRoot, 'RAG');
        if (fs.existsSync(path.join(rootRagDir, 'semantic_index', 'scheme_metadata.json')) || fs.existsSync(path.join(rootRagDir, 'chroma_db', 'chroma.sqlite3'))) {
            ragDir = rootRagDir;
        }
    }

    const scriptPath = path.join(ragDir, 'src', 'query_engine.py');
    const semanticIndexPath = path.join(ragDir, 'semantic_index', 'scheme_metadata.json');
    const dbPath = path.join(ragDir, 'chroma_db', 'chroma.sqlite3');

    if (!fs.existsSync(semanticIndexPath) && !fs.existsSync(dbPath)) {
        console.error('[RAG Controller] Neither semantic index nor Chroma database found at:', ragDir);
        return res.status(503).json({
            status: 'error',
            code: 'VECTOR_INDEX_UNAVAILABLE',
            message: 'Scheme knowledge base is currently unavailable. Please try again shortly.'
        });
    }

    if (!fs.existsSync(scriptPath)) {
        console.error('[RAG Controller] RAG query script not found at:', scriptPath);
        return res.status(503).json({
            status: 'error',
            code: 'RAG_SERVICE_UNAVAILABLE',
            message: 'RAG pipeline service is not available.'
        });
    }

    const pythonBin = resolvePythonBinary();

    // 3. Spawn Python process with timeout
    const timeoutMs = 30000;
    let timedOut = false;
    let stdoutData = '';
    let stderrData = '';

    const payload = JSON.stringify(payloadObj);

    let pyProcess;
    try {
        pyProcess = spawn(pythonBin, [scriptPath, '--stdin'], {
            cwd: ragDir,
            env: {
                ...process.env,
                PYTHONIOENCODING: 'utf-8',
                FASTEMBED_CACHE_PATH: process.env.FASTEMBED_CACHE_PATH || path.join(ragDir, '.cache')
            }
        });
    } catch (err) {
        console.error('[RAG Controller] Failed to spawn python process:', err.message);
        return res.status(503).json({
            status: 'error',
            code: 'PROCESS_SPAWN_FAILED',
            message: 'Unable to start RAG retrieval process.'
        });
    }

    const timer = setTimeout(() => {
        timedOut = true;
        try {
            if (process.platform === 'win32') {
                pyProcess.kill();
            } else {
                pyProcess.kill('SIGKILL');
            }
        } catch (e) {}
    }, timeoutMs);

    pyProcess.stdin.write(payload);
    pyProcess.stdin.end();

    pyProcess.stdout.on('data', (chunk) => {
        stdoutData += chunk.toString('utf-8');
    });

    pyProcess.stderr.on('data', (chunk) => {
        stderrData += chunk.toString('utf-8');
    });

    pyProcess.on('error', (err) => {
        clearTimeout(timer);
        console.error('[RAG Controller] Subprocess error:', err.message);
        return res.status(503).json({
            status: 'error',
            code: 'RAG_EXECUTION_ERROR',
            message: 'Failed executing RAG query process.'
        });
    });

    pyProcess.on('close', (code) => {
        clearTimeout(timer);

        if (timedOut) {
            console.error('[RAG Controller] Subprocess timed out after 15s');
            return res.status(504).json({
                status: 'error',
                code: 'GATEWAY_TIMEOUT',
                message: 'RAG query processing timed out. Please try again.'
            });
        }

        if (code !== 0) {
            console.error(`[RAG Controller] Subprocess exited with code ${code}. Stderr: ${stderrData.slice(0, 300)}`);
            return res.status(502).json({
                status: 'error',
                code: 'RAG_PROCESSING_FAILED',
                message: 'Failed to process RAG query.'
            });
        }

        try {
            const parsed = JSON.parse(stdoutData.trim());
            return res.status(200).json(parsed);
        } catch (parseErr) {
            console.error('[RAG Controller] Failed to parse Python stdout as JSON:', stdoutData.slice(0, 200));
            return res.status(502).json({
                status: 'error',
                code: 'MALFORMED_RAG_RESPONSE',
                message: 'Invalid response from RAG service.'
            });
        }
    });
};
