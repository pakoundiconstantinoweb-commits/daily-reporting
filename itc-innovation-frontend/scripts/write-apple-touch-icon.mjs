import { createWriteStream } from 'node:fs';
import { once } from 'node:events';
import { deflateSync } from 'node:zlib';
import { createHash } from 'node:crypto';

const SIZE = 180;

const hex = (value) => [
  Number.parseInt(value.slice(1, 3), 16),
  Number.parseInt(value.slice(3, 5), 16),
  Number.parseInt(value.slice(5, 7), 16),
];
const from = hex('#655af5');
const to = hex('#00a8c6');

const mix = (a, b, t) => a.map((channel, index) => Math.round(channel + (b[index] - channel) * t));

const inRoundedRect = (x, y, left, top, right, bottom, radius) => {
  const cx = Math.min(Math.max(x, left + radius), right - radius);
  const cy = Math.min(Math.max(y, top + radius), bottom - radius);
  if (x >= left + radius && x <= right - radius) return y >= top && y <= bottom;
  if (y >= top + radius && y <= bottom - radius) return x >= left && x <= right;
  const dx = x - cx;
  const dy = y - cy;
  return dx * dx + dy * dy <= radius * radius;
};

const distToSegment = (px, py, ax, ay, bx, by) => {
  const abx = bx - ax;
  const aby = by - ay;
  const t = Math.max(0, Math.min(1, ((px - ax) * abx + (py - ay) * aby) / (abx * abx + aby * aby)));
  const dx = px - (ax + t * abx);
  const dy = py - (ay + t * aby);
  return Math.hypot(dx, dy);
};

const pixels = Buffer.alloc(SIZE * (SIZE * 3 + 1));
for (let y = 0; y < SIZE; y += 1) {
  const row = y * (SIZE * 3 + 1);
  pixels[row] = 0;
  for (let x = 0; x < SIZE; x += 1) {
    const t = (x / (SIZE - 1) * 0.55) + (y / (SIZE - 1) * 0.45);
    let [r, g, b] = mix(from, to, Math.min(1, t));
    if (inRoundedRect(x, y, 42, 39, 129, 137, 14)) {
      r = 255;
      g = 255;
      b = 255;
    }
    if (distToSegment(x, y, 104, 39, 104, 64) < 4 || distToSegment(x, y, 104, 64, 129, 64) < 4) {
      [r, g, b] = hex('#00a8c6');
    }
    if (distToSegment(x, y, 65, 101, 79, 115) < 6 || distToSegment(x, y, 79, 115, 110, 81) < 6) {
      [r, g, b] = hex('#655af5');
    }
    const i = row + 1 + x * 3;
    pixels[i] = r;
    pixels[i + 1] = g;
    pixels[i + 2] = b;
  }
}

const crc = (buffer) => {
  const table = new Int32Array(256);
  for (let n = 0; n < 256; n += 1) {
    let c = n;
    for (let k = 0; k < 8; k += 1) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    table[n] = c;
  }
  let c = 0xffffffff;
  for (const byte of buffer) c = table[(c ^ byte) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};

const chunk = (type, data) => {
  const typeBuffer = Buffer.from(type);
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  const crcBuffer = Buffer.alloc(4);
  crcBuffer.writeUInt32BE(crc(Buffer.concat([typeBuffer, data])));
  return Buffer.concat([length, typeBuffer, data, crcBuffer]);
};

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(SIZE, 0);
ihdr.writeUInt32BE(SIZE, 4);
ihdr[8] = 8;
ihdr[9] = 2;
const png = Buffer.concat([
  Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
  chunk('IHDR', ihdr),
  chunk('IDAT', deflateSync(pixels, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);

const unusedHash = createHash('sha1').update(png).digest('hex');
const output = 'public/apple-touch-icon.png';
const stream = createWriteStream(output);
stream.end(png);
await once(stream, 'finish');
console.log(`wrote ${output} (${png.length} bytes, ${unusedHash.slice(0, 8)})`);
