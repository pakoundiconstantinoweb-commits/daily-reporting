import { writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

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

const samplePoint = (x, y, size) => {
  const s = size / 180;
  const t = (x / (size - 1) * 0.55) + (y / (size - 1) * 0.45);
  let [r, g, b] = mix(from, to, Math.min(1, Math.max(0, t)));

  if (inRoundedRect(x, y, 42 * s, 39 * s, 129 * s, 137 * s, 14 * s)) {
    r = 255;
    g = 255;
    b = 255;
  }
  if (
    distToSegment(x, y, 104 * s, 39 * s, 104 * s, 64 * s) < Math.max(1.2, 4 * s) ||
    distToSegment(x, y, 104 * s, 64 * s, 129 * s, 64 * s) < Math.max(1.2, 4 * s)
  ) {
    [r, g, b] = hex('#00a8c6');
  }
  if (
    distToSegment(x, y, 65 * s, 101 * s, 79 * s, 115 * s) < Math.max(1.5, 6 * s) ||
    distToSegment(x, y, 79 * s, 115 * s, 110 * s, 81 * s) < Math.max(1.5, 6 * s)
  ) {
    [r, g, b] = hex('#655af5');
  }
  return [r, g, b];
};

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

export const generatePng = (size) => {
  const pixels = Buffer.alloc(size * (size * 3 + 1));
  const subOffsets = [0.25, 0.75];

  for (let y = 0; y < size; y += 1) {
    const row = y * (size * 3 + 1);
    pixels[row] = 0;
    for (let x = 0; x < size; x += 1) {
      let rSum = 0;
      let gSum = 0;
      let bSum = 0;
      for (const dy of subOffsets) {
        for (const dx of subOffsets) {
          const [r, g, b] = samplePoint(x + dx, y + dy, size);
          rSum += r;
          gSum += g;
          bSum += b;
        }
      }
      const i = row + 1 + x * 3;
      pixels[i] = Math.round(rSum / 4);
      pixels[i + 1] = Math.round(gSum / 4);
      pixels[i + 2] = Math.round(bSum / 4);
    }
  }

  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(size, 0);
  ihdr.writeUInt32BE(size, 4);
  ihdr[8] = 8;
  ihdr[9] = 2; // RGB

  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(pixels, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
};

for (const size of [48, 96, 192]) {
  const buffer = generatePng(size);
  const filename = `itc-innovation-frontend/public/icon-${size}x${size}.png`;
  writeFileSync(filename, buffer);
  console.log(`Generated ${filename} (${buffer.length} bytes)`);
}

// Also write favicon-48x48.png
const buf48 = generatePng(48);
writeFileSync('itc-innovation-frontend/public/favicon-48x48.png', buf48);
writeFileSync('itc-innovation-frontend/public/favicon.png', buf48);
console.log('All favicons generated successfully.');
