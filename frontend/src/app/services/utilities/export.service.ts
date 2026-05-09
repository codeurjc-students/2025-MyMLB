import { Injectable } from '@angular/core';
import JSZip from 'jszip';

@Injectable({
	providedIn: 'root',
})
export class ExportService {
	public downloadPNG(chartCanvas: HTMLCanvasElement, fileName: string) {
		if (!chartCanvas) {
			return;
		}

		const logo = new Image();
		logo.src = 'assets/logo.png';
		logo.crossOrigin = 'anonymous';

		logo.onload = () => {
			const tempCanvas = document.createElement('canvas');
			const ctx = tempCanvas.getContext('2d');
			if (!ctx) {
				return;
			}

			// Add extra space below the chart for the logo
			const extraSpace = 80;
			tempCanvas.width = chartCanvas.width;
			tempCanvas.height = chartCanvas.height + extraSpace;

			// Render the Chart
			ctx.drawImage(chartCanvas, 0, 0);

			// Locate the Logo
			const logoHeight = 70;
			const logoWidth = (logo.width * logoHeight) / logo.height;

			const paddingRight = 20;
			const paddingBottom = 20;

			const x = tempCanvas.width - logoWidth - paddingRight;
			const y = tempCanvas.height - logoHeight - paddingBottom;

			ctx.drawImage(logo, x, y, logoWidth, logoHeight);

			// Perform the Download
			const image = tempCanvas.toDataURL('image/png', 1.0);
			const link = document.createElement('a');
			link.href = image;
			link.download = `${fileName}.png`;
			link.click();
		};
	}

	async downloadZip(zipFile: JSZip, zipName: string) {
		const zipContent = await zipFile.generateAsync({ type: 'blob' });
		const link = document.createElement('a');
		link.href = URL.createObjectURL(zipContent);
		link.download = zipName;
		link.click();
	}
}