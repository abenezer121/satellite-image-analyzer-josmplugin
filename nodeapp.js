const http = require('http');
const url = require('url');


const server = http.createServer((req, res) => {
    if (req.method === 'GET' && req.url.startsWith('/coordinates')) {
        const queryParams = url.parse(req.url, true).query;
        const bbox = {
            minLat: parseFloat(queryParams.minLat),
            maxLat: parseFloat(queryParams.maxLat),
            minLon: parseFloat(queryParams.minLon),
            maxLon: parseFloat(queryParams.maxLon)
        };

        if (isNaN(bbox.minLat) || isNaN(bbox.maxLat) ||
            isNaN(bbox.minLon) || isNaN(bbox.maxLon)) {
            res.writeHead(400, { 'Content-Type': 'text/plain' });
            res.end('Invalid bounding box parameters');
            return;
        }

     
        const responseLines = generateDummyLines(bbox);

        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify(responseLines));
    } else {
        res.writeHead(404);
        res.end('Not found');
    }
});

function generateDummyLines(bbox) {
    const generateLine = () => {
        const line = [];
        for (let i = 0; i < 5; i++) {
            const lat = bbox.minLat + (bbox.maxLat - bbox.minLat) * (i / 4);
            const lon = bbox.minLon + (bbox.maxLon - bbox.minLon) * Math.random();
            line.push([lat, lon]);
        }
        return line;
    };
    return [generateLine(), generateLine()];
}

server.listen(3000, () => {
    console.log('Server running at http://localhost:3000/');
});
