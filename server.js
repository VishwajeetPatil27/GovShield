#!/usr/bin/env node

// GovShield Simple HTTP Server
// Alternative to Live Server if you prefer running from command line
// Usage: node server.js

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 5500;
const DIR = path.join(__dirname, 'frontend');

const server = http.createServer((req, res) => {
    // Default to index.html for root
    let filePath = path.join(DIR, req.url === '/' ? 'index.html' : req.url);
    
    // Security: prevent directory traversal
    if (!filePath.startsWith(DIR)) {
        res.writeHead(403, { 'Content-Type': 'text/plain' });
        res.end('Access denied');
        return;
    }

    // Try to serve the file
    fs.readFile(filePath, (err, data) => {
        if (err) {
            // Try adding .html extension
            filePath = filePath + '.html';
            fs.readFile(filePath, (err, data) => {
                if (err) {
                    res.writeHead(404, { 'Content-Type': 'text/plain' });
                    res.end('404 Not Found: ' + req.url);
                    return;
                }
                serveFile(res, filePath, data);
            });
        } else {
            serveFile(res, filePath, data);
        }
    });
});

function serveFile(res, filePath, data) {
    const ext = path.extname(filePath);
    let contentType = 'text/plain';

    switch (ext) {
        case '.html': contentType = 'text/html'; break;
        case '.css': contentType = 'text/css'; break;
        case '.js': contentType = 'text/javascript'; break;
        case '.json': contentType = 'application/json'; break;
        case '.png': contentType = 'image/png'; break;
        case '.jpg': contentType = 'image/jpeg'; break;
        case '.gif': contentType = 'image/gif'; break;
        case '.svg': contentType = 'image/svg+xml'; break;
        case '.woff': contentType = 'font/woff'; break;
        case '.woff2': contentType = 'font/woff2'; break;
    }

    res.writeHead(200, { 
        'Content-Type': contentType,
        'Cache-Control': 'no-cache, no-store, must-revalidate'
    });
    res.end(data);
}

server.listen(PORT, () => {
    console.log(`🚀 GovShield Frontend Server running at http://localhost:${PORT}`);
    console.log(`📁 Serving files from: ${DIR}`);
    console.log(`Press Ctrl+C to stop\n`);
});
