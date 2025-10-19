import sharp from 'sharp';
import fs from 'fs';
import path from 'path';

const inputBase = '../assets/team-logos';
const outputBase = '../assets/team-logos-new';
const canvasSize = 100;
const logoSize = 70;

function processImage(inputPath: string, outputPath: string) {
	sharp(inputPath)
		.trim()
		.resize(logoSize, logoSize, {
			fit: 'contain',
			background: { r: 255, g: 255, b: 255, alpha: 0 },
		})
		.extend({
			top: Math.floor((canvasSize - logoSize) / 2),
			bottom: Math.ceil((canvasSize - logoSize) / 2),
			left: Math.floor((canvasSize - logoSize) / 2),
			right: Math.ceil((canvasSize - logoSize) / 2),
			background: { r: 255, g: 255, b: 255, alpha: 0 },
		})
		.png()
		.toFile(outputPath)
		.then(() => console.log(`✅ Normalizado: ${outputPath}`))
		.catch((err) => console.error(`❌ Error con ${inputPath}:`, err));
}

function walkAndProcess(dir: string) {
	fs.readdirSync(dir).forEach((entry) => {
		const inputPath = path.join(dir, entry);
		const stat = fs.statSync(inputPath);

		if (stat.isDirectory()) {
			walkAndProcess(inputPath);
		} else if (stat.isFile() && /\.(png|jpg|jpeg)$/i.test(entry)) {
			const relativePath = path.relative(inputBase, inputPath);
			const outputPath = path.join(outputBase, relativePath);

			fs.mkdirSync(path.dirname(outputPath), { recursive: true });
			processImage(inputPath, outputPath);
		}
	});
}

walkAndProcess(inputBase);